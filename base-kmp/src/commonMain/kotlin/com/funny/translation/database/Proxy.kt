package com.funny.translation.database

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.funny.translation.helper.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

private const val TAG = "DaoProxy"

// 转换 Dao 的调用为 SqlDelight 的调用
class DaoProxy(private val sqlDelightQueries: Any) : InvocationHandler {
    override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
        return callAppropriateMethod(method, args)
//            ?: callMethod(roomDao, method, args, beanClazz, beanClazz)
    }

    private fun callAppropriateMethod(
        method: Method,
        args: Array<out Any>?
    ): Any? {
        val sqldelightMethod = sqlDelightQueries.javaClass.methods.find { it.name == method.name }
        sqldelightMethod ?: throw UnsupportedOperationException("Method ${method.name} not found")
        Log.d(TAG, "find sqldelightMethod: $sqldelightMethod")

        val returnType = method.returnType
        // 强转成类型 Query<ExpectedGenericType>
        val query = sqldelightMethod.invoke(sqlDelightQueries, *args.orEmpty()) as? Query<*> ?: return null
        // 调用 Query 的 executeAsList 方法

        return when (returnType) {
            List::class.java -> query.executeAsList()
            Flow::class.java -> query.executeAsFlowList()
            else -> query.executeAsOneOrNull()
        }
    }

    private fun callMethod(
        target: Any,
        method: Method,
        args: Array<out Any>?,
        expectedReturnType: Class<*>? = null,
        expectedGenericType: Class<*>? = null
    ): Any? {
        val result = method.invoke(target, *args.orEmpty())

        if (expectedReturnType != null && !expectedReturnType.isAssignableFrom(method.returnType)) {
            throw UnsupportedOperationException("Method ${method.name} does not return expected type.")
        }

        if (expectedGenericType != null && result is List<*> && result.isNotEmpty() && result[0] != null) {
            val actualGenericType = result[0]!!.javaClass
            if (!expectedGenericType.isAssignableFrom(actualGenericType)) {
                throw UnsupportedOperationException("Method ${method.name} does not return expected generic type.")
            }
        }

        return result
    }
}

// 工厂函数创建代理
inline fun <reified T : Any> createDaoProxy(sqlDelightQueries: Any): T {
    val handler = DaoProxy(sqlDelightQueries)
    return Proxy.newProxyInstance(
        T::class.java.classLoader,
        arrayOf(T::class.java),
        handler
    ) as T
}

inline fun <reified RowType : Any> Query<RowType>.executeAsFlowList(): Flow<List<RowType>> {
    return this.asFlow().mapToList(Dispatchers.IO)
}