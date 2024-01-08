// commonMain/src/com/funny/translation/helper/ClipBoardUtil.kt

package com.funny.translation.helper

expect object ClipBoardUtil {
    fun read(): String
    fun copy(content: CharSequence?)
    fun clear()
}
