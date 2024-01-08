// androidMain/src/com/funny/translation/helper/ClipBoardUtilAndroid.kt

package com.funny.translation.helper

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import com.funny.translation.kmp.appCtx

actual object ClipBoardUtil {
    private const val CLIPBOARD_LABEL = "ClipBoardLabel"

    private fun getClipboardManager(context: Context): ClipboardManager {
        return context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    actual fun read(): String {
        return try {
            val clipboardManager = getClipboardManager(appCtx)
            if (clipboardManager.hasPrimaryClip() && clipboardManager.primaryClipDescription?.hasMimeType(
                    ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
                val clip = clipboardManager.primaryClip
                val item = clip?.getItemAt(0)
                item?.text?.toString() ?: ""
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    actual fun copy(content: CharSequence?) {
        val clipboardManager = getClipboardManager(appCtx)
        val clip = ClipData.newPlainText(CLIPBOARD_LABEL, content)
        clipboardManager.setPrimaryClip(clip)
    }

    actual fun clear() {
        val clipboardManager = getClipboardManager(appCtx)
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", ""))
    }
}
