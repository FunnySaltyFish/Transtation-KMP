package com.funny.translation.translate

import com.funny.translation.BaseActivity
import com.funny.translation.kmp.NavController

actual class TransActivity : BaseActivity() {
    actual var navController: NavController? = null

    val activityViewModel: ActivityViewModel by lazy {
        ActivityViewModel()
    }

    override fun onShow() {
        super.onShow()
        activityViewModel.refreshUserInfo()
    }
}