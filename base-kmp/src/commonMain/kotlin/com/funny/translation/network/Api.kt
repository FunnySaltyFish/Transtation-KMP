package com.funny.translation.network

import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.KMPMain
import com.funny.translation.kmp.appCtx
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend

const val CODE_SUCCESS = 50

@OptIn(ExperimentalContracts::class)
class Api<T>(
    private val func: KFunction<CommonData<T>?>,
    private val args: Array<out Any?>,
    private val dispatcher: CoroutineDispatcher
) {
    private var successFunc = { resp: CommonData<T> ->
        appCtx.toastOnUi(resp.message)
    }

    private var failFunc = { resp: CommonData<T> ->
        appCtx.toastOnUi(resp.error_msg ?: resp.message)
    }

    private var respNullFunc = {

    }

    private var errorFunc = { err: Throwable ->
        appCtx.toastOnUi(err.message)
    }

    fun success(block: (resp: CommonData<T>) -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        this.successFunc = block
    }

    fun addSuccess(block: (resp: CommonData<T>) -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        val old = this.successFunc
        this.successFunc = { resp ->
            old(resp)
            block(resp)
        }
    }

    fun fail(block: (resp: CommonData<T>) -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        this.failFunc = block
    }

    fun addFail(block: (resp: CommonData<T>) -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        val old = this.failFunc
        this.failFunc = { resp ->
            old(resp)
            block(resp)
        }
    }

    fun error(block: (Throwable) -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        this.errorFunc = block
    }

    fun addError(block: (Throwable) -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        val old = this.errorFunc
        this.errorFunc = { err ->
            old(err)
            block(err)
        }
    }

    suspend fun call(rethrowErr: Boolean = false): T? = withContext(dispatcher) {
        try {
            val resp = if (func.isSuspend) func.callSuspend(*args) else func.call(*args)
            if (resp == null) {
                withContext(Dispatchers.KMPMain) {
                    respNullFunc()
                }
                return@withContext null
            }
            if (resp.code == CODE_SUCCESS) {
                withContext(Dispatchers.KMPMain) {
                    successFunc(resp)
                }
            } else {
                withContext(Dispatchers.KMPMain) {
                    failFunc(resp)
                }
            }
            resp.data
        } catch (e: Exception) {
            withContext(Dispatchers.KMPMain) {
                errorFunc(e)
            }
            e.printStackTrace()
            if (rethrowErr) throw e
            null
        }
    }
}

inline fun <reified T : Any?> apiNoCall(
    func: KFunction<CommonData<T>?>,
    vararg args: Any?,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    block: Api<T>.() -> Unit = {},
) = Api(func, args = args, dispatcher).apply(block)

suspend inline fun <reified T : Any?> api(
    func: KFunction<CommonData<T>?>,
    vararg args: Any?,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    rethrowErr: Boolean = false,
    block: Api<T>.() -> Unit = {},
) = apiNoCall(func, *args, dispatcher = dispatcher, block = block).call(rethrowErr)

/**
 * 不弹出“请求成功”提示的api调用
 */
suspend inline fun <reified T : Any?> apiSilent(
    func: KFunction<CommonData<T>?>,
    vararg args: Any?,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    rethrowErr: Boolean = false,
    noinline block: Api<T>.() -> Unit = {},
) = apiNoCall(func, *args, dispatcher = dispatcher, block = block).apply { success {  } }.call(rethrowErr)


//suspend inline fun <reified T> api(
//    noinline func: (args: Array<out Any>) -> CommonData<T>?,
//    vararg args: Any?,
//    dispatcher: CoroutineDispatcher = Dispatchers.IO,
//    block: Api<T>.() -> Unit = {},
//) {
//    // Api(func = ::func, args = args, dispatcher = dispatcher).apply(block).call()
//}