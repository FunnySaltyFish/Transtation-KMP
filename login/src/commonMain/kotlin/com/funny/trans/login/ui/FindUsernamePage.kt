package com.funny.trans.login.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.LocalKMPContext
import com.funny.translation.kmp.viewModel
import com.funny.translation.login.strings.ResStrings

@Composable
fun FindUsernamePage() {
    Column(
        Modifier
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val vm = viewModel<LoginViewModel>()
        val context = LocalKMPContext.current

        Spacer(modifier = Modifier.height(60.dp))
        Column(Modifier.fillMaxWidth(WIDTH_FRACTION)) {
            InputEmail(
                modifier = Modifier.fillMaxWidth(),
                value = vm.email,
                onValueChange = { vm.email = it },
                isError = vm.email != "" && !vm.isValidEmail,
                verifyCode = vm.verifyCode,
                onVerifyCodeChange = { vm.verifyCode = it },
                initialSent = false,
                onClick = { vm.sendFindUsernameEmail(context) }
            )


            Spacer(modifier = Modifier.height(8.dp))
            val enable by remember {
                derivedStateOf {
                    vm.isValidEmail && vm.verifyCode.length == 6
                }
            }

            val usernameList = remember {
                mutableStateListOf<String>()
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                vm.findUsername {
                    usernameList.clear()
                    usernameList.addAll(it)
                    context.toastOnUi(ResStrings.find_account_amount.format(it.size.toString()))
                }
            }, enabled = enable) {
                Text(text = ResStrings.query_related_account)
            }

            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(Modifier.fillMaxWidth()) {
                items(usernameList.size) {
                    Text(text = usernameList[it], modifier = Modifier.padding(8.dp), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}