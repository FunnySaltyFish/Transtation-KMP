package com.funny.translation.kmp

import App
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.funny.translation.base.ScriptEngine

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val code = """
            function helloWorld() {
                return "Hello World!";
            }
            console.log(helloWorld());
        """.trimIndent()

        val engine = ScriptEngine
        engine.put("console", object {
            fun log(msg: String) {
                Log.d("MainActivity", "log: $msg")
            }
        })
        engine.eval(script = code)

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