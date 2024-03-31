package com.funny.translation.translate.ui.long_text.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.strings.ResStrings

@Composable
fun RemarkDialog(
    showState: MutableState<Boolean>,
    taskId: String,
    initialRemark: String,
    updateRemarkAction: (taskId: String, remark: String) -> Unit
) {
    // 更改备注
    var remark by rememberStateOf(value = initialRemark)
    SimpleDialog(
        openDialogState = showState,
        title = ResStrings.change_remark,
        content = {
            TextField(
                value = remark,
                onValueChange = { remark = it },
                placeholder = {
                    Text(ResStrings.remark)
                },
                maxLines = 1,
                singleLine = true
            )
        },
        confirmButtonAction = {
            updateRemarkAction(taskId, remark)
        },
        confirmButtonText = com.funny.translation.login.strings.ResStrings.confirm_to_modify,
    )
}