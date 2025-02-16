package com.funny.translation.network

import com.funny.translation.helper.Log
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.roundToInt

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
            // 动态创建提取器
            val paramExtractor = timeout.timeoutParamExtractor.java.getDeclaredConstructor().newInstance()

            // 获取参数
            val modelId = paramExtractor.getModelId(request) ?: 0
            val textLength = paramExtractor.getTextLength(request) ?: 0
            val baseReadTimeout = paramExtractor.getBaseReadTimeout(request) ?: 40
            val perCharTimeoutMillis = paramExtractor.getPerCharTimeoutMillis(request) ?: 5

            // 计算超时时间
            val calculatedTimeout = baseReadTimeout +
                    ((textLength * perCharTimeoutMillis).toFloat() / 1000).roundToInt()

            val readTimeout = max(timeout.readTimeout, calculatedTimeout)

            Log.i("DynamicTimeout", "Model: $modelId, TextLength: $textLength, base: ${baseReadTimeout}, perChar: ${perCharTimeoutMillis}, Calculated: $calculatedTimeout")

            var newChain = chain
            if (timeout.connectTimeout > 0) {
                newChain = newChain.withConnectTimeout(timeout.connectTimeout, TimeUnit.SECONDS)
            }
            if (readTimeout > 0) {
                newChain = newChain.withReadTimeout(readTimeout, TimeUnit.SECONDS)
            }
            if (timeout.writeTimeout > 0) {
                newChain = newChain.withWriteTimeout(timeout.writeTimeout, TimeUnit.SECONDS)
            }
            Log.d("DynamicTimeoutInterceptor", "proceed timeout: (${newChain.connectTimeoutMillis()}, ${newChain.readTimeoutMillis()}, ${newChain.writeTimeoutMillis()})")
            return newChain.proceed(request)
        }

        return chain.proceed(request)
    }

}