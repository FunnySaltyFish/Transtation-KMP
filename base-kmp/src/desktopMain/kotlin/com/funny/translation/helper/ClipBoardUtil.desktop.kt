// jvmMain/src/com/funny/translation/helper/ClipBoardUtilJvm.kt

package com.funny.translation.helper

import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

actual object ClipBoardUtil {
    private val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard

    actual fun read(): String {
        val transferable = clipboard.getContents(null)
        return if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            transferable.getTransferData(DataFlavor.stringFlavor) as? String ?: ""
        } else {
            ""
        }
    }

    actual fun copy(content: CharSequence?) {
        val selection = StringSelection(content?.toString() ?: "")
        clipboard.setContents(selection, selection)
    }

    actual fun clear() {
        clipboard.setContents(StringSelection(""), null)
    }
}
