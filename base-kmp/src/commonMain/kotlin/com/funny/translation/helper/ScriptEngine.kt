package com.funny.translation.helper

import org.mozilla.javascript.Context
import org.mozilla.javascript.Function
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.ScriptableObject
import java.util.concurrent.ConcurrentHashMap

private const val DefaultID = "default"

object ScriptEngine {
    private val contexts = ConcurrentHashMap<String, Context>()
    private val scopes = ConcurrentHashMap<String, ScriptableObject>()

    private fun getContext(scriptId: String = DefaultID): Context {
        return contexts.computeIfAbsent(scriptId) {
            val cx = Context.enter()
            cx.optimizationLevel = -1 // 关闭优化以兼容所有 Android 设备
            cx
        }
    }

    private fun getScope(scriptId: String = DefaultID): ScriptableObject {
        return scopes.computeIfAbsent(scriptId) {
            getContext(scriptId).initStandardObjects()
        }
    }

    fun getScopedContext(scriptId: String = DefaultID): Pair<Context, ScriptableObject> {
        return getContext(scriptId) to getScope(scriptId)
    }

    fun eval(script: String, scriptId: String = DefaultID): Any? {
        return getContext(scriptId).use { cx ->
            getScope(scriptId).let { scope ->
                cx.evaluateString(scope, script, scriptId, 1, null)
            }
        }
    }

    fun invokeFunction(functionName: String, vararg args: Any?, scriptId: String = DefaultID): Any? {
        val scope = getScope(scriptId)
        val function = scope.get(functionName, scope) as? Function
            ?: throw IllegalArgumentException("Function $functionName not found in script $scriptId")

        return getContext(scriptId).use { cx ->
            function.call(cx, scope, scope, args)
        }
    }

    fun invokeMethod(obj: NativeObject, methodName: String, vararg args: Any?, scriptId: String = DefaultID): Any? {
        val scope = getScope(scriptId)
        val function = obj.get(methodName, scope) as? Function
            ?: throw IllegalArgumentException("Function $methodName not found in script $scriptId")

        return getContext(scriptId).use { cx ->
            function.call(cx, scope, obj, args)
        }
    }

    fun get(key: String, scriptId: String = DefaultID): Any? {
        return getScope(scriptId).get(key, getScope(scriptId))
    }

    fun put(key: String, value: Any?, scriptId: String = DefaultID) {
        val scope = getScope(scriptId)
        ScriptableObject.putProperty(scope, key, Context.javaToJS(value, scope))
    }

    fun cleanUp(scriptId: String) {
        contexts[scriptId]?.let {
            Context.exit()
            contexts.remove(scriptId)
        }
        scopes.remove(scriptId)
    }
}

// call `ScriptEngine` with scriptId conveniently
class ScriptEngineDelegate(private val scriptId: String) {
    fun eval(script: String): Any? {
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