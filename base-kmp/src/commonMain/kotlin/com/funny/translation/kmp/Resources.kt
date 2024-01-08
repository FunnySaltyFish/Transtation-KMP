package com.funny.translation.kmp

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun painterDrawableRes(name: String, suffix: String = "png") = painterResource("drawable/${name}.$suffix")