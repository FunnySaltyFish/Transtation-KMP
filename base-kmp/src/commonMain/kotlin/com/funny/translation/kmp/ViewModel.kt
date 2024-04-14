package com.funny.translation.kmp

import androidx.compose.runtime.Composable
import com.funny.translation.helper.Log
import moe.tlaster.precompose.viewmodel.ViewModel

// 为了方便的使用 PreCompose 提供的 ViewModel
@Composable
inline fun <reified T: ViewModel> viewModel(): T {
    return moe.tlaster.precompose.viewmodel.viewModel(listOf()) {
        Log.d("ViewModel", "create ${T::class.java.simpleName}")
        T::class.java.getConstructor().newInstance()
    }
}