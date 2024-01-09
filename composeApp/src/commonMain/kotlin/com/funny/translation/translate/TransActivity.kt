package com.funny.translation.translate

import com.funny.translation.BaseActivity
import com.funny.translation.kmp.NavController

expect class TransActivity: BaseActivity {
    var navController: NavController
}