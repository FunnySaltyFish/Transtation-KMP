package com.funny.translation.js.bean

import com.funny.translation.translate.Language

@kotlinx.serialization.Serializable
data class SerializableJsBean(
    val id : Int = 0,
    val fileName : String = "Plugin",
    val code : String = "",
    val author : String = "Author",
    val version : Int = 1,
    val description : String = "",
    val enabled : Int = 1,
    val minSupportVersion : Int = 2,
    val maxSupportVersion : Int = 999, // 自 version 4 起弃用
    val targetSupportVersion : Int = 4,
    val isOffline : Boolean = false,
    val debugMode : Boolean = true,
    val supportLanguages: List<Language> = arrayListOf()
){
    fun toSQL() = """
        insert into table_js($fileName,$code,$author,$version,$description,$enabled,
        $minSupportVersion,$maxSupportVersion,$isOffline,$debugMode)
        )
    """.trimIndent()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JsBean

        if (id != other.id) return false
        if (fileName != other.fileName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + version
        return result
    }
}

typealias JsBean = com.funny.translation.database.Plugin

fun SerializableJsBean.toJsBean() = JsBean(
    id = id,
    fileName = fileName,
    code = code,
    author = author,
    version = version,
    description = description,
    enabled = enabled,
    minSupportVersion = minSupportVersion,
    maxSupportVersion = maxSupportVersion,
    targetSupportVersion = targetSupportVersion,
    isOffline = isOffline,
    debugMode = debugMode,
    supportLanguages = supportLanguages
)

fun JsBean.toSerializableJsBean() = SerializableJsBean(
    id = id,
    fileName = fileName,
    code = code,
    author = author,
    version = version,
    description = description,
    enabled = enabled,
    minSupportVersion = minSupportVersion,
    maxSupportVersion = maxSupportVersion,
    targetSupportVersion = targetSupportVersion,
    isOffline = isOffline,
    debugMode = debugMode,
    supportLanguages = supportLanguages
)

//// https://github.com/Kotlin/kotlinx.serialization/blob/v1.6.2/docs/serializers.md#deriving-external-serializer-for-another-kotlin-class-experimental
//@OptIn(ExperimentalSerializationApi::class)
//@Serializer(forClass = JsBean::class)
//object JsBeanSerializer