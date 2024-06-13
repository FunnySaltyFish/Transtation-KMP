package com.funny.translation.translate.utils

import com.funny.translation.js.core.JsTranslateTaskText
import com.funny.translation.translate.Language
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.engine.TextTranslationEngines
import com.funny.translation.translate.task.ModelTranslationTask

object TranslateUtils {
    fun createTask(translationEngine: TranslationEngine, actualTransText:String, sourceLanguage: Language, targetLanguage: Language) =
        if (translationEngine is TextTranslationEngines) {
            translationEngine.createTask(actualTransText, sourceLanguage, targetLanguage)
        } else if (translationEngine is ModelTranslationTask) {
            val modelTranslationTask = translationEngine as ModelTranslationTask
            modelTranslationTask.sourceString = actualTransText
            modelTranslationTask.sourceLanguage = sourceLanguage
            modelTranslationTask.targetLanguage = targetLanguage
            modelTranslationTask
        } else{
            val jsTask = translationEngine as JsTranslateTaskText
            jsTask.sourceString = actualTransText
            jsTask.sourceLanguage = sourceLanguage
            jsTask.targetLanguage = targetLanguage
            jsTask
        }
}