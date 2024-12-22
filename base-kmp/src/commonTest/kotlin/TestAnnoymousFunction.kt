import kotlin.test.Test


class TestAnonymousFunction {
    @Test
    fun test_anonymousFunction() {
        val sum: (Int, Int, Int) -> Int = fun(a, b, c) = a + b + c
        println(sum(1, 2, 3))

        val sum2 = fun(a: Int, b: Int, c: Int): Int {
            return a + b + c
        }
        println(sum2(1, 2, 3))
    }
}