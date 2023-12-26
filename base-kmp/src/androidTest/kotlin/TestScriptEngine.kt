
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.funny.translation.base.ScriptEngine
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
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