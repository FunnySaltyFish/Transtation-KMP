package com.funny.translation.base

private const val DefaultID = "default"

expect object ScriptEngine {
    fun eval(script: String, scriptId: String = DefaultID): Any?
    fun invokeFunction(functionName: String, vararg args: Any?, scriptId: String = DefaultID): Any?
    fun get(key: String, scriptId: String = DefaultID): Any?
    fun put(key: String, value: Any?, scriptId: String = DefaultID)
}

