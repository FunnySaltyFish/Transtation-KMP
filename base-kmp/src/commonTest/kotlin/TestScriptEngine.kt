import com.funny.translation.helper.ScriptEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.test.Test

class TestScriptEngine {
    @Test
    fun test_HelloWorld() {
        val code = """
            function helloWorld() {
                return "Hello World!";
            }
            console.log(helloWorld());
        """.trimIndent()

        val engine = ScriptEngine
        engine.put("console", object {
            fun log(msg: String) {
                println(msg)
            }
        })
        CoroutineScope(Dispatchers.IO).launch {
            engine.eval(script = code)
        }
    }
}