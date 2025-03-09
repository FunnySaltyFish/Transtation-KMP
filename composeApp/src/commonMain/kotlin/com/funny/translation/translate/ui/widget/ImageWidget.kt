package com.funny.translation.translate.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.funny.jetsetting.core.ui.FunnyIcon
import com.funny.translation.kmp.painterDrawableRes
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.seiko.imageloader.model.ImageRequest
import com.seiko.imageloader.ui.AutoSizeImage

@Composable
fun AsyncImage(
    model: Any,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    placeholder: Painter = painterDrawableRes("ic_loading"),
    contentScale: ContentScale = ContentScale.Fit,
    onError: (Throwable) -> Unit = {
        it.printStackTrace()
    }
) {
    AutoSizeImage(
        request = ImageRequest {
//            if (model is String && model.startsWith("file://")) {
//                Log.d("AsyncImage", "Load image from file: $model")
//                data(model.removePrefix("file://").toPath())
//            } else {
//                data(model)
//            }
            data(model)
        },
        contentDescription = contentDescription,
        modifier = modifier,
        placeholderPainter = { placeholder },
        contentScale = contentScale,
        errorPainter = { rememberVectorPainter(Icons.Default.Error) }
    )
}

@Composable
fun ShadowedAsyncRoundImage(
    modifier: Modifier = Modifier,
    model: Any,
    contentDescription: String? = null,
) {
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier
            .shadow(8.dp, CircleShape, ambientColor = Color(0xffbdbdbd)),
    )
}

@Composable
fun ShadowedRoundImage(
    modifier: Modifier = Modifier,
    funnyIcon: FunnyIcon,
    contentDescription: String? = null,
) {
    FunnyImage(
        modifier = modifier.shadow(4.dp, CircleShape, ambientColor = Color(0xffbdbdbd)),
        funnyIcon = funnyIcon,
        contentDescription = contentDescription,
        contentScale = ContentScale.FillBounds
    )
}

@Composable
fun ZoomableImage(
    modifier: Modifier = Modifier,
    uri: String,
    contentDescription: String? = "image"
) {
    SketchZoomAsyncImage(
        uri = uri,
        contentDescription = contentDescription,
        modifier = modifier
    )
}

@Composable
fun FunnyImage(
    modifier: Modifier = Modifier,
    funnyIcon : FunnyIcon,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String? = null,
) {
    when (val icon = funnyIcon.get()) {
        is ImageVector -> {
            Image(imageVector = icon, contentDescription = contentDescription, modifier = modifier, contentScale = contentScale)
        }

        is Int -> {
            error("image id is not support in CMP")
//        Image(painter = painterDrawableRes(icon), contentDescription = contentDescription, modifier = modifier, contentScale = contentScale)
        }

        is String -> {
            Image(painter = painterDrawableRes(icon), contentDescription = contentDescription, modifier = modifier, contentScale = contentScale)
        }
    }
}