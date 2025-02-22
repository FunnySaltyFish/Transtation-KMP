package com.funny.translation.translate

import com.funny.translation.BaseActivity
import com.funny.translation.kmp.NavController
import com.funny.translation.translate.utils.UpdateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

actual class TransActivity : BaseActivity() {
    actual var navController: NavController? = null
    private var initialized = false

    val activityViewModel: ActivityViewModel by lazy {
        ActivityViewModel()
    }

    override fun onShow() {
        super.onShow()
        activityViewModel.refreshUserInfo()
        if (!initialized) {
            CoroutineScope(Dispatchers.IO).launch {
                UpdateUtils.checkUpdate()
                initialized = true
            }
        }
    }
}