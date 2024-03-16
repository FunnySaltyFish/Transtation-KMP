package com.funny.translation.network

//class LoggingInterceptor(
//    val print: Boolean = true
//): Interceptor {
//    override fun intercept(chain: Interceptor.Chain): Response {
//        val request = chain.request()
//        // 打印
//        log(LINE_SEP)
//        log("【Request】")
//        log("- Url: ${request.url}")
//        log("- Method: ${request.method}")
//        log("- Headers: ${request.headers}")
//
//        log(LINE_SEP)
//        val response = chain.proceed(request)
//        // 非流请求打印结果
//        log("【Response】")
//        log("- Code: ${response.code}, Message: ${response.message}")
//        log("- Headers: ${response.headers}")
//
//        return response
//    }
//
//    private fun log(msg: String) {
//        if (print) {
//            Log.d("LoggingInterceptor", msg)
//        }
//    }
//
//    companion object {
//        private val LINE_SEP = "=".repeat(10)
//    }
//}