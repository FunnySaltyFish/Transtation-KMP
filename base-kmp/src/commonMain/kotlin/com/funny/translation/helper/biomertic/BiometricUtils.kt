package com.funny.translation.helper.biomertic

import com.funny.translation.kmp.KMPActivity


expect object BiometricUtils {
    internal var tempSetFingerPrintInfo: FingerPrintInfo
    var tempSetUserName: String

    internal val fingerPrintService: FingerPrintService

    internal val cryptographyManager: CryptographyManager


    internal fun checkBiometricAvailable(): String

    fun init()

    suspend fun uploadFingerPrint(username: String)

    // 设置指纹信息，相关内容会暂存，等到注册时提交
    fun setFingerPrint(
        activity: KMPActivity,
        username: String,
        did: String,
        onNotSupport: (msg: String) -> Unit = { _ -> },
        onSuccess: (String, String) -> Unit = { _, _ -> },
        onUsePassword: () -> Unit = {},
        onFail: () -> Unit = {},
        onError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> }
    )

    fun validateFingerPrint(
        activity: KMPActivity,
        username: String,
        did: String,
        onNotSupport: (msg: String) -> Unit = { _ -> },
        onSuccess: (String, String) -> Unit = { _, _ -> },
        onFail: () -> Unit = {},
        onUsePassword: () -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> },
        // 新设备登录时回调
        onNewFingerPrint: (email: String) -> Unit = {}
    )

    fun clearFingerPrintInfo(username: String)
}