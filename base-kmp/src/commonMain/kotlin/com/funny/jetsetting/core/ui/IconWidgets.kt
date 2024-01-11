package com.funny.jetsetting.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.funny.translation.kmp.painterDrawableRes
import com.funny.translation.ui.FixedSizeIcon

class FunnyIcon(
    private val imageVector: ImageVector?= null,
    private val resourceName: String? = null
){
    fun get() = imageVector ?: resourceName
}

@Composable
fun IconWidget(
    modifier: Modifier = Modifier,
    funnyIcon : FunnyIcon,
    tintColor : Color = MaterialTheme.colorScheme.secondary,
    contentDescription: String? = null,
) {
    val icon = funnyIcon.get()
    if (icon is ImageVector){
        FixedSizeIcon(imageVector = icon, contentDescription = contentDescription, tint = tintColor, modifier = modifier)
    }else if(icon is String){
//        error("show icon by resource id is not supported yet in KMP")
        FixedSizeIcon(painter = painterDrawableRes(icon), contentDescription = contentDescription, tint = tintColor, modifier = modifier)
    }
}