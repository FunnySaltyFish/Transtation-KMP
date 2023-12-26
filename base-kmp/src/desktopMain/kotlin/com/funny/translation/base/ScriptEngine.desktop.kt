package com.funny.translation.base

import org.mozilla.javascript.Context
import org.mozilla.javascript.Function
import org.mozilla.javascript.ScriptableObject
import java.util.concurrent.ConcurrentHashMap

actual object ScriptEngine {
    private val contexts = ConcurrentHashMap<String, Context>()
    private val scopes = ConcurrentHashMap<String, ScriptableObject>()

    private fun getContext(scriptId: String): Context {
        return contexts.computeIfAbsent(scriptId) {
            val cx = Context.enter()
            cx.optimizationLevel = -1 // 关闭优化以兼容所有 Android 设备
            cx
        }
    }

    private fun getScope(scriptId: String): ScriptableObject {
        return scopes.computeIfAbsent(scriptId) {
            getContext(scriptId).initStandardObjects()
        }
    }

    actual fun eval(script: String, scriptId: String): Any? {
        return getContext(scriptId).use { cx ->
            getScope(scriptId).let { scope ->
                cx.evaluateString(scope, script, "<cmd>", 1, null)
            }
        }
    }

    actual fun invokeFunction(functionName: String, vararg args: Any?, scriptId: String): Any? {
        val scope = getScope(scriptId)
        val function = scope.get(functionName, scope) as? Function
            ?: throw IllegalArgumentException("Function $functionName not found in script $scriptId")

        return getContext(scriptId).let { cx ->
            function.call(cx, scope, scope, args)
        }
    }

    actual fun get(key: String, scriptId: String): Any? {
        return getScope(scriptId).get(key, getScope(scriptId))
    }

    actual fun put(key: String, value: Any?, scriptId: String) {
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
