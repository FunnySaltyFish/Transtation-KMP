package com.funny.translation.network

import com.funny.translation.helper.Log
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import java.util.concurrent.TimeUnit

/**
 *  @Override
 *     public Response intercept(Chain chain) throws IOException {
 *         Request request = chain.request();
 * 		//核心代码!!!
 *         final Invocation tag = request.tag(Invocation.class);
 *         final Method method = tag != null ? tag.method() : null;
 *         final DynamicTimeout timeout = method != null ? method.getAnnotation(DynamicTimeout.class) : null;
 *
 *         XLog.d("invocation",tag!= null ? tag.toString() : "");
 *
 *         if(timeout !=null && timeout.timeout() > 0){
 *
 *             Response proceed = chain.withConnectTimeout(timeout.timeout(), TimeUnit.SECONDS)
 *                     .withReadTimeout(timeout.timeout(), TimeUnit.SECONDS)
 *                     .withWriteTimeout(timeout.timeout(), TimeUnit.SECONDS)
 *                     .proceed(request);
 *             return proceed;
 *         }
 *
 *         return chain.proceed(request);
 *     }
 *
 */

class DynamicTimeoutInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val tag = request.tag(Invocation::class.java)
        val method = tag?.method()
        val timeout = method?.getAnnotation(DynamicTimeout::class.java)

        if (timeout != null) {
            // 大于 0 才设置超时时间，否则使用默认值
            Log.i("DynamicTimeoutInterceptor", "reset dynamic timeout: $timeout for url: ${request.url}")
            return chain.apply {
                if (timeout.connectTimeout > 0) {
                    withConnectTimeout(timeout.connectTimeout, TimeUnit.SECONDS)
                }
                if (timeout.readTimeout > 0) {
                    withReadTimeout(timeout.readTimeout, TimeUnit.SECONDS)
                }
                if (timeout.writeTimeout > 0) {
                    withWriteTimeout(timeout.writeTimeout, TimeUnit.SECONDS)
                }
                Log.d("DynamicTimeoutInterceptor", "proceed timeout: (${chain.connectTimeoutMillis()}, ${chain.readTimeoutMillis()}, ${chain.writeTimeoutMillis()})")
            }.proceed(request)
        }

        return chain.proceed(request)
    }

}