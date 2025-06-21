package com.funny.translation.translate.ui.thanks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.cmaterialcolors.MaterialColors
import com.funny.compose.loading.loadingList
import com.funny.compose.loading.rememberRetryableLoadingState
import com.funny.trans.login.ui.LoginRoute
import com.funny.trans.login.ui.addLoginRoutes
import com.funny.translation.AppConfig
import com.funny.translation.bean.UserInfoBean
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.LocalContext
import com.funny.translation.helper.UserUtils
import com.funny.translation.helper.formatBraceStyle
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.NavGraphBuilder
import com.funny.translation.kmp.NavHostController
import com.funny.translation.kmp.animateComposable
import com.funny.translation.kmp.navigation
import com.funny.translation.network.api
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.LocalActivityVM
import com.funny.translation.translate.bean.AI_TEXT_POINT
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.translate.utils.QQUtils
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import kotlinx.coroutines.launch

private const val TAG = "UserProfileScreen"

enum class UserProfileScreenRoutes {
    Settings;

    val route:String get() = "user_profile_route_${name.lowercase()}"
}

fun NavGraphBuilder.addUserProfileRoutes(
    navHostController: NavHostController,
    onLoginSuccess: (UserInfoBean) -> Unit
) {
    navigation(UserProfileScreenRoutes.Settings.route, TranslateScreen.UserProfileScreen.route){
        animateComposable(UserProfileScreenRoutes.Settings.route){
            UserProfileSettings(navHostController = navHostController)
        }
        addLoginRoutes(navHostController, onLoginSuccess)
    }
}


@Composable
fun UserProfileSettings(navHostController: NavHostController) {
    val activityVM = LocalActivityVM.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userInfo = AppConfig.userInfo.value
    CommonPage(
        Modifier
            .padding(horizontal = 8.dp),
        title = ResStrings.user_profile + "(${userInfo.username})"
    ) {

        UserAvatarTile()
        Tile(text = ResStrings.change_username, onClick = {
            navHostController.navigate(LoginRoute.ChangeUsernamePage.route)
        })
        Tile(text = ResStrings.modify_password, onClick = {
            navHostController.navigate(LoginRoute.ResetPasswordPage.route)
        })
        Tile(text = ResStrings.img_remaining_points) {
            Text(text = userInfo.img_remain_points.toString())
        }
        // 剩余 AI 文字点数
        Tile(text = ResStrings.ai_remaining_text_points, onClick = {
            navHostController.navigate(
                TranslateScreen.BuyAIPointScreen.route.formatBraceStyle(
                    "planName" to AI_TEXT_POINT
                )
            )
        }) {
            Text(text = "%.3f".format(userInfo.ai_text_point))
        }
        // AI点数消耗
        Tile(text = com.funny.translation.login.strings.ResStrings.ai_cost_title, onClick = {
            navHostController.navigate(LoginRoute.AIPointCostPage.route)
        })
        Tile(text = ResStrings.vip_end_time) {
            Text(text = userInfo.vipEndTimeStr())
        }
        HorizontalDivider()
        // 生成邀请码
        Tile(text = ResStrings.invite_code, onClick = {
            if (userInfo.invite_code.isBlank()) {
                scope.launch {
                    api(UserUtils.userService::generateInviteCode) {
                        addSuccess {
                            AppConfig.userInfo.value =
                                userInfo.copy(invite_code = it.data ?: "")
                        }
                    }
                }
            } else {
                val txt = ResStrings.invite_user_content.format(userInfo.invite_code)
                ClipBoardUtil.copy(txt)
                context.toastOnUi(ResStrings.copied_to_clipboard)
            }
        }) {
            Text(userInfo.invite_code.ifEmpty { ResStrings.click_to_generate })
        }
        // 查询被邀请人
        val (showInviteUserDialog, update) = remember { mutableStateOf(false) }
        Tile(text = ResStrings.invited_users, onClick = {
            update(true)
        }) {
            InvitedUserAlertDialog(show = showInviteUserDialog, updateShow = update)
        }
        HorizontalDivider()
        Tile(text = ResStrings.disable_account, onClick = {
            navHostController.navigate(LoginRoute.CancelAccountPage.route)
        })
        HorizontalDivider()
        Spacer(modifier = Modifier.height(40.dp))
        Button(modifier = Modifier.align(CenterHorizontally), onClick = {
            AppConfig.logout()
            navHostController.popBackStack()
        }) {
            Text(text = ResStrings.logout)
        }

        val text = remember {
            buildAnnotatedString {
                append(ResStrings.join_group_tip_p1)
                pushStringAnnotation(
                    tag = "url",
                    annotation = "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D"
                )
                withStyle(style = SpanStyle(color = MaterialColors.BlueA700)) {
                    append(" 857362450 ")
                }
                pop()
                append(ResStrings.join_group_tip_p2)
            }
        }
        ClickableText(
            text = text,
            modifier = Modifier.fillMaxWidth(0.9f),
            style = TextStyle(
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        ) { index ->
            // 根据tag取出annotation并打印
            text.getStringAnnotations(tag = "url", start = index, end = index).firstOrNull()
                ?.let {
                    QQUtils.joinQQGroup(context)
                }
        }
    }

}

@Composable
internal fun Tile(
    text: String,
    onClick: () -> Unit = {},
    endIcon: @Composable () -> Unit = {
        FixedSizeIcon(imageVector = Icons.Default.ArrowRight, "")
    }
) {
    ListItem(
        headlineContent = { Text(text) },
        trailingContent = endIcon,
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun InvitedUserAlertDialog(
    show: Boolean,
    updateShow: (Boolean) -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = { updateShow(false) },
            title = {
                Text(text = ResStrings.invited_users)
            },
            text = {
                val (state, retry) = rememberRetryableLoadingState(loader = ::loadInvitedUsers)
                LazyColumn {
                    loadingList(state, retry, key = { it.uid }, empty = {
                        Text(
                            text = ResStrings.empty_invited_users,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }) { item ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Uid: " + item.uid)
                            Text(text = item.register_time)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { updateShow(false) }) {
                    Text(text = ResStrings.ok)
                }
            },
        )
    }
}

private suspend fun loadInvitedUsers() = api(UserUtils.userService::getInviteUsers) ?: emptyList()

@Composable
expect fun UserAvatarTile()