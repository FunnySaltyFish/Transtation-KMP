import com.funny.translation.helper.OkHttpUtils
import kotlin.test.Test

class TestOkHttp {
    @Test
    fun test_get() {
        val url = "https://www.baidu.com"
        val content = OkHttpUtils.get(url)
        println(content)
    }
}