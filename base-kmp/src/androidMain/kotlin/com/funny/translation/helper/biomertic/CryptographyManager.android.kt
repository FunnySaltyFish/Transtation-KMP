package com.funny.translation.helper.biomertic

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import com.funny.translation.helper.Context
import com.funny.translation.helper.JsonX
import com.funny.translation.helper.Log
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

@RequiresApi(Build.VERSION_CODES.M)
actual fun CryptographyManager(): CryptographyManager = CryptographyManagerImpl()

/**
 * To get an instance of this private CryptographyManagerImpl class, use the top-level function
 * fun CryptographyManager(): CryptographyManager = CryptographyManagerImpl()
 */
@RequiresApi(Build.VERSION_CODES.M)
private class CryptographyManagerImpl : CryptographyManager {

    private val keyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null) // Keystore must be loaded before it can be accessed
        }
    }

    companion object {
        private const val TAG = "CryptographyManagerImpl"
        private const val KEY_SIZE = 256
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    }



    override fun getInitializedCipherForEncryption(keyName: String, shouldRetry: Boolean): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey(keyName)
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        }catch (e: KeyPermanentlyInvalidatedException){
            Log.d(TAG, "key 过期了，删除重建 ")
            keyStore.deleteEntry(keyName)
            if (shouldRetry) return getInitializedCipherForEncryption(keyName, false)
            else throw Exception("getInitializedCipherForEncryption Key过期了，且无法重建")
        }

        return cipher
    }

    override fun getInitializedCipherForDecryption(
        keyName: String,
        initializationVector: ByteArray,
        shouldRetry: Boolean
    ): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey(keyName)
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, initializationVector))
        }catch (e: KeyPermanentlyInvalidatedException){
            Log.d(TAG, "key 过期了，删除重建 ")
            keyStore.deleteEntry(keyName)
            if (shouldRetry) return getInitializedCipherForDecryption(keyName, initializationVector, false)
            else throw Exception("getInitializedCipherForDecryption Key过期了，且无法重建")
        }

        return cipher
    }

    override fun encryptData(plaintext: String, cipher: Cipher): CiphertextWrapper {
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charset.forName("UTF-8")))
        return CiphertextWrapper(ciphertext, cipher.iv)
    }

    override fun decryptData(ciphertext: ByteArray, cipher: Cipher): String {
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charset.forName("UTF-8"))
    }

    private fun getCipher(): Cipher {
        val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"
        return Cipher.getInstance(transformation)
    }

    private fun getOrCreateSecretKey(keyName: String): SecretKey {
        keyStore.getKey(keyName, null)?.let { return it as SecretKey }

        // if you reach here, then a new SecretKey must be generated for that keyName
        val paramsBuilder = KeyGenParameterSpec.Builder(
            keyName,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        paramsBuilder.apply {
            setBlockModes(ENCRYPTION_BLOCK_MODE)
            setEncryptionPaddings(ENCRYPTION_PADDING)
            setKeySize(KEY_SIZE)
            setUserAuthenticationRequired(false)
        }

        val keyGenParams = paramsBuilder.build()
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }

    override fun persistCiphertextWrapperToSharedPrefs(
        ciphertextWrapper: CiphertextWrapper,
        context: Context,
        filename: String,
        mode: Int,
        prefKey: String
    ) {
        val json = JsonX.toJson(ciphertextWrapper)
        context.getSharedPreferences(filename, mode).edit().putString(prefKey, json).apply()
    }

    override fun getCiphertextWrapperFromSharedPrefs(
        context: Context,
        filename: String,
        mode: Int,
        prefKey: String
    ): CiphertextWrapper? {
        var json = context.getSharedPreferences(filename, mode).getString(prefKey, null) ?: return null
        // 处理 kotlinx.serialization.Json 和 Gson 的差异
        json = json.replace("ciphertext", "a").replace("initializationVector", "b")
        return JsonX.fromJson(json, CiphertextWrapper::class.java)
    }

    override fun clearCiphertextWrapperFromSharedPrefs(
        ctx: Context,
        filename: String,
        modePrivate: Int,
        username: String
    ) {
        ctx.getSharedPreferences(filename, modePrivate).edit().remove(username).apply()
    }
}
