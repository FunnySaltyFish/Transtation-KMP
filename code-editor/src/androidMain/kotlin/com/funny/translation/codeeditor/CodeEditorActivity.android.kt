package com.funny.translation.codeeditor

import android.os.Bundle
import androidx.activity.compose.setContent
import com.funny.translation.BaseActivity
import com.funny.translation.codeeditor.ui.CodeEditorNavigation
import com.funny.translation.ui.App

actual class CodeEditorActivity : BaseActivity() {
    private val TAG = "CodeEditorActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App {
                CodeEditorNavigation()
            }
        }
    }
}