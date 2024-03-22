package com.funny.translation.database

import app.cash.sqldelight.ColumnAdapter
import com.funny.translation.bean.EditablePrompt
import com.funny.translation.helper.JsonX
import com.funny.translation.translate.Language
import com.funny.translation.translate.findLanguageById

private typealias Term = Pair<String, String>

object LanguageListAdapter : ColumnAdapter<List<Language>, String> {
    override fun encode(value: List<Language>): String = JsonX.toJson(value)

    override fun decode(databaseValue: String): List<Language> = JsonX.fromJson(databaseValue)
}

object StringListAdapter : ColumnAdapter<List<String>, String> {
    override fun encode(value: List<String>): String = JsonX.toJson(value)

    override fun decode(databaseValue: String): List<String> = JsonX.fromJson(databaseValue)
}

object TermListAdapter : ColumnAdapter<List<Term>, String> {
    override fun encode(value: List<Term>): String = JsonX.toJson(value)

    override fun decode(databaseValue: String): List<Term> = JsonX.fromJson(databaseValue)
}

object EditablePromptAdapter : ColumnAdapter<EditablePrompt, String> {
    override fun encode(value: EditablePrompt): String = JsonX.toJson(value)

    override fun decode(databaseValue: String): EditablePrompt = JsonX.fromJson(databaseValue)
}

object IntListAdapter : ColumnAdapter<List<Int>, String> {
    override fun encode(value: List<Int>): String = JsonX.toJson(value)

    override fun decode(databaseValue: String): List<Int> = JsonX.fromJson(databaseValue)
}

object LanguageAdapter: ColumnAdapter<Language, Long> {
    override fun encode(value: Language) = value.id.toLong()
    override fun decode(databaseValue: Long) = findLanguageById(databaseValue.toInt())
}