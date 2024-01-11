package com.funny.translation.helper

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.kyant.monet.getKeyColors

private const val TAG = "BitmapEx"

fun Bitmap.getKeyColors(count: Int) = this.asImageBitmap().getKeyColors(count)