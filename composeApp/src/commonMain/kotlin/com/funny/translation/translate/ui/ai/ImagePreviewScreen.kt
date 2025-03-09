package com.funny.translation.translate.ui.ai

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.funny.translation.helper.ExperimentalSharedTransitionApi
import com.funny.translation.helper.LocalSharedTransitionScope
import com.funny.translation.helper.Log
import com.funny.translation.helper.SimpleAction
import com.funny.translation.translate.ui.widget.ZoomableImage
import moe.tlaster.precompose.navigation.BackHandler

const val KEY_SHARED_PREVIEW_IMAGE = "shared_preview_image"
private const val TAG = "ImagePreviewScreen"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ImagePreviewScreen(
    imageUri: String,
    modifier: Modifier = Modifier,
    animatedContentScope: AnimatedVisibilityScope,
    goBackAction: SimpleAction
) {
    SideEffect {
        Log.d(TAG, "ImagePreviewScreen preview: $imageUri")
    }

    Box(modifier.fillMaxSize().background(Color.Black)) {
        BackHandler {
            goBackAction()
        }

        with(LocalSharedTransitionScope.current) {
            ZoomableImage(
                uri = imageUri,
                contentDescription = "image",
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = goBackAction,
                modifier = Modifier.align(Alignment.TopStart).offset(16.dp, 64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}