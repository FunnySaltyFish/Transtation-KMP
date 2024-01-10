package com.funny.translation.translate

import com.funny.translation.BaseActivity
import com.funny.translation.kmp.NavController
import kotlin.properties.Delegates

actual class TransActivity : BaseActivity() {
    actual var navController: NavController by Delegates.notNull()
    val activityViewModel: ActivityViewModel by lazy {
        ActivityViewModel()
    }

    init {

    }
}