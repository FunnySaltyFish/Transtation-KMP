package com.funny.translation.translate.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Checkbox
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.compose.loading.loadingList
import com.funny.translation.bean.Price
import com.funny.translation.bean.showWithUnit
import com.funny.translation.helper.rememberDerivedStateOf
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.tts.Gender
import com.funny.translation.translate.tts.OpenAIProvider
import com.funny.translation.translate.tts.Speaker
import com.funny.translation.translate.tts.TTSConf
import com.funny.translation.translate.tts.TTSProvider
import com.funny.translation.translate.ui.widget.HintText
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

    DisposableEffect(vm) {
        vm.queryAllSpeakers()
        onDispose {
            // 退出页面时保存配置
            vm.save()
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
            title = ResStrings.speak_voice
        ) {
            LazyColumn {
                val map = vm.providerListMap
                vm.filteredTTSProviders.forEach { provider: TTSProvider ->
                    expandableStickyRow(
                        expand = provider.expanded,
                        updateExpand = { provider.expanded = it },
                        headlineContent = {
                            Text(
                                text = provider.name,
                                fontWeight = FontWeight.Normal,
                                fontSize = 20.sp
                            )
                        },
                        supportContent = {
                            if (provider.price1kChars > Price.ZERO) {
                                Text(
                                    text = ResStrings.price_1k_chars.plus(provider.price1kChars.showWithUnit()),
                                )
                            }
                        }
                    ) {
                        loadingList(
                            map[provider]!!,
                            retry = { vm.loadProviderList(provider) },
                            key = { it.fullName },
                            successFooter = {
                                if (provider is OpenAIProvider) {
                                    HintText(
                                        text = ResStrings.tts_generated_by_ai_note,
                                    )
                                }
                            }
                        ) { speaker ->
                            ConfItem(vm, speaker, provider)
                        }
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        modifier = Modifier.fillMaxSize().background(Color.LightGray.copy(alpha = 0.8f)),
        visible = vm.speaking,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        CircularProgressIndicator(
            modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
        )
    }
}

@Composable
private fun Category(
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
    Text(gender.displayName)
}

@Composable
private fun ConfItem(
    vm: TTSConfEditViewModel,
    speaker: Speaker,
    provider: TTSProvider
) {
    Column {
        val selected = vm.speaker == speaker
        val onClick = {
            if (!vm.speaking) {
                vm.updateSpeakerChecked(provider, speaker)
            }
        }
        ListItem(
            modifier = Modifier.clickable(onClick = onClick).slideIn(),
            headlineContent = {
                Text(speaker.shortName)
            },
            trailingContent = {
                RadioButton(
                    selected = selected,
                    onClick = onClick,
                    enabled = !vm.speaking
                )
            }
        )
        AnimatedVisibility(
            visible = selected,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            provider.Settings(
                conf = vm.conf,
                onSettingSpeedFinish = {
                    vm.updateSpeed(it.toInt())
                }
            )
        }
    }

}