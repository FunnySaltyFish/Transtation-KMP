package com.funny.translation.kmp

import androidx.activity.ComponentActivity
import com.funny.translation.helper.toastOnUi

// IDE might say: Actual typealias 'KMPActivity' has no corresponding expected declaration The following declaration is incompatible because some supertypes are missing in the actual declaration:
// public open expect class KMPActivity : KMPContext defined in com.funny.translation.kmp in file Activity.kt
// it's a difference between K1 and K2, and this can be compiled successfully
actual typealias KMPActivity = ComponentActivity

fun KMPActivity.toastOnUi(msg: String) {
    appCtx.toastOnUi(msg)
}