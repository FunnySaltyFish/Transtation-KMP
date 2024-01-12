package com.funny.trans.login.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.funny.translation.AppConfig
import com.funny.translation.helper.UserUtils
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.LocalKMPContext
import com.funny.translation.kmp.NavController
import com.funny.translation.kmp.viewModel
import com.funny.translation.login.strings.ResStrings
import com.funny.translation.ui.CommonPage

@Composable
fun ResetPasswordPage(
    navController: NavController
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
                onClick = { vm.sendResetPasswordEmail(context) }
            )

            Spacer(modifier = Modifier.height(8.dp))
            InputPassword(
                passwordProvider = vm::password,
                updatePassword = vm::updatePassword,
                labelText = ResStrings.new_password
            )
            // 重复密码
            var repeatPassword by remember { mutableStateOf("") }

            val isRepeatPwdError by remember {
                derivedStateOf { vm.password != repeatPassword }
            }
            Spacer(modifier = Modifier.height(8.dp))
            ConcealableTextField(
                value = repeatPassword,
                onValueChange = { repeatPassword = it },
                modifier = Modifier.fillMaxWidth(),
                isError = isRepeatPwdError,
                label = { Text(ResStrings.repeat_password) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
            val enable by remember {
                derivedStateOf {
                    vm.isValidUsername && vm.isValidEmail && vm.verifyCode.length == 6 && UserUtils.isValidPassword(
                        vm.password
                    ) && vm.password == repeatPassword
                }
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                vm.resetPassword(context, onSuccess = {
                    context.toastOnUi("密码重置成功！")
                    navController.popBackStack()
                })
            }, enabled = enable) {
                Text(text = ResStrings.reset_password)
            }
        }
    }
}