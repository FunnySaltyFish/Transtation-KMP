import com.funny.translation.helper.extractJSON
import org.junit.Test
import kotlin.test.assertEquals

class StringExtensionsTest {

    @Test
    fun extractJSON_ReturnsJSON_WhenValidJSONIsWrappedInMarkdown() {
        val input = "Here is some code: ```json {\"key\": \"value\"}``` and more text."
        val expected = "{\"key\": \"value\"}"
        assertEquals(expected, input.extractJSON())
    }

    @Test
    fun extractJSON_ReturnsJSON_WhenValidJSONIsNotWrapped() {
        val input = "{ \"key\": \"value\" } is a JSON string."
        val expected = "{ \"key\": \"value\" }"
        assertEquals(expected, input.extractJSON())
    }

    @Test
    fun extractJSON_ReturnsEmptyJSON_WhenInputIsEmpty() {
        val input = ""
        val expected = "{}"
        assertEquals(expected, input.extractJSON())
    }

    @Test
    fun extractJSON_ReturnsEmptyJSON_WhenNoValidJSONPresent() {
        val input = "This string has no JSON."
        val expected = "{}"
        assertEquals(expected, input.extractJSON())
    }

    @Test
    fun extractJSON_ReturnsJSONArray_WhenValidJSONArrayIsWrappedInMarkdown() {
        val input = "Example: ```json [{\"key1\": \"value1\"}, {\"key2\": \"value2\"}]```"
        val expected = "[{\"key1\": \"value1\"}, {\"key2\": \"value2\"}]"
        assertEquals(expected, input.extractJSON())
    }

    @Test
    fun testJSONArray() {
        val input = "[{\"key1\": \"value1\"}, {\"key2\": \"value2\"}]"
        val expected = input
        assertEquals(expected, input.extractJSON())
    }

    @Test
    fun testMultiLineJSON() {
        val input = """
            {
                "key1": "value1",
                "key2": "value2"
            }
        """.trimIndent()
        val expected = """
            {
                "key1": "value1",
                "key2": "value2"
            }
        """.trimIndent()
        assertEquals(expected, input.extractJSON())
    }
}