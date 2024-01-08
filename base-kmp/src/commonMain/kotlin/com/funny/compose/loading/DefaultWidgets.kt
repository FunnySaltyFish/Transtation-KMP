package com.funny.compose.loading

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.kmp.base.strings.ResStrings
import kotlin.math.sin


@Composable
fun DefaultLoading(modifier: Modifier = Modifier) {
    CustomLoading(modifier = modifier
        .fillMaxWidth()
        .wrapContentWidth(align = Alignment.CenterHorizontally)
        .height(40.dp)
    )
}

@Composable
fun CustomLoading(
    modifier: Modifier,
    radius: Int = 100,
    color: Color = Color.LightGray,
    yMaxOffset: Int = 20,
    animDuration: Int = 2000,
) {
    // 三个点的loading
    val transition = rememberInfiniteTransition()
    val degree by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2*3.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(animDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    Canvas(modifier = modifier) {
        // 画三个点，x位置分别在 20%, 50%, 80%，纵坐标百分比为 dot1, dot2, dot3
        drawCircle(
            color = color,
            radius = 10f,
            center = center.copy(
                x = size.width / 2 - 0.5f * radius,
                y = size.height / 2 + yMaxOffset * sin(degree)
            )
        )
        drawCircle(
            color = color,
            radius = 10f,
            center = center.copy(
                x = size.width / 2,
                y = size.height / 2 + yMaxOffset * sin(1.04f+degree)
            )
        )
        drawCircle(
            color = color,
            radius = 10f,
            center = center.copy(
                x = size.width / 2 + 0.5f * radius,
                y = size.height / 2 + yMaxOffset * sin(2.09f+degree)
            )
        )
    }
}

@Composable
fun DefaultFailure(modifier: Modifier = Modifier, retry: () -> Unit) {
    Text(
        text = ResStrings.loading_error,
        modifier = modifier
            .clickable(onClick = retry)
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .padding(8.dp),
        color = Color.Gray,
        fontWeight = FontWeight.Light,
        fontSize = 16.sp
    )
}

@Composable
fun DefaultEmpty(modifier: Modifier = Modifier) {
    Text(
        text = ResStrings.loading_empty,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .padding(8.dp),
        color = Color.Gray,
        fontWeight = FontWeight.Light,
        fontSize = 16.sp
    )
}