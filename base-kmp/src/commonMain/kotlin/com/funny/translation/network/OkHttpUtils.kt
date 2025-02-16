package com.funny.translation.network

import androidx.annotation.Keep
import cn.netdiscovery.http.interceptor.LoggingInterceptor
import cn.netdiscovery.http.interceptor.log.LogManager
import cn.netdiscovery.http.interceptor.log.LogProxy
import com.funny.translation.AppConfig
import com.funny.translation.BaseActivity
import com.funny.translation.BuildConfig
import com.funny.translation.GlobalTranslationConfig
import com.funny.translation.helper.CacheManager
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.LocaleUtils
import com.funny.translation.helper.Log
import com.funny.translation.helper.createParentDirIfNotExist
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.ActivityManager
import com.funny.translation.kmp.appCtx
import com.funny.translation.kmp.base.strings.ResStrings
import com.funny.translation.network.ServiceCreator.TRANS_PATH
import com.funny.translation.sign.SignUtils
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit

@Keep
object OkHttpUtils {
    private const val SET_COOKIE_KEY = "set-cookie"
    private const val COOKIE_NAME = "Cookie"
    private const val CONNECT_TIMEOUT = 15L
    private const val READ_TIMEOUT = 20L
    private const val TAG = "OkHttpUtils"

    private fun saveCookie(url: String?, domain: String?, cookies: String) {
        url ?: return
        DataSaverUtils.saveData(url, cookies)
        domain ?: return
        DataSaverUtils.saveData(domain, cookies)
    }

    private val cache = Cache(CacheManager.cacheDir, 1024 * 1024 * 20L)

    init {
        Log.d(TAG, "cache path: ${CacheManager.cacheDir}")
    }

    private fun createBaseClient() = OkHttpClient().newBuilder().apply {
        connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        cache(cache)
        addInterceptor(HttpCacheInterceptor())
        // set request cookie
        // 添加自定义请求头
        addInterceptor {
            val request = it.request()
            val builder = request.newBuilder()
            var newUrl = URL(removeExtraSlashOfUrl(request.url.toString()))
            val domain = request.url.host
            // get domain cookie
            if (domain.isNotEmpty()) {
                val spDomain: String = DataSaverUtils.readData(domain, "")
                val cookie: String = spDomain.ifEmpty { "" }
                if (cookie.isNotEmpty()) {
                    builder.addHeader(COOKIE_NAME, cookie)
                }
            }
            // 对所有向本项目请求的域名均加上应用名称
            if (newUrl.toString().startsWith(ServiceCreator.BASE_URL)){
                builder.addHeader("Referer", "FunnyTranslation")
                builder.addHeader("User-Agent", "FunnyTranslation/${AppConfig.versionCode}")
                builder.addHeader("App-Build-Type", BuildConfig.BUILD_TYPE)
                builder.addHeader("App-Flavor", BuildConfig.FLAVOR)
                builder.addHeader("Accept-Language", LocaleUtils.getAppLanguage().toLocale().language)
            }

            // 访问 trans/v1下的所有api均带上请求头-jwt
            if (newUrl.path.startsWith(TRANS_PATH)){
                val jwt = AppConfig.jwtToken
                if (jwt != "") builder.addHeader("Authorization", "Bearer $jwt")
            }

            if (newUrl.path.startsWith(TRANS_PATH + "api/translate")){
                if (GlobalTranslationConfig.isValid()) {
                    builder.addHeader("sign", SignUtils.encodeSign(
                        uid = AppConfig.uid.toLong(), appVersionCode = AppConfig.versionCode,
                        sourceLanguageCode = GlobalTranslationConfig.sourceLanguage!!.id,
                        targetLanguageCode = GlobalTranslationConfig.targetLanguage!!.id,
                        text = GlobalTranslationConfig.sourceString!!,
                        extra = 1 // 1 -> 截止 2.8.0 出现pojieban的额外措施
                    ).also {
                        Log.d(TAG, "createBaseClient: add sign: $it")
                    })

                    // 对于文本翻译，如果是 vip 且开启了显示详细结果，那么加上 show_detail=true
                    if (!newUrl.path.endsWith("translate_image")
                        && !newUrl.path.endsWith("translate_streaming")
                        && AppConfig.isMembership() && AppConfig.sShowDetailResult.value) {
                        newUrl = URL("$newUrl&show_detail=true")
                    }
                }
            }

            builder.url(newUrl)
            it.proceed(builder.build())
        }

        addInterceptor(DynamicTimeoutInterceptor())

        // get response cookie
        addInterceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            val requestUrl = request.url.toString()
            val domain = request.url.host

            // token 过期了
            // 422: jwt 校验错误
            if (response.code in intArrayOf(401, 422) && requestUrl.startsWith(ServiceCreator.BASE_URL)){
                val clazz = Class.forName("com.funny.trans.login.LoginActivity")
                ActivityManager.start(clazz as Class<BaseActivity>, )
                AppConfig.logout()
                appCtx.toastOnUi(ResStrings.login_status_expired)

                return@addInterceptor response
            }

            // set-cookie maybe has multi, login to save cookie
            if (response.headers(SET_COOKIE_KEY).isNotEmpty()) {
                val cookies = response.headers(SET_COOKIE_KEY)
                val cookie = encodeCookie(cookies)
                saveCookie(requestUrl, domain, cookie)
            }
            response
        }

        // 如果不是流式请求，才添加日志拦截器
        addInterceptor(
            LoggingInterceptor.Builder()
                .loggable(BuildConfig.DEBUG)
                .request()
                .response()
                .excludePath(TRANS_PATH + "ai/ask_stream")
                .excludePath(TRANS_PATH + "ai/tts/generate_stream")
                .excludePath(TRANS_PATH + "app_update/get_apk")
                .excludePath(TRANS_PATH + "api/translate_streaming")
                .build()
        )

        LogManager.logProxy(object: LogProxy {
            override fun d(tag: String, msg: String) {
                Log.d(tag, msg)
            }

            override fun e(tag: String, msg: String) {
                Log.e(tag, msg)
            }

            override fun i(tag: String, msg: String) {
                Log.i(tag, msg)
            }

            override fun w(tag: String, msg: String) {
                Log.w(tag, msg)
            }
        })

    }.build()

    val okHttpClient by lazy {
        createBaseClient()
    }

    @JvmOverloads
    fun get(
        url: String,
        headersMap: HashMap<String, String>? = null,
        params: HashMap<String, String>? = null,
        timeout: IntArray? = null // [connectTimeout, readTimeout, writeTimeout]
    ): String {
        val response = getResponse(url, headersMap, params, timeout)
        return response.body?.string() ?: ""
    }

    @JvmOverloads
    fun getRaw(
        url: String,
        headersMap: HashMap<String, String>? = null,
        params: HashMap<String, String>? = null,
        timeout: IntArray? = null // [connectTimeout, readTimeout, writeTimeout]
    ): ByteArray {
        val response = getResponse(url, headersMap, params, timeout)
        return response.body?.bytes() ?: ByteArray(0)
    }

    @JvmOverloads
    fun getResponse(
        url: String,
        headersMap: HashMap<String, String>? = null,
        params: HashMap<String, String>? = null,
        timeout: IntArray? = null // [connectTimeout, readTimeout, writeTimeout]
    ): Response {
        val urlBuilder = url.toHttpUrl().newBuilder()
        params?.let {
            for ((key, value) in it) {
                urlBuilder.addQueryParameter(key, value)
            }
        }
        val requestBuilder = Request.Builder().url(urlBuilder.build()).get()
        headersMap?.let {
            requestBuilder.addHeaders(it)
        }
        val client = getClient(timeout)
        return client.newCall(requestBuilder.build()).execute()
    }

    /**
     * 支持断点续传的下载，如果文件已存在，则会在文件末尾追加数据。
     * 需要由外部处理错误
     * @param url String
     * @param file File
     * @param headersMap HashMap<String, String>?
     * @param timeout IntArray?
     * @param progressCallback Function2<[@kotlin.ParameterName] Long, [@kotlin.ParameterName] Long, Unit>?
     */
    @JvmOverloads
    fun downloadWithResume(
        url: String,
        file: File,
        expectedLength: Long,
        headersMap: HashMap<String, String>? = null,
        timeout: IntArray? = null,
        progressCallback: ((downloadedBytes: Long, totalBytes: Long) -> Unit)? = null
    ) {
        val existingFileLength = if (file.exists()) file.length() else 0L
        if (existingFileLength >= expectedLength) {
            progressCallback?.invoke(existingFileLength, existingFileLength)
            return
        }
        // Create request with Range header if the file already exists
        val requestBuilder = Request.Builder().url(url)
        if (existingFileLength > 0) {
            requestBuilder.addHeader("Range", "bytes=$existingFileLength-")
        }
        headersMap?.let {
            requestBuilder.addHeaders(it)
        }

        val request = requestBuilder.build()
        val client = getClient(timeout)

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { resp ->
                    if (!resp.isSuccessful) throw IOException("${resp.message}(code=${resp.code})")

                    val totalBytes = resp.body?.contentLength() ?: 0L
                    file.createParentDirIfNotExist()
                    val outputStream = FileOutputStream(file, existingFileLength > 0)

                    resp.body?.byteStream()?.use { inputStream ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var downloadedBytes = existingFileLength

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            progressCallback?.invoke(downloadedBytes, totalBytes)
                            Log.d(TAG, "downloadWithResume: $downloadedBytes / $totalBytes")
                        }
                    }
                    outputStream.flush()
                    outputStream.close()
                }
            }
        })
    }


    @Throws(IOException::class)
    @JvmOverloads
    fun postJSON(
        url: String,
        json: String,
        headers: HashMap<String, String>? = null,
        timeout: IntArray? = null
    ): String {
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body: RequestBody = json.toRequestBody(JSON)
        val requestBuilder = Request.Builder()
            .url(url)
            .post(body)
        headers?.let {
            requestBuilder.addHeaders(headers)
        }
        val response: Response = getClient(timeout).newCall(requestBuilder.build()).execute()
        return response.body?.string() ?: ""
    }

    @JvmOverloads
    fun postForm(
        url: String,
        form: HashMap<String, String>,
        headers: HashMap<String, String>? = null,
        timeout: IntArray? = null
    ): String {
        val builder = FormBody.Builder()
        for ((key, value) in form) {
            builder.add(key, value)
        }
        val body: RequestBody = builder.build()
        val requestBuilder = Request.Builder()
            .url(url)
            .post(body)
        headers?.let {
            requestBuilder.addHeaders(headers)
        }
        val response: Response = getClient(timeout).newCall(requestBuilder.build()).execute()
        return response.body?.string() ?: ""
    }

    @JvmOverloads
    fun postMultiForm(
        url: String,
        body: RequestBody,
        headers: HashMap<String, String>? = null,
        timeout: IntArray? = null
    ): String {
        val requestBuilder = Request.Builder()
            .url(url)
            .post(body)
        headers?.let {
            requestBuilder.addHeaders(headers)
        }
        val response: Response = getClient(timeout).newCall(requestBuilder.build()).execute()
        return response.body?.string() ?: ""
    }

    @JvmOverloads
    fun getClient(timeout: IntArray? = null): OkHttpClient {
        var client = okHttpClient
        timeout?.let {
            if (it.size == 3) {
                Log.d(TAG, "getClient: reset timeout: ${it.joinToString()}")
                client = okHttpClient.newBuilder()
                    .connectTimeout(it[0].toLong(), TimeUnit.SECONDS)
                    .readTimeout(it[1].toLong(), TimeUnit.SECONDS)
                    .writeTimeout(it[2].toLong(), TimeUnit.SECONDS)
                    .build()
            }
        }
        return client
    }

    private fun Request.Builder.addHeaders(headers: Map<String, String>) {
        headers.forEach {
            addHeader(it.key, it.value)
        }
    }

    fun removeExtraSlashOfUrl(url: String): String {
        return if (url.isEmpty()) {
            url
        } else url.replace("(?<!(http:|https:))/+".toRegex(), "/")
    }
}