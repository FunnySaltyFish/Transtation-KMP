package com.funny.translation.base
import javax.script.Invocable
import javax.script.ScriptEngineManager
import javax.script.ScriptEngine as JavaxScriptEngine

actual object ScriptEngine {
    private val engine: JavaxScriptEngine by lazy(LazyThreadSafetyMode.PUBLICATION) {
        ScriptEngineManager().getEngineByName("rhino")
    }

    private val invocable get() = engine as Invocable

    actual fun eval(script: String, scriptId: String): Any? {
        return engine.eval(script)
    }

    actual fun invokeFunction(functionName: String, vararg args: Any?, scriptId: String): Any? {
        return invocable.invokeFunction(functionName, *args)
    }

    actual fun get(key: String, scriptId: String): Any? {
        return engine.get(key)
    }

    actual fun put(key: String, value: Any?, scriptId: String) {
        engine.put(key, value)
    }
}