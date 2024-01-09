package com.funny.translation.translate

import com.funny.translation.kmp.KMPActivity
import com.funny.translation.kmp.NavController
import kotlin.properties.Delegates

actual class TransActivity : KMPActivity {
    actual var navController: NavController by Delegates.notNull()

    init {

    }
}