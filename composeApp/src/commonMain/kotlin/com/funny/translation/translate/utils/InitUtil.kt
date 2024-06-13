package com.funny.translation.translate.utils

import com.funny.translation.debug.Debug
import com.funny.translation.debug.DefaultDebugTarget
import com.funny.translation.sign.SignUtils
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.enabledLanguages
import com.funny.translation.translate.initLanguageDisplay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

expect object InitUtil {
    suspend fun CoroutineScope.initAndroidActivity()
    suspend fun CoroutineScope.initDesktopActivity()
}


internal suspend fun InitUtil.initCommon() {
    Debug.addTarget(DefaultDebugTarget)
    with(AppUpgradeOneTimeJobManager) {
        addJobs()
        executeIfNeeded()
    }

    CoroutineScope(Dispatchers.IO).launch {
        SignUtils.loadJs()
        SortResultUtils.init()
    }

    initTypeConverters()
    initLanguageDisplay()
    EngineManager.loadEngines()
}

private fun addJobs() {
    // 62.1 v2.8.0 开发中第一版，为启用的语言添加默认配置
    AppUpgradeOneTimeJobManager.addJob(62.1f) {
        val languages = enabledLanguages.value
        languages.forEach {
            if (appDB.tTSConfQueries.getByLanguage(it).executeAsOneOrNull() == null) {
                appDB.tTSConfQueries.insert(
                    TTSConfManager.createDefaultConf(it)
                )
            }
        }
    }
}