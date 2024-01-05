package com.funny.translation.helper.biomertic

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.funny.translation.AppConfig
import com.funny.translation.helper.Log
import com.funny.translation.helper.ServiceCreator
import com.funny.translation.helper.UserUtils
import com.funny.translation.helper.awaitDialog
import com.funny.translation.helper.handler.runOnUI
import com.funny.translation.kmp.KMPActivity
import com.funny.translation.kmp.appCtx
import com.funny.translation.kmp.base.strings.ResStrings
import com.funny.translation.kmp.toastOnUi
import com.funny.translation.network.api
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.M)
actual object BiometricUtils {
    private const val TAG = "BiometricUtils"
    private const val SHARED_PREFS_FILENAME = "biometric_prefs_v2"
    const val CIPHERTEXT_WRAPPER = "ciphertext_wrapper"
    private const val SECRET_KEY = "trans_key"

    // 用于注册时临时保存当前的指纹数据
    actual var tempSetFingerPrintInfo = FingerPrintInfo()
    actual var tempSetUserName = ""

    actual val fingerPrintService by lazy(LazyThreadSafetyMode.PUBLICATION) {
        ServiceCreator.create(FingerPrintService::class.java)
    }

    private val biometricManager by lazy {
        BiometricManager.from(appCtx)
    }

    actual val cryptographyManager = CryptographyManager()

    private val scope by lazy {
        CoroutineScope(Dispatchers.IO)
    }

    actual fun checkBiometricAvailable(): String = when (biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG
    )) {
        BiometricManager.BIOMETRIC_SUCCESS -> ""
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> ResStrings.err_no_fingerprint_device
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> ResStrings.err_fingerprint_not_enabled
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> ResStrings.err_no_fingerprint
        else -> ResStrings.unknown_error
    }

    actual fun init() {

    }

    actual suspend fun uploadFingerPrint(username: String) = withContext(Dispatchers.IO){
        fingerPrintService.saveFingerPrintInfo(
            username = username,
            did = AppConfig.androidId,
            encryptedInfo = tempSetFingerPrintInfo.encrypted_info,
            iv = tempSetFingerPrintInfo.iv,
        )
    }

    // 设置指纹信息，相关内容会暂存，等到注册时提交
    actual fun setFingerPrint(
        activity: KMPActivity,
        username: String,
        did: String,
        onNotSupport: (msg: String) -> Unit,
        onSuccess: (String, String) -> Unit,
        onUsePassword: () -> Unit,
        onFail: () -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit
    ) {
        val error = checkBiometricAvailable()
        if (error != "") {
            onNotSupport(error)
            return
        }

        val secretKeyName = SECRET_KEY
        try {
            val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
            val biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(activity, authSuccess = { authResult ->
                    authResult.cryptoObject?.cipher?.apply {
                        val encryptedServerTokenWrapper =
                            cryptographyManager.encryptData("$username@$did", this)

                        val ei = encryptedServerTokenWrapper.ciphertext.joinToString(",")
                        val iv = encryptedServerTokenWrapper.initializationVector.joinToString(",")

                        cryptographyManager.persistCiphertextWrapperToSharedPrefs(
                            encryptedServerTokenWrapper,
                            appCtx,
                            SHARED_PREFS_FILENAME,
                            Context.MODE_PRIVATE,
                            username
                        )

                        tempSetFingerPrintInfo.iv = iv
                        tempSetFingerPrintInfo.encrypted_info = ei
                        tempSetUserName = username

                        onSuccess(ei, iv)
                    }
                }, authError = { errorCode: Int, errString: String ->
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        onUsePassword()
                    } else {
                        onError(errorCode, errString)
                    }
                }, onFail) ?: return
            val promptInfo = BiometricPromptUtils.createPromptInfo()
            runOnUI {
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        }catch (e: Exception){
            e.printStackTrace()
            onFail()
        }
    }

    actual fun validateFingerPrint(
        activity: KMPActivity,
        username: String,
        did: String,
        onNotSupport: (msg: String) -> Unit,
        onSuccess: (String, String) -> Unit,
        onFail: () -> Unit,
        onUsePassword: () -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit,
        // 新设备登录时回调
        onNewFingerPrint: (email: String) -> Unit
    ) {
        val error = checkBiometricAvailable()
        if (error != "") {
            onNotSupport(error)
            return
        }

        val ciphertextWrapper = cryptographyManager.getCiphertextWrapperFromSharedPrefs(
            appCtx,
            SHARED_PREFS_FILENAME,
            Context.MODE_PRIVATE,
            username
        )

        if (ciphertextWrapper == null) {
            scope.launch(Dispatchers.IO) {
                val email = api(UserUtils.userService::getUserEmail, username)
                if (email == null || email == ""){
                    activity.toastOnUi(ResStrings.not_registered)
                    return@launch
                }

                if (activity !is AppCompatActivity) return@launch
                if (awaitDialog(
                        activity,
                        ResStrings.hint,
                        ResStrings.tip_reset_fingerprint.format(
                            username,
                            UserUtils.anonymousEmail(email)
                        ),
                        ResStrings.confirm,
                        ResStrings.cancel,
                    )
                ) {
                    setFingerPrint(
                        activity,
                        username,
                        did,
                        onSuccess = { encryptedInfo, iv ->
                            onSuccess(encryptedInfo, iv)
                            onNewFingerPrint(email)
                        },
                        onFail = {
                            onError(-2, ResStrings.validate_fingerprint_failed)
                        },
                        onError = onError,
                        onUsePassword = onUsePassword
                    )
                }
            }
        } else {
            val secretKeyName = SECRET_KEY
            try {
                val cipher = cryptographyManager.getInitializedCipherForDecryption(
                    secretKeyName, ciphertextWrapper.initializationVector
                )
                val biometricPrompt = BiometricPromptUtils.createBiometricPrompt(
                    activity,
                    authSuccess = { authResult ->
                        authResult.cryptoObject?.cipher?.let {
                            val plaintext =
                                cryptographyManager.decryptData(ciphertextWrapper.ciphertext, it)
                            Log.d(TAG, "validateFingerPrint: plainText: $plaintext")
                            //                                    SampleAppUser.fakeToken = plaintext
                            // Now that you have the token, you can query server for everything else
                            // the only reason we call this fakeToken is because we didn't really get it from
                            // the server. In your case, you will have gotten it from the server the first time
                            // and therefore, it's a real token.
                            onSuccess(
                                ciphertextWrapper.ciphertext.joinToString(","),
                                ciphertextWrapper.initializationVector.joinToString(",")
                            )
                        }
                    },
                    authError = { errorCode, errString ->
                        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            onUsePassword()
                        } else {
                            onError(errorCode, errString)
                        }
                    },
                    onFail
                ) ?: return
                val promptInfo = BiometricPromptUtils.createPromptInfo()
                runOnUI {
                    biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
                }
            }catch (e: Exception){
                e.printStackTrace()
                onFail()
            }
        }
    }

    actual fun clearFingerPrintInfo(username: String){
        tempSetFingerPrintInfo = FingerPrintInfo()
        tempSetUserName = ""
        cryptographyManager.clearCiphertextWrapperFromSharedPrefs(
            appCtx,
            SHARED_PREFS_FILENAME,
            android.content.Context.MODE_PRIVATE,
            username
        )
    }
}