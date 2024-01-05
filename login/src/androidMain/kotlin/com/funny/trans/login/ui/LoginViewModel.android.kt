package com.funny.trans.login.ui

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.M)
actual val supportBiometric: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M