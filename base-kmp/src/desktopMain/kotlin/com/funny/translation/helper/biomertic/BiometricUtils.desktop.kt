package com.funny.translation.helper.biomertic

import com.funny.translation.kmp.KMPActivity

actual object BiometricUtils {
    internal actual var tempSetFingerPrintInfo: FingerPrintInfo
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var tempSetUserName: String
        get() = TODO("Not yet implemented")
        set(value) {}
    internal actual val fingerPrintService: FingerPrintService
        get() = TODO("Not yet implemented")
    internal actual val cryptographyManager: CryptographyManager
        get() = TODO("Not yet implemented")

    internal actual fun checkBiometricAvailable(): String {
        TODO("Not yet implemented")
    }

    actual fun init() {
    }

    actual suspend fun uploadFingerPrint(username: String) {
    }

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
        onNewFingerPrint: (email: String) -> Unit
    ) {
    }

    actual fun clearFingerPrintInfo(username: String) {
    }
}