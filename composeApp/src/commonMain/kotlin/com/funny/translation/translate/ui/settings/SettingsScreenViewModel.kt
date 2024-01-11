package com.funny.translation.translate.ui.settings

import com.funny.translation.helper.JsonX
import com.funny.translation.helper.lazyPromise
import com.funny.translation.helper.readAssets
import com.funny.translation.kmp.appCtx
import com.funny.translation.translate.bean.OpenSourceLibraryInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class SettingsScreenViewModel : ViewModel() {
    companion object {
        private const val TAG = "SettingsScreenVM"
    }

    private val openSourceLibraryList by lazyPromise<List<OpenSourceLibraryInfo>>(viewModelScope){
        withContext(Dispatchers.IO) {
            val json = appCtx.readAssets("open_source_libraries.json")
            JsonX.fromJson(json)
        }
    }

    suspend fun loadOpenSourceLibInfo(): List<OpenSourceLibraryInfo> = openSourceLibraryList.await()
}