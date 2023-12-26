import com.funny.translation.base.ScriptEngine
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
        engine.eval(script = code)
    }
}