package com.funny.translation.translate.ui.ai.componets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.helper.SimpleAction
import com.funny.translation.kmp.currentPlatform
import com.funny.translation.strings.ResStrings
import com.funny.translation.ui.FixedSizeIcon

//private val panels = hashMapOf(


@Composable
internal fun AddFilePanel(
    modifier: Modifier,
    selectImageAction: SimpleAction,
    takePhotoAction: SimpleAction
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier,
    ) {
        // Select Image
        item {
            PanelItem(
                title = ResStrings.image,
                icon = Icons.Default.Image,
                onClick = selectImageAction
            )
        }

        // Take Photo, only available on Android
        if (currentPlatform.isAndroid) {
            item {
                PanelItem(
                    title = ResStrings.take_photo,
                    icon = Icons.Default.AddAPhoto,
                    onClick = takePhotoAction
                )
            }
        }
    }
}

@Composable
private fun PanelItem(
    title: String,
    icon: ImageVector,
    onClick: SimpleAction
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(8.dp)
    ) {
        FixedSizeIcon(
            imageVector = icon,
            modifier = Modifier.size(48.dp),
            contentDescription = title
        )
        Text(
            text = title,
            fontSize = 14.sp,
//            style = MaterialTheme.typography.bodySmall
        )
    }
}