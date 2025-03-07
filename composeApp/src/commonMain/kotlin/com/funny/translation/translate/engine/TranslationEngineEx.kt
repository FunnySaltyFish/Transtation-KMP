package com.funny.translation.translate.engine

import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.task.ModelTranslationTask

val TranslationEngine.selectKey
    get() = when (this) {
        is ModelImageTranslationEngine -> "model_${model.chatBotId}_IMG_SELECTED"
        is ImageTranslationEngine -> this.name + "_IMG_SELECTED"
        is ModelTranslationTask -> this.engineCodeName + "_SELECTED"
        else -> this.name + "_SELECTED"
    }