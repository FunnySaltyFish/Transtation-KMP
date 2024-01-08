package com.funny.translation.translate

import com.funny.translation.kmp.KMPActivity
import com.funny.translation.kmp.NavController

expect class TransActivity: KMPActivity {
    var navController: NavController
}