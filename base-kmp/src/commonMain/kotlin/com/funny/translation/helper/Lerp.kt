package com.funny.translation.helper

import kotlin.math.roundToInt

fun lerp(start: Int, stop: Int, fraction: Float): Int {
    return (start + fraction * (stop - start)).roundToInt()
}

// float
fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (start + fraction * (stop - start))
}