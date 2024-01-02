package com.funny.translation.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.funny.translation.kmp.base.strings.ResStrings
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun Permission(
    permission: String,
    description: String,
    content: @Composable () -> Unit
) {
    val permissionState = rememberPermissionState(permission)
    if (permissionState.status.isGranted){
        content()
    } else {
        Rationale(text = description) {
            permissionState.launchPermissionRequest()
        }
    }
}

@Composable
private fun Rationale(
    text: String,
    onRequestPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Don't */ },
        title = {
            Text(text = ResStrings.request_permission)
        },
        text = {
            Text(text)
        },
        confirmButton = {
            Button(onClick = onRequestPermission) {
                Text(ResStrings.confirm)
            }
        }
    )
}