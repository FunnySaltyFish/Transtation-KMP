package com.funny.translation.translate.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.helper.rememberDerivedStateOf
import com.funny.translation.kmp.strings.ResStrings
import com.funny.translation.translate.tts.Gender
import com.funny.translation.translate.tts.TTSConf
import com.funny.translation.translate.tts.TTSProvider
import com.funny.translation.translate.tts.ttsProviders
import com.funny.translation.translate.utils.expandableStickyRow
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.slideIn
import moe.tlaster.precompose.viewmodel.viewModel

@Composable
fun TTSConfEditScreen(
    conf: TTSConf
) {
    val vm: TTSConfEditViewModel = viewModel(keys = listOf(conf)) {
        TTSConfEditViewModel(conf)
    }

    val supportedTTSProviders = remember {
        ttsProviders.filter { it.supportLanguages.contains(conf.language) }
    }

    LaunchedEffect(vm) {
        supportedTTSProviders.forEach { ttsProvider ->
            vm.loadProviderList(ttsProvider)
        }
    }

    CommonPage(
        modifier = Modifier.padding(horizontal = 16.dp),
        title = conf.language.displayText
    ) {
        Category(
            title = ResStrings.gender,
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SelectGender(vm, Gender.Male)
                Spacer(modifier = Modifier.width(8.dp))
                SelectGender(vm, Gender.Female)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Category(
            title = "TTS Engines"
        ) {

            LazyColumn {
                val map = vm.providerListMap

                supportedTTSProviders.forEach { provider: TTSProvider ->
                    expandableStickyRow(
                        title = provider.name,
                        expand = provider.expanded,
                        updateExpand = { provider.expanded = it }
                    ) {
                        items(map[provider] ?: emptyList(), key = { it.fullName }) { speaker ->
                            ListItem(
                                modifier = Modifier.clickable {
                                    vm.updateSpeakerChecked(speaker)
                                }.slideIn(),
                                headlineContent = {
                                    Text(speaker.shortName)
                                },
                                trailingContent = {
                                    RadioButton(
                                        selected = conf.speaker == speaker,
                                        onClick = {
                                            vm.updateSpeakerChecked(speaker)
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun Category(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Normal
        )
        content()
    }
}

@Composable
private fun RowScope.SelectGender(
    vm: TTSConfEditViewModel,
    gender: Gender,
) {
    val checked by rememberDerivedStateOf { vm.gender.contains(gender) }
    Checkbox(
        checked = checked,
        onCheckedChange = {
            vm.updateGenderChecked(newValue = gender, checked = it)
        }
    )
    Text(gender.name)
}