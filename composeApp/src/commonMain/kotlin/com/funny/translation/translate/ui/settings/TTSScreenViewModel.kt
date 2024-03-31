package com.funny.translation.translate.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.funny.translation.database.executeAsFlowList
import com.funny.translation.helper.BaseViewModel
import com.funny.translation.translate.Language
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.enabledLanguages
import com.funny.translation.translate.tts.TTSConf
import kotlinx.coroutines.flow.collectLatest

class TTSScreenViewModel: BaseViewModel() {
    val confListFlow = appDB.tTSConfQueries.getAll().executeAsFlowList()

    // 暂时没有被添加 conf 的语言列表
    var noConfLangList by mutableStateOf<List<Language>>(emptyList())

    fun delete(conf: TTSConf) {
        submit {
            appDB.tTSConfQueries.deleteById(conf.id)
        }
    }

    fun applyToConfs(origin: TTSConf, targets: List<TTSConf>) {
        submit {
            appDB.transaction {
                targets.forEach {
                    appDB.tTSConfQueries.updateById(
                        id = it.id,
                        ttsProviderId = origin.ttsProviderId,
                        speaker = origin.speaker,
                        extraConf = origin.extraConf,
                    )
                }
            }
        }
    }

    suspend fun init() {
        confListFlow.collectLatest {  confList ->
            noConfLangList = enabledLanguages.value - confList.map { it.language }.toSet()
        }
    }

}