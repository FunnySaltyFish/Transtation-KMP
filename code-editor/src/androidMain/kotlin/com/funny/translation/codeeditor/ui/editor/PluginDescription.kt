package com.funny.translation.codeeditor.ui.editor

import com.funny.translation.network.OkHttpUtils
import com.funny.translation.translate.Language
import io.github.rosemoe.editor.langs.desc.JavaScriptDescription
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberFunctions

val FunnyPluginDescription = PluginDescription()

class PluginDescription : JavaScriptDescription() {

    override fun getKeywords(): Array<String> {
        val originKeywords = super.getKeywords().toMutableList()
        for (language in Language.entries){
            originKeywords.add("LANGUAGE_${language.name}")
        }
        originKeywords.add("funny")
        originKeywords.add("http")
        originKeywords.add("BASE_URL")
        originKeywords.addAll(arrayOf("sourceString", "targetString", "log"))
        val clazz = OkHttpUtils::class
        val set = mutableSetOf<String>()
        clazz.declaredMemberFunctions.filter { it.visibility == KVisibility.PUBLIC }.forEach {
            set.add(it.name)
        }
        originKeywords.addAll(set)
        return originKeywords.toTypedArray()
    }
}