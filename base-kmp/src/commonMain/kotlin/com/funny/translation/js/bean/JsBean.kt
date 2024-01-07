package com.funny.translation.js.bean

//@kotlinx.serialization.Serializable
//data class JsBean(
//    var id : Int = 0,
//    var fileName : String = "Plugin",
//    var code : String = "",
//    var author : String = "Author",
//    var version : Int = 1,
//    var description : String = "",
//    var enabled : Int = 1,
//    var minSupportVersion : Int = 2,
//    var maxSupportVersion : Int = 999, // 自 version 4 起弃用
//    var targetSupportVersion : Int = 4,
//    var isOffline : Boolean = false,
//    var debugMode : Boolean = true,
//    var supportLanguages: List<Language> = arrayListOf()
//){
//    fun toSQL() = """
//        insert into table_js($fileName,$code,$author,$version,$description,$enabled,
//        $minSupportVersion,$maxSupportVersion,$isOffline,$debugMode)
//        )
//    """.trimIndent()
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as JsBean
//
//        if (id != other.id) return false
//        if (fileName != other.fileName) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = fileName.hashCode()
//        result = 31 * result + code.hashCode()
//        result = 31 * result + version
//        return result
//    }
//}

typealias JsBean = com.funny.translation.database.Plugin