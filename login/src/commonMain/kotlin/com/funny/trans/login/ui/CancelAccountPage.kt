@file:OptIn(ExperimentalMaterial3Api::class)

package com.funny.trans.login.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.translation.AppConfig
import com.funny.translation.kmp.LocalKMPContext
import com.funny.translation.kmp.NavController
import com.funny.translation.kmp.NavHostController
import com.funny.translation.kmp.viewModel
import com.funny.translation.login.strings.ResStrings
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.MarkdownText

@Composable
fun CancelAccountPage(
    navController: NavHostController
) {
    CommonPage {
        val vm = viewModel<LoginViewModel>()
        val context = LocalKMPContext.current

        SideEffect {
            AppConfig.userInfo.value.takeIf { it.isValid() }?.let {
                vm.email = it.email
                vm.username = it.username
            }
        }

        TipDialog(navController)

        Spacer(modifier = Modifier.height(60.dp))
        Column(Modifier.fillMaxWidth(WIDTH_FRACTION)) {
            InputUsername(
                usernameProvider = vm::username,
                updateUsername = vm::updateUsername,
                isValidUsernameProvider = vm::isValidUsername
            )
            Spacer(modifier = Modifier.height(8.dp))
            InputEmail(
                modifier = Modifier.fillMaxWidth(),
                value = vm.email,
                onValueChange = { vm.email = it },
                isError = vm.email != "" && !vm.isValidEmail,
                verifyCode = vm.verifyCode,
                onVerifyCodeChange = { vm.verifyCode = it },
                initialSent = false,
                onClick = { vm.sendCancelAccountEmail(context) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            val enable by remember {
                derivedStateOf {
                    vm.isValidUsername && vm.isValidEmail && vm.verifyCode.length == 6
                }
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                vm.cancelAccount {
                    AppConfig.logout()
                    navController.popBackStack() // 账号详情
                    navController.popBackStack() // 主页
                }
            }, enabled = enable) {
                Text(text = ResStrings.confirm_cancel)
            }
        }
    }
}

@Composable
fun TipDialog(navController: NavController) {
    var showTipDialog by remember { mutableStateOf(true) }
    if (showTipDialog) {
        AlertDialog(
            onDismissRequest = {  },
            title = { Text(ResStrings.warning) },
            text = { MarkdownText(ResStrings.cancel_account_tip) },
            confirmButton = {
                CountDownTimeButton(
                    modifier = Modifier,
                    onClick = { showTipDialog = false },
                    text = ResStrings.confirm,
                    initialSent = true,
                    countDownTime = 10
                )
            },
            dismissButton = {
                TextButton(onClick = {
                    showTipDialog = false
                    navController.popBackStack()
                }) {
                    Text(ResStrings.cancel)
                }
            }
        )
    }
}