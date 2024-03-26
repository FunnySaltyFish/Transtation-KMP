package com.funny.translation.translate.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.translation.database.executeAsFlowList
import com.funny.translation.helper.formatBraceStyle
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.tts.TTSConf
import com.funny.translation.translate.tts.findTTSProviderById
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TTSScreen() {
    CommonPage(title = "TTS") {
        val navController = LocalNavController.current
        val confList by appDB.tTSConfQueries.getAll().executeAsFlowList().collectAsState(emptyList())
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
//            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(confList, key = { it.id }) { conf ->
                ConfigItem(
                    conf = conf,
                    editAction = {
                        navController.navigate(TranslateScreen.TTSEditConfScreen.route.formatBraceStyle("id" to it.id))
                    }
                )
            }
        }
    }
}

@Composable
private fun ConfigItem(
    conf: TTSConf,
    editAction: (TTSConf) -> Unit
) {
    val ttsProvider = remember(conf) {
        findTTSProviderById(conf.ttsProviderId)
    }

    ListItem(
        modifier = Modifier.clickable { editAction(conf) },
        headlineContent = {
            Text(conf.language.displayText)
        },
        supportingContent = {
            Text("${ttsProvider.name} - ${conf.speaker.shortName}")
        },
        trailingContent = {
            FixedSizeIcon(imageVector = Icons.Default.Edit, contentDescription = null)
        }
    )
}