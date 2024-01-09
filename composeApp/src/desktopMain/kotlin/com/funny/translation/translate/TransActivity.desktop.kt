package com.funny.translation.translate

import com.funny.translation.BaseActivity
import com.funny.translation.kmp.NavController
import kotlin.properties.Delegates

actual class TransActivity : BaseActivity() {
    actual var navController: NavController by Delegates.notNull()
    var activityViewModel: ActivityViewModel

    init {
        activityViewModel = ActivityViewModel()
    }
}