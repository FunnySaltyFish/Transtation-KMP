package com.funny.translation.translate.utils

import androidx.compose.ui.geometry.Offset
import com.eygraber.uri.Uri
import com.funny.compose.ai.bean.ChatMemory
import com.funny.data_saver.core.DataSaverConverter
import com.funny.trans.login.ui.AICostSortType
import com.funny.translation.bean.AppLanguage
import com.funny.translation.bean.EditablePrompt
import com.funny.translation.bean.UserInfoBean
import com.funny.translation.helper.JsonX
import com.funny.translation.translate.Language
import com.funny.translation.translate.tts.TTSExtraConf
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.translate.ui.thanks.SponsorSortType
import com.funny.translation.ui.theme.LightDarkMode
import com.funny.translation.ui.theme.ThemeType

fun initTypeConverters() {
    // For ComposeDataSaver
    DataSaverConverter.registerTypeConverters<UserInfoBean>(
        save = {
            JsonX.toJson(it)
        },
        restore = {
            // 修复旧版本 vip_start_time 为 Long 的问题
            try {
                val matchResult = OLD_VIP_START_TIME.find(it)
                if (matchResult != null)
                    JsonX.fromJson(it.replace(OLD_VIP_START_TIME, "$1:null"), UserInfoBean::class)
                else
                    JsonX.fromJson(it, UserInfoBean::class)
            } catch (e: Exception) {
                e.printStackTrace()
                UserInfoBean()
            }
        }
    )
//
    DataSaverConverter.registerTypeConverters<SponsorSortType>(
        save = { it.name },
        restore = { SponsorSortType.valueOf(it) }
    )

    DataSaverConverter.registerTypeConverters<Pair<String, Int>>(
        save = { "${it.first}:${it.second}" },
        restore = { val split = it.split(":"); Pair(split[0], split[1].toInt()) }
    )

    DataSaverConverter.registerTypeConverters<Language>(
        save = { it.name },
        restore = { Language.valueOf(it) }
    )

    DataSaverConverter.registerTypeConverters<Offset>(
        save = { "${it.x},${it.y}" },
        restore = { val split = it.split(",").map { it.toFloat() }; Offset(split[0], split[1]) }
    )

    DataSaverConverter.registerTypeConverters<Uri?>(
        save = { it.toString() },
        restore = { if (it == "null") null else Uri.parse(it) }
    )

    DataSaverConverter.registerTypeConverters<AppLanguage>(
        save = { it.ordinal.toString() },
        restore = { AppLanguage.entries[it.toInt()] }
    )

    DataSaverConverter.registerTypeConverters<ThemeType>(
        save = ThemeType.Saver,
        restore = ThemeType.Restorer
    )
    DataSaverConverter.registerTypeConverters<TranslateScreen>(
        save = TranslateScreen.Saver,
        restore = TranslateScreen.Restorer
    )
    DataSaverConverter.registerTypeConverters<ChatMemory>(
        save = ChatMemory.Saver,
        restore = ChatMemory.Restorer
    )
    DataSaverConverter.registerTypeConverters<EditablePrompt>(
        save = { JsonX.toJson(it) },
        restore = { JsonX.fromJson(it) })
    DataSaverConverter.registerTypeConverters<LightDarkMode>(
        save = { it.name },
        restore = { LightDarkMode.valueOf(it) })

    DataSaverConverter.registerTypeConverters<TTSExtraConf>(
        save = { JsonX.toJson(it) },
        restore = { JsonX.fromJson(it) }
    )

    // MutableList<String>
    DataSaverConverter.registerTypeConverters<MutableList<String>>(
        save = { JsonX.toJson(it) },
        restore = { JsonX.fromJson(it) }
    )

    // CostSortType
    DataSaverConverter.registerTypeConverters<AICostSortType>(
        save = { it.name },
        restore = { AICostSortType.valueOf(it) }
    )

    DataSaverConverter.registerTypeConverters<Int?>(
        save = { it.toString() },
        restore = { if (it == "null") null else it.toInt() }
    )
}

private val OLD_VIP_START_TIME = """("vip_start_time"):(-?\d+)""".toRegex()