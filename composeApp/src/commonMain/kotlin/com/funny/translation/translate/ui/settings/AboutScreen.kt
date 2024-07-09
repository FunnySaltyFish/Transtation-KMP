package com.funny.translation.translate.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight.Companion.W300
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.cmaterialcolors.MaterialColors
import com.funny.compose.loading.loadingList
import com.funny.compose.loading.rememberRetryableLoadingState
import com.funny.jetsetting.core.JetSettingTile
import com.funny.jetsetting.core.ui.FunnyIcon
import com.funny.jetsetting.core.ui.SettingItemCategory
import com.funny.translation.AppConfig
import com.funny.translation.WebViewActivity
import com.funny.translation.helper.LocalContext
import com.funny.translation.helper.LocalNavController
import com.funny.translation.helper.openUrl
import com.funny.translation.helper.rememberFastClickHandler
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.viewModel
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.BuildConfig
import com.funny.translation.translate.RegionConfig
import com.funny.translation.translate.bean.OpenSourceLibraryInfo
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.translate.ui.widget.ShadowedRoundImage
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.theme.isLight
import com.funny.translation.ui.touchToScale

@Composable
//@Preview
fun AboutScreen() {
    val context = LocalContext.current
    val navController = LocalNavController.current
    CommonPage(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        title = ResStrings.about
    ) {
        LargeImageTile(
            funnyIcon = FunnyIcon(resourceName = "ic_launcher_icon"),
            text = "译站 | Transtation",
            description = "v${AppConfig.versionName}, ${ResStrings.build_type}${BuildConfig.BUILD_TYPE}"
        )
        SettingItemCategory(title = {
            Text(text = ResStrings.developer)
        }) {
            val fastClickHandler = rememberFastClickHandler {
                // 非 Debug 模式下不可开启开发者模式
                if (!BuildConfig.DEBUG) return@rememberFastClickHandler

                AppConfig.developerMode.value = true
                context.toastOnUi(ResStrings.open_developer_mode)
            }
            LargeImageTile(
                modifier = Modifier.clickable(onClick = fastClickHandler),
                text = "FunnySaltyFish",
                description = ResStrings.my_description,
                funnyIcon = FunnyIcon(resourceName = "ic_developer_avatar.jpg")
            )
        }
        SettingItemCategory(title = {
            Text(text = ResStrings.discussion)
        }) {
            JetSettingTile(
                resourceName = "ic_qq",
                text = ResStrings.join_qq_group,
                description = ResStrings.join_qq_group_description
            ) {
                WebViewActivity.start(context, "https://jq.qq.com/?_wv=1027&k=3Bvvfzdu")
            }
            JetSettingTile(
                imageVector = Icons.Default.Email,
                text = ResStrings.contact_developer_via_email,
                description = ResStrings.contact_developer_via_email_description
            ) {
                context.openUrl("mailto://funnysaltyfish@foxmail")
            }
        }
        SettingItemCategory(title = {
            Text(text = ResStrings.source_code)
        }) {
            JetSettingTile(
                resourceName = "ic_github",
                text = ResStrings.source_code,
                description = ResStrings.source_code_description
            ) {
                context.toastOnUi(ResStrings.welcome_star)
                WebViewActivity.start(context, "https://github.com/FunnySaltyFish/Transtation-KMP")
            }
        }
        SettingItemCategory(title = {
            Text(text = ResStrings.more_about)
        }) {
            JetSettingTile(
                resourceName = "ic_open_source_library",
                text = ResStrings.open_source_library
            ) {
                navController.navigate(TranslateScreen.OpenSourceLibScreen.route)
            }
            JetSettingTile(
                resourceName = "ic_privacy",
                text = ResStrings.privacy
            ) {
                WebViewActivity.start(
                    context,
                    "https://api.funnysaltyfish.fun/trans/v1/api/privacy"
                )
            }
            // 用户协议
            JetSettingTile(
                resourceName = "ic_user_agreement",
                text = ResStrings.user_agreement
            ) {
                WebViewActivity.start(
                    context,
                    "https://api.funnysaltyfish.fun/trans/v1/api/user_agreement"
                )
            }
            if (RegionConfig.beianNumber.isNotEmpty()) {
                JetSettingTile(
                    resourceName = "ic_beian",
                    text = ResStrings.beian,
                    description = RegionConfig.beianNumber
                ) {
                    WebViewActivity.start(
                        context,
                        "https://beian.miit.gov.cn/#/Integrated/index"
                    )
                }
            }
        }
    }
}

@Composable
private fun LargeImageTile(
    modifier: Modifier = Modifier,
    funnyIcon: FunnyIcon,
    text: String,
    description: String,
) {
    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(text = text, fontWeight = W500, modifier = Modifier.padding(bottom = 2.dp))
        },
        leadingContent = {
            ShadowedRoundImage(modifier = Modifier.size(64.dp), funnyIcon = funnyIcon)
        },
        supportingContent = {
            Text(text = description)
        }
    )
}

@Composable
fun OpenSourceLibScreen() {
    val vm : SettingsScreenViewModel = viewModel()
    val (state, retry) = rememberRetryableLoadingState(loader = vm::loadOpenSourceLibInfo)
    CommonPage(title = ResStrings.open_source_library) {
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            loadingList<OpenSourceLibraryInfo>(state, retry, key = { it.name }) { info ->
                val color =
                    if (info.author == "FunnySaltyFish" && MaterialTheme.colorScheme.isLight) MaterialColors.Orange200 else MaterialTheme.colorScheme.primaryContainer
                OpenSourceLibItem(
                    modifier = Modifier
                        .touchToScale()
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(12.dp))
                        .background(color)
                        .padding(top = 12.dp, start = 12.dp, end = 12.dp, bottom = 4.dp),
                    info = info
                )
            }
        }
    }
}

@Composable
fun OpenSourceLibItem(
    modifier: Modifier = Modifier,
    info: OpenSourceLibraryInfo
) {
    val context = LocalContext.current
    Column(modifier = modifier){
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = info.name, fontWeight = W700)
            Text(text = info.author, fontWeight = W300, fontSize = 12.sp)
        }
        Row(Modifier.fillMaxSize(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(modifier = Modifier.weight(6f), text = info.description, fontWeight = W500, fontSize = 14.sp)
            IconButton(modifier = Modifier
                .size(48.dp)
                .weight(1f), onClick = {
                WebViewActivity.start(context, info.url)
            }) {
                FixedSizeIcon(Icons.Default.KeyboardArrowRight, contentDescription = ResStrings.browser_url)
            }
        }
    }
}

//@Composable
//@Preview
//fun OpenSourceLibItemPreview() {
//    OpenSourceLibItem(modifier = Modifier
//        .fillMaxWidth()
//        .wrapContentHeight(), info = OpenSourceLibraryInfo(
//        name="ComposeDataSaver",
//        url= "https://github.com/FunnySaltyFish/ComposeDataSaver",
//        description= "在 Jetpack Compose 中优雅完成数据持久化",
//        author= "FunnySaltyFish"
//    ))
//}