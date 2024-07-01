package com.funny.translation.helper

import kotlinx.coroutines.ThreadContextElement
import kotlinx.coroutines.withContext
import org.mozilla.javascript.Context
import org.mozilla.javascript.ContextFactory
import org.mozilla.javascript.Function
import org.mozilla.javascript.NativeJavaObject
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.ScriptableObject
import org.mozilla.javascript.WrapFactory
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

private const val DefaultID = "default"

// 此类来自 GPT-4-Turbo，用于在协程作用域中管理 Rhino 的 Context
class RhinoContextElement(private val scriptId: String) : AbstractCoroutineContextElement(RhinoContextElement), ThreadContextElement<Context> {
    companion object Key : CoroutineContext.Key<RhinoContextElement>
    // 通过 ThreadLocal 保证在多线程环境下有正确的 Context
    private val threadLocal = ThreadLocal<Context>()

    override fun updateThreadContext(context: CoroutineContext): Context {
        val oldState = threadLocal.get()
        if (oldState == null) {
            threadLocal.set(ScriptEngine.getContext(scriptId))
        }
        return oldState ?: Context.enter()
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: Context) {
        Context.exit()
        threadLocal.remove()
    }
}

object ScriptEngine {
    private val contexts = ConcurrentHashMap<String, Context>()
    private val scopes = ConcurrentHashMap<String, ScriptableObject>()
    private val contextFactory = ContextFactory()

//    fun getContext(scriptId: String = DefaultID): Context {
//        var context = contextThreadLocal.get()
//        if (context == null) {
//            context = contextFactory.enterContext().apply {
//                optimizationLevel = -1
//                languageVersion = Context.VERSION_ES6
//                setLocale(Locale.getDefault())
//                wrapFactory = WrapFactory()
//            }
//            contextThreadLocal.set(context)
//        }
//        return context
//    }

    internal fun getContext(scriptId: String = DefaultID): Context {
        return contextFactory.enterContext().apply {
            optimizationLevel = -1
            languageVersion = Context.VERSION_ES6
            setLocale(Locale.getDefault())
            wrapFactory = WrapFactory()
        }.also {
            contexts[scriptId] = it
        }
    }

    fun cleanUp(scriptId: String) {
        contexts[scriptId]?.let {
            Context.exit()
            contexts.remove(scriptId)
        }
        scopes.remove(scriptId)
    }

//    private fun getContext(scriptId: String = DefaultID): Context {
//        return contexts.computeIfAbsent(scriptId) {
//            val cx = Context.enter()
//            cx.optimizationLevel = -1 // 关闭优化以兼容所有 Android 设备
//            cx.languageVersion = Context.VERSION_ES6
//            cx.setLocale(Locale.getDefault())
//            cx.wrapFactory = WrapFactory()
//            val scope = ImporterTopLevel()
//            scope.initStandardObjects(Context.getCurrentContext(), false)
//            scopes[scriptId] = scope
//            cx
//        }
//    }

    private fun getScope(scriptId: String = DefaultID): ScriptableObject {
        return scopes.computeIfAbsent(scriptId) {
            getContext(scriptId).initStandardObjects()
        }
    }

    fun getScopedContext(scriptId: String = DefaultID): Pair<Context, ScriptableObject> {
        return getContext(scriptId) to getScope(scriptId)
    }

    suspend fun eval(script: String, scriptId: String = DefaultID): Any? {
        return withContext(RhinoContextElement(scriptId)) {
            getContext(scriptId).let { cx ->
                getScope(scriptId).let { scope ->
                    cx.evaluateString(scope, script, scriptId, 1, null).unwrap()
                }
            }
        }
    }

    fun invokeFunction(functionName: String, vararg args: Any?, scriptId: String = DefaultID): Any? {
        val scope = getScope(scriptId)
        val function = scope.get(functionName, scope) as? Function
            ?: throw IllegalArgumentException("Function $functionName not found in script $scriptId")

        return getContext(scriptId).let { cx ->
            function.call(cx, scope, scope, args).unwrap()
        }
    }

    fun invokeMethod(obj: NativeObject, methodName: String, vararg args: Any?, scriptId: String = DefaultID): Any? {
        val scope = getScope(scriptId)
        val function = obj.get(methodName, scope) as? Function
            ?: throw IllegalArgumentException("Function $methodName not found in script $scriptId")

        return getContext(scriptId).let { cx ->
            function.call(cx, scope, obj, args).unwrap()
        }
    }

    fun get(key: String, scriptId: String = DefaultID): Any? {
        return getScope(scriptId).get(key, getScope(scriptId)).unwrap()
    }

    fun put(key: String, value: Any?, scriptId: String = DefaultID) {
        val scope = getScope(scriptId)
        scope.put(key, scope, value)
    }

    // 将一些常见的包装类型转化为原生类型
    private fun Any?.unwrap() = when(this) {
        is NativeJavaObject -> this.unwrap()
        else -> this
    }
}

// call `ScriptEngine` with scriptId conveniently
class ScriptEngineDelegate(private val scriptId: String) {
    suspend fun eval(script: String): Any? {
        return ScriptEngine.eval(script, scriptId)
    }

    fun invokeFunction(functionName: String, vararg args: Any?): Any? {
        return ScriptEngine.invokeFunction(functionName, *args, scriptId = scriptId)
    }

    fun invokeMethod(obj: NativeObject, methodName: String, vararg args: Any?): Any? {
        return ScriptEngine.invokeMethod(obj, methodName, *args, scriptId = scriptId)
    }

    fun get(key: String): Any? {
        return ScriptEngine.get(key, scriptId)
    }

    fun put(key: String, value: Any?) {
        ScriptEngine.put(key, value, scriptId)
    }

    fun cleanUp() {
        ScriptEngine.cleanUp(scriptId)
    }
}