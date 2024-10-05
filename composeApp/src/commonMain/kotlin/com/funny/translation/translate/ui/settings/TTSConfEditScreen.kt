package com.funny.translation.translate.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.compose.loading.loadingList
import com.funny.data_saver.core.getLocalDataSaverInterface
import com.funny.translation.bean.Price
import com.funny.translation.bean.showWithUnit
import com.funny.translation.helper.rememberDerivedStateOf
import com.funny.translation.kmp.viewModel
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.tts.DashScopeProvider
import com.funny.translation.translate.tts.Gender
import com.funny.translation.translate.tts.OpenAIProvider
import com.funny.translation.translate.tts.Speaker
import com.funny.translation.translate.tts.TTSConf
import com.funny.translation.translate.tts.TTSProvider
import com.funny.translation.translate.ui.widget.HintText
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.navPaddingItem
import com.funny.translation.ui.slideIn
import kotlinx.coroutines.launch

@Composable
fun TTSConfEditScreen(
    conf: TTSConf
) {
    val vm: TTSConfEditViewModel = viewModel {
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
            ConfListPager(vm)
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

// TabRow + Pager 改造主体页面
private const val KEY_SELECTED_TAB = "TTSConfEditScreen_selectedTab"

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ColumnScope.ConfListPager(
    vm: TTSConfEditViewModel
) {
    // TabRow + Pager
    val dataSaver = getLocalDataSaverInterface()

    val map = vm.providerListMap
    val providers = vm.filteredTTSProviders

    val pagerState = rememberPagerState(
        initialPage = dataSaver.readData(KEY_SELECTED_TAB, 0),
        pageCount = providers::size
    )
    val scope = rememberCoroutineScope()
    fun changePage(index: Int) = scope.launch {
        pagerState.animateScrollToPage(index)
    }

    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
//        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 0.dp
//        indicator = { tabPositions ->
//            SecondaryIndicator(
//                modifier = Modifier.height(2.dp),
//                color = MaterialTheme.colorScheme.primary
//            )
//        }
    ) {
        providers.forEachIndexed { index, provider ->
            Tab(
                text = { Text(provider.name) },
                selected = pagerState.currentPage == index,
                onClick = {
                    changePage(index)
                    dataSaver.saveData(KEY_SELECTED_TAB, index)
                }
            )
        }
    }

    // Pager
    HorizontalPager(
        modifier = Modifier.fillMaxSize(),
        state = pagerState,
        contentPadding = PaddingValues(top = 4.dp),
        pageSpacing = 4.dp,
        verticalAlignment = Alignment.Top
    ) { page: Int ->
        val provider = providers[page]
        val listState = map[provider]!!
        LazyColumn {
            loadingList(
                listState,
                retry = { vm.loadProviderList(provider) },
                key = { it.fullName + "_" + it.locale },
                successHeader = {
                    if (provider.price1kChars > Price.ZERO) {
                        HintText(
                            text = ResStrings.price_1k_chars.plus(provider.price1kChars.showWithUnit()),
                        )
                    }
                },
                successFooter = {
                    if (provider is OpenAIProvider || provider is DashScopeProvider) {
                        HintText(
                            text = ResStrings.tts_generated_by_ai_note,
                        )
                    }
                }
            ) { speaker ->
                ConfItem(vm, speaker, provider)
            }
            navPaddingItem()
        }

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
    val selected by rememberDerivedStateOf { vm.gender.contains(gender) }
    FilterChip(
        selected = selected,
        onClick = {
            vm.updateGenderChecked(newValue = gender, checked = !selected)
        },
        label = {
            Text(gender.displayName)
        },
        leadingIcon = {
            val icon = if (gender == Gender.Male) Icons.Default.Male else Icons.Default.Female
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.ConfItem(
    vm: TTSConfEditViewModel,
    speaker: Speaker,
    provider: TTSProvider
) {
    Column(
        modifier = Modifier.padding(horizontal = 0.dp, vertical = 4.dp)
            .background(MaterialTheme.colorScheme.primaryContainer , RoundedCornerShape(4.dp))
            .slideIn()
            .animateItemPlacement()
    ) {
        val selected = vm.speaker == speaker
        val onClick = {
            if (!vm.speaking) {
                vm.updateSpeakerChecked(provider, speaker)
            }
        }
        ListItem(
            modifier = Modifier
                .clickable(onClick = onClick),
            headlineContent = {
                Text(speaker.shortName)
            },
            trailingContent = {
                RadioButton(
                    selected = selected,
                    onClick = onClick,
                    enabled = !vm.speaking,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,

                    )
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
        AnimatedVisibility(
            visible = selected,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column {
                with(provider) {
                    Settings(
                        conf = vm.conf,
                        onSettingSpeedFinish = {
                            vm.updateSpeed(it.toInt())
                        },
                        onSettingVolumeFinish = {
                            vm.updateVolume(it.toInt())
                        }
                    )
                }
            }
        }
    }

}