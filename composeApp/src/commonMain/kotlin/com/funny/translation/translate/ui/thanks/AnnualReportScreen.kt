package com.funny.translation.translate.ui.thanks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.cmaterialcolors.MaterialColors
import com.funny.compose.loading.LoadingContent
import com.funny.compose.loading.LoadingState
import com.funny.translation.AppConfig
import com.funny.translation.helper.Log
import com.funny.translation.kmp.currentPlatform
import com.funny.translation.kmp.rememberSystemUiController
import com.funny.translation.kmp.viewModel
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.network.service.LLMAnalyzeResult
import com.funny.translation.translate.ui.widget.AutoFadeInComposableColumn
import com.funny.translation.translate.ui.widget.FadeInColumnScope
import com.funny.translation.translate.ui.widget.rememberAutoFadeInColumnState
import com.funny.translation.ui.AutoIncreaseAnimatedNumber
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.TransparentTopBar
import com.funny.translation.ui.animatedGradientBackground
import java.util.Date
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.DurationUnit

private const val TAG = "AnnualReportScreen"

@Composable
fun AnnualReportScreen() {
    val vm : AnnualReportViewModel = viewModel()
    val systemUiController = rememberSystemUiController()

    DisposableEffect(key1 = systemUiController){
        systemUiController.isSystemBarsVisible = false
        onDispose {
            systemUiController.isSystemBarsVisible = true
        }
    }

    LoadingContent(
        loader = vm::loadAnnualReport,
        initialValue = vm.loadingState,
        retry = { vm.loadingState = LoadingState.Loading },
        failure = { err, _ ->
            AutoFadeInComposableColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .animatedGradientBackground(MaterialColors.DeepPurple800, Color.Black)
            ) {
                LabelText(
                    text = ResStrings.no_annual_report,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                        .padding(24.dp)
                )
            }
        }
    ) {
        if (currentPlatform.isDesktop) {
            CommonPage(
                modifier = Modifier.animatedGradientBackground(
                    MaterialColors.DeepPurple800, Color.Black
                ),
                topBar = {
                    TransparentTopBar(title = ResStrings.annual_report, titleColor = Color.White)
                }
            ) {
                AnnualReport(vm = vm)
            }
        } else {
            AnnualReport(modifier = Modifier.animatedGradientBackground(
                MaterialColors.DeepPurple800, Color.Black
            ), vm = vm)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnnualReport(
    modifier: Modifier = Modifier,
    vm : AnnualReportViewModel
) {
    val state = rememberPagerState {
        12
    }
    VerticalPager(state = state, beyondViewportPageCount = 0, modifier = modifier
        .fillMaxSize()
    ) { page ->
        /**
         *
         * 总共六页
         * 第一页：你的 2024年度报告生成完成，耗时 {vm.loadingTime}
         *        点击查看
         * 第二页：2024年你一共翻译了 ${vm.totalTranslateTimes} 个字
         *        在 ${vm.mostDate} 最多
         *        翻译了 ${vm.mostTranslateTimes} 个字
         * 第三页：有些时刻，你可能已经遗忘，但我们还记得
         *
         *        ${vm.earliestTime}，你打开译站，开始了翻译
         *        这是你最早的一天，还记得是做什么吗？
         *        ${vm.latestTime}，你仍然没有放下译站
         *        这是你最晚的一天，记得早些休息
         * 第四页：你最常用的源语言是
         *       ${vm.mostCommonSourceLanguage}
         *       使用了 ${vm.mostCommonSourceLanguageTimes} 次
         *
         *       你最常用的目标语言是
         *       ${vm.mostCommonTargetLanguage}
         *       使用了 ${vm.mostCommonTargetLanguageTimes} 次
         * 第五页：译站的一大特点就是多引擎翻译
         *        你一共使用过 ${vm.enginesUsesList.size} 个引擎
         *        你最常用的引擎是
         *        ${vm.enginesUsesList[0].first}
         *        使用了 ${vm.enginesUsesList[0].second} 次
         *        其余引擎使用情况如下
         * 新一页：这一年，译站在大模型持续发力
         *        先后支持25+大模型翻译，
         *        上线对话翻译、长文翻译、
         *        AI图片后处理、AI朗读
         *        等多项功能
         *        我们相信，大模型是翻译的未来
         *        也欢迎你的尝试
         * 新一页：
         *        自 2024-07-17 统计起
         *        你使用付费大模型产生共 {vm.llmTotalTimes} 次消耗
         *        （开启智能翻译后，单次翻译产生两笔消耗）
         *        使用AI朗读过 {vm.llmTotalReadTimes} 次
         *        全部共计
         *        输入Token：{vm.llmTotalInputToken}
         *        输出Token：{vm.llmTotalOutputToken}
         *        有哪个模型的翻译惊艳到你吗
         * 新一页：
         *        来看看译站的整体数据吧
         *        这一年，最常被使用的是 百度翻译，
         *        共翻译了 138598 次，
         *        超 1000万字
         *        它在 2024-02-06 表现特别突出，翻译了 892 次
         *
         *        译站年度总共翻译了 535879 次
         *        日均约 1480 次
         *
         *        年度总翻译字数: 4194万
         *        日均约 11万8千
         * 新一页：
         *       2024年，
         *       大模型共翻译超 674万 字
         *       其中
         *       模型一、GLM-4-Plus
         *       通义千问-Max、GPT-4o
         *       最受大家青睐
         *       译站一直在不断上新，欢迎多多尝试哦
         * 新一页：2020年1月1日，译站迎来了第一次代码提交
         *        到今天，已经过了
         *        ${} 天
         *        在这期间，译站提交了代码超 500 次
         *        发布了 65 个版本
         *        桌面版、Google Play版本已在路上
         *
         * 新一页： 目前，译站App代码量超 40000 行
         *        应用下载量超 30000 次（包含应用内更新）
         *        注册用户 2100+

         *        译站的未来，我们一起期待
         *        感谢一路支持
         *
         *        @译站 2024年度报告
         *
         */
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            when (page) {
                0 -> AnnualReportPart1(loadingDuration = vm.loadingDuration, loadLatest = vm.shouldLoadLatest)
                1 -> AnnualReportPart2(totalTranslateTimes = vm.totalTranslateTimes, totalTranslateWords = vm.totalTranslateWords)
                2 -> AnnualReportPart3(earliestTime = vm.earliestTime, latestTime = vm.latestTime)
                3 -> AnnualReportPart4(mostCommonSourceLanguage = vm.mostCommonSourceLanguage, mostCommonSourceLanguageTimes = vm.mostCommonSourceLanguageTimes, mostCommonTargetLanguage = vm.mostCommonTargetLanguage, mostCommonTargetLanguageTimes = vm.mostCommonTargetLanguageTimes)
                4 -> AnnualReportPart5(engineUsedList = vm.enginesUsesList)
                5 -> AnnualReportPart6()
                6 -> AnnualReportPart7(llmAnalyzeResult = vm.llmAnalyzeResult)
                7 -> AnnualReportPart8()
                8 -> AnnualReportPart8_2()
                9 -> AnnualReportPart9()
                10 -> AnnualReportPart10()
                11 -> AnnualReportPart11()
            }
        }
    }
}

@Composable
fun AnnualReportPart1(
    loadingDuration: Duration,
    loadLatest: Boolean
) {
    AutoFadeInComposableColumn(
        Modifier
            .fillMaxWidth()
            .padding(24.dp)) {
        TitleText("译站 2024\n年度报告")
        Spacer(height = 8.dp)
        LabelText(text = "你的年度报告加载完成\n耗时 ${loadingDuration.toString(DurationUnit.MILLISECONDS, decimals = 0)}")
        Spacer(height = 48.dp)
        TipText(text = "下滑开启")
        TipText(text = if (loadLatest) "*2024你似乎还不认识译站，已自动切换数据到至今" else "*统计数据开始于2024/01/01 仅本地数据")
    }
}

@Composable
fun AnnualReportPart2(
    totalTranslateTimes: Int,
    totalTranslateWords: Int
) {
    val state = rememberAutoFadeInColumnState()
    AutoFadeInComposableColumn(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        state = state
    ) {
        LabelText(text = "在这一年，你一共翻译了")

        Row(modifier = Modifier) {
            AnimatedNumber(
                startAnim = state.currentFadeIndex == 1,
                number = totalTranslateTimes,
            )

            ResultText(text = "次", modifier = Modifier.align(Alignment.CenterVertically))
        }
        LabelText(text = "总共")
        Row(modifier = Modifier) {
            AnimatedNumber(
                startAnim = state.currentFadeIndex == 3,
                number = totalTranslateWords,
            )

            ResultText(text = "字", modifier = Modifier.align(Alignment.CenterVertically))
        }
        LabelText(text = "相当于 %.2f 篇 800 字的高考作文".format(totalTranslateWords / 800.0))
    }
}

@Composable
fun AnnualReportPart3(
    earliestTime :Long,
    latestTime : Long
) {
    // 时间戳转化为 xx年xx月xx日 xx:xx:xx
    fun formatTime(time: Long): String {
        val formatter = java.text.SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINESE)
        return formatter.format(Date(time))
    }

    AutoFadeInComposableColumn(
        Modifier
            .fillMaxSize()
            .padding(24.dp)) {
        TipText(text = "有些时刻，你可能已经遗忘，但我们还记得")
        Spacer(height = 8.dp)

        ResultText(text = formatTime(earliestTime))
        LabelText(text = "你打开译站，开始了翻译")
        Spacer(height = 8.dp)
        TipText(text = "这是你最早的一天，还记得是做什么吗？")

        Spacer(height = 100.dp)

        ResultText(text = formatTime(latestTime))
        LabelText(text = "你仍然没有放下译站")
        Spacer(height = 8.dp)
        TipText(text = "这是你最晚的一天，记得早些休息")
    }
}

@Composable
fun AnnualReportPart4(
    mostCommonSourceLanguage: String,
    mostCommonSourceLanguageTimes: Int,
    mostCommonTargetLanguage: String,
    mostCommonTargetLanguageTimes: Int
) {
    val state = rememberAutoFadeInColumnState()
    LaunchedEffect(key1 = state.currentFadeIndex){
        Log.d(TAG, "AnnualReportPart4: ${state.currentFadeIndex}")
    }
    AutoFadeInComposableColumn(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        state = state
    ) {
        TipText(text = "你最常用的源语言是")
        Spacer(height = 8.dp)
        ResultText(text = mostCommonSourceLanguage)
        Spacer(height = 18.dp)
        TipText(text = "使用了")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = mostCommonSourceLanguageTimes, startAnim = state.currentFadeIndex == 5)
            ResultText(text = "次")
        }

        Spacer(height = 48.dp)
        TipText(text = "你最常用的目标语言是")
        Spacer(height = 8.dp)
        ResultText(text = mostCommonTargetLanguage)
        Spacer(height = 18.dp)
        TipText(text = "使用了")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = mostCommonTargetLanguageTimes, startAnim = state.currentFadeIndex == 12)
            ResultText(text = "次")
        }

        Spacer(height = 48.dp)
        TipText(text = "你的刻苦，相信能被看见")
    }
}

@Composable
fun AnnualReportPart5(
    engineUsedList: List<Pair<String, Int>>
){
    val state = rememberAutoFadeInColumnState()
    AutoFadeInComposableColumn(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        state = state
    ) {
        TipText(text = "译站的一大特点就是多引擎翻译")
        Spacer(height = 8.dp)
        TipText(text = "你一共使用过")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = engineUsedList.size, startAnim = state.currentFadeIndex == 3)
            ResultText(text = "个引擎")
        }
        if (engineUsedList.isEmpty()){
            Spacer(height = 48.dp)
            TipText(text = "很遗憾，之后再体验吧")
        } else {
            Spacer(height = 24.dp)
            TipText(text = "你最常用的引擎是")
            Spacer(height = 8.dp)
            ResultText(text = engineUsedList[0].first)
            Spacer(height = 18.dp)
            TipText(text = "使用了")
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedNumber(
                    number = engineUsedList[0].second,
                    startAnim = state.currentFadeIndex == 10
                )
                ResultText(text = "次")
            }
            Spacer(height = 48.dp)
            TipText(text = "其余引擎使用情况如下")
            Spacer(height = 8.dp)
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .heightIn(0.dp, 200.dp)) {
                itemsIndexed(engineUsedList.subList(1, engineUsedList.size)) { _, it ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = it.first, color = Color.White.copy(0.8f))
                        Text(text = "${it.second}次", color = Color.White.copy(0.8f))
                    }
                }
            }
        }

        Spacer(height = 24.dp)
        TipText(text = "更多彩的功能，仍在未来等待")

    }
}

@Composable
fun AnnualReportPart6() {
    val state = rememberAutoFadeInColumnState()
    AutoFadeInComposableColumn(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        state = state
    ) {
        LabelText(text = "这一年，译站在大模型持续发力")
        Spacer(height = 8.dp)
        LabelText(text = "先后支持")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 25, startAnim = state.currentFadeIndex == 3)
            ResultText(text = "+大模型翻译")
        }
        Spacer(height = 24.dp)
        LabelText(text = "上线")
        ResultText(text = "对话翻译、长文翻译、")
        Spacer(height = 8.dp)
        ResultText(text = "AI图片后处理、AI朗读")
        Spacer(height = 2.dp)
        LabelText(text = "等多项功能")
        Spacer(height = 40.dp)
        TipText(text = "我们相信，大模型是翻译的未来")
        Spacer(height = 8.dp)
        TipText(text = "也欢迎你的尝试")
    }
}

@Composable
fun AnnualReportPart7(
    llmAnalyzeResult: LLMAnalyzeResult
) {
    val state = rememberAutoFadeInColumnState()
    AutoFadeInComposableColumn(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        state = state
    ) {
        if (llmAnalyzeResult.isAllZero()) {
            if (AppConfig.uid > 0) {
                TipText(text = "你还没有使用付费大模型")
                Spacer(height = 8.dp)
                TipText(text = "他们能力出众，欢迎体验体验~")
            } else {
                TipText(text = "你可以登录后，在这里看到付费大模型使用情况哦~")
            }
        } else {
            TipText(text = "自 2024-07-17 统计起")
            Spacer(height = 8.dp)
            TipText(text = "你使用付费大模型产生共")
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedNumber(
                    number = llmAnalyzeResult.llmTotalTimes,
                    startAnim = state.currentFadeIndex == 3
                )
                ResultText(text = "次消耗")
            }
            Spacer(height = 2.dp)
            TipText(text = "（开启智能翻译后，单次翻译产生两笔消耗）")
            Spacer(height = 18.dp)
            TipText(text = "使用AI朗读过")
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedNumber(
                    number = llmAnalyzeResult.llmTotalReadTimes,
                    startAnim = state.currentFadeIndex == 8
                )
                ResultText(text = "次")
            }
            Spacer(height = 18.dp)
            TipText(text = "全部共计")
            Spacer(height = 8.dp)
            TipText(text = "输入Token：")
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedNumber(
                    number = llmAnalyzeResult.llmTotalInputToken,
                    startAnim = state.currentFadeIndex == 13
                )
            }
            Spacer(height = 8.dp)
            TipText(text = "输出Token：")
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedNumber(
                    number = llmAnalyzeResult.llmTotalOutputToken,
                    startAnim = state.currentFadeIndex == 16
                )
            }
            Spacer(height = 18.dp)
            TipText(text = "有哪个模型的翻译惊艳到你吗")
        }
    }
}

@Composable
fun AnnualReportPart8() {
    val state = rememberAutoFadeInColumnState()
    AutoFadeInComposableColumn(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        state = state
    ) {
        TipText(text = "来看看译站的整体数据吧")
        Spacer(height = 8.dp)
        TipText(text = "这一年，最常被使用的是")
        Spacer(height = 8.dp)
        ResultText(text = "百度翻译")
        Spacer(height = 8.dp)
        TipText(text = "共翻译了")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 138598, startAnim = state.currentFadeIndex == 7)
            ResultText(text = "次")
        }
        Spacer(height = 8.dp)
        TipText(text = "超")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 10000000, startAnim = state.currentFadeIndex == 10)
            ResultText(text = "字")
        }
        Spacer(height = 8.dp)
        TipText(text = "它在")
        Row(verticalAlignment = Alignment.CenterVertically) {
            ResultText(text = "2024-02-06")
        }
        Spacer(height = 4.dp)
        TipText(text = "表现特别突出，翻译了")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 892, startAnim = state.currentFadeIndex == 15)
            ResultText(text = "次")
        }
        Spacer(height = 8.dp)
        TipText(text = "虽然相比其他软件不多，\n但对于译站，也是一点点累计起来的啦")
    }
}

@Composable
fun AnnualReportPart8_2() {
    val state = rememberAutoFadeInColumnState()
    AutoFadeInComposableColumn(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        state = state
    ) {
        TipText(text = "译站年度总共翻译了")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 535879, startAnim = state.currentFadeIndex == 1)
            ResultText(text = "次")
        }
        Spacer(height = 8.dp)
        TipText(text = "日均约")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 1480, startAnim = state.currentFadeIndex == 4)
            ResultText(text = "次")
        }
        Spacer(height = 8.dp)
        TipText(text = "年度总翻译字数:")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 41940000, startAnim = state.currentFadeIndex == 7)
            ResultText(text = "字")
        }
        Spacer(height = 8.dp)
        TipText(text = "日均约")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 118000, startAnim = state.currentFadeIndex == 10)
            ResultText(text = "字")
        }
        Spacer(height = 8.dp)
        TipText(text = "这里面，也有你的贡献哦~")
    }
}


@Composable
fun AnnualReportPart9() {
    val state = rememberAutoFadeInColumnState()
    AutoFadeInComposableColumn(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        state = state
    ) {
        TipText(text = "2024年，")
        Spacer(height = 8.dp)
        TipText(text = "大模型共翻译超")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 6740000, startAnim = state.currentFadeIndex == 3)
            ResultText(text = "字")
        }
        Spacer(height = 8.dp)
        TipText(text = "其中")
        Spacer(height = 8.dp)
        ResultText(text = "模型一、GLM-4-Plus")
        Spacer(height = 8.dp)
        ResultText(text = "通义千问-Max、GPT-4o")
        Spacer(height = 8.dp)
        TipText(text = "最受大家青睐")
        Spacer(height = 24.dp)
        TipText(text = "译站一直在不断上新，欢迎多多尝试哦")
    }
}

@Composable
fun AnnualReportPart10() {
    val state = rememberAutoFadeInColumnState()
    AutoFadeInComposableColumn(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        state = state
    ) {
        TipText(text = "2020年1月1日，译站迎来了第一次代码提交")
        Spacer(height = 8.dp)
        TipText(text = "到今天，已经过了")
        Spacer(height = 2.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(
                number = ((System.currentTimeMillis() - 1577808000000) / 86400000).toInt(),
                startAnim = state.currentFadeIndex == 4,
                textSize = 32.sp
            )
            ResultText(text = "天")
        }
        Spacer(height = 18.dp)
        TipText(text = "在这期间，译站提交了代码达")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 500, startAnim = state.currentFadeIndex == 7, textSize = 32.sp)
            ResultText(text = "次")
        }
        Spacer(height = 18.dp)
        TipText(text = "发布了")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 65, startAnim = state.currentFadeIndex == 10, textSize = 32.sp)
            ResultText(text = "个版本")
        }
//        桌面版、Google Play版本已在路上
        Spacer(height = 18.dp)
        TipText(text = "桌面版、Google Play版本已在路上")
    }
}

@Composable
fun AnnualReportPart11() {
    val state = rememberAutoFadeInColumnState()
    AutoFadeInComposableColumn(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        state = state
    ) {
        TipText(text = "目前，译站App代码量超")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 40000, startAnim = state.currentFadeIndex == 1, textSize = 32.sp)
            ResultText(text = "行")
        }
        Spacer(height = 8.dp)
        TipText(text = "应用下载量约")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 30000, startAnim = state.currentFadeIndex == 4, textSize = 32.sp)
            ResultText(text = "次")
        }
        Spacer(height = 8.dp)
        TipText(text = "注册用户")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 2100, startAnim = state.currentFadeIndex == 7, textSize = 32.sp)
            ResultText(text = "+人")
        }

        Spacer(height = 18.dp)
        TipText(text = "译站的未来，我们一起期待")
        Spacer(height = 8.dp)
        TipText(text = "感谢一路支持")
        Spacer(height = 24.dp)

        val placeholder = AppConfig.userInfo.value.takeIf { it.isValid() }?.username?.run { "$this "} ?: ""
        TipText(text = "@${placeholder}译站 2024年度报告")
    }
}

@Composable
private fun LabelText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = modifier, color = Color.White.copy(alpha = 0.8f))
}

@Composable
private fun ResultText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, modifier = modifier, color = Color.White)
}


@Composable
private fun TipText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = modifier, color = Color.White.copy(alpha = 0.6f))
}

@Composable
private fun TitleText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = modifier, color = Color.White)
}

@Composable
private fun FadeInColumnScope.AnimatedNumber(
    startAnim: Boolean,
    number: Int,
    textSize: TextUnit = 48.sp,
) {
    AutoIncreaseAnimatedNumber(
        modifier = Modifier,
        startAnim = startAnim,
        number = number,
        durationMills = 1000,
        textSize = textSize,
        textColor = Color.White,
        textWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun FadeInColumnScope.Spacer(
    whetherFade: Boolean = false,
    height: Dp
) {
    Spacer(modifier = Modifier
        .fadeIn(whetherFade)
        .height(height))
}


