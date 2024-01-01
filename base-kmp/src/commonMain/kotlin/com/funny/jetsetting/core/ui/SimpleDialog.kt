package com.funny.jetsetting.core.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.funny.translation.kmp.base.strings.ResStrings

@Composable
fun SimpleDialog(
    openDialog: Boolean,
    updateOpenDialog: (Boolean) -> Unit,
    title: String? = null,
    content: (@Composable () -> Unit)? = null,
    confirmButtonAction: (() -> Unit)? = null,
    confirmButtonText : String = ResStrings.confirm,
    dismissButtonAction: (() -> Unit)? = null,
    dismissButtonText : String = ResStrings.cancel,
    closeable: Boolean = true
) {
    if (openDialog) {
        AlertDialog(
            onDismissRequest = {
                if (closeable) updateOpenDialog(false)
            },
            title = {
                if (title != null) Text(text = title)
            },
            text = content,
            confirmButton = {
                if(confirmButtonText.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            updateOpenDialog(false)
                            confirmButtonAction?.invoke()
                        }) {
                        Text(confirmButtonText)
                    }
                }
            },
            dismissButton = {
                if(dismissButtonText.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            updateOpenDialog(false)
                            dismissButtonAction?.invoke()
                        }) {
                        Text(dismissButtonText)
                    }
                }
            }
        )
    }
}

@Composable
fun SimpleDialog(
    openDialogState: MutableState<Boolean>,
    title: String? = null,
    message: String = "message",
    confirmButtonAction: (() -> Unit)? = {},
    confirmButtonText : String = ResStrings.confirm,
    dismissButtonAction: (() -> Unit)? = {},
    dismissButtonText : String = ResStrings.cancel,
    closeable: Boolean = true
) {
    val (openDialog, updateOpenDialog) = openDialogState
    SimpleDialog(openDialog, updateOpenDialog, title, message, confirmButtonAction, confirmButtonText, dismissButtonAction, dismissButtonText, closeable)
}

@Composable
fun SimpleDialog(
    openDialogState: MutableState<Boolean>,
    title: String? = null,
    content: (@Composable () -> Unit)? = null,
    confirmButtonAction: (() -> Unit)? = null,
    confirmButtonText : String = ResStrings.confirm,
    dismissButtonAction: (() -> Unit)? = null,
    dismissButtonText : String = ResStrings.cancel,
    closeable: Boolean = true
) {
    val (openDialog, updateOpenDialog) = openDialogState
    SimpleDialog(openDialog, updateOpenDialog, title, content, confirmButtonAction, confirmButtonText, dismissButtonAction, dismissButtonText, closeable)

}

@Composable
fun SimpleDialog(
    openDialog: Boolean,
    updateOpenDialog: (Boolean) -> Unit,
    title: String? = null,
    message: String = "message",
    confirmButtonAction: (() -> Unit)? = null,
    confirmButtonText : String = ResStrings.confirm,
    dismissButtonAction: (() -> Unit)? = null,
    dismissButtonText : String = ResStrings.cancel,
    closeable: Boolean = true
) {
    SimpleDialog(openDialog, updateOpenDialog, title, content = { Text(text = message)}, confirmButtonAction, confirmButtonText, dismissButtonAction, dismissButtonText, closeable)
}