package com.funny.translation.kmp

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.funny.translation.database.TransHistory
import com.funny.translation.database.appDB
import com.funny.translation.helper.Log
import com.funny.translation.helper.now
import com.funny.translation.translate.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            val transHistory = TransHistory(0, "hello", Language.ENGLISH.id, Language.CHINESE.id, arrayListOf(), now())
            appDB.transHistoryQueries.insertTransHistory(transHistory)
            Log.d("transHistory = ${appDB.transHistoryQueries.queryAllBetween(0, now()).executeAsList()}")
        }

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}