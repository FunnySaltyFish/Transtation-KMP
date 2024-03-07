package com.funny.translation.translate.ui

import com.funny.translation.kmp.strings.ResStrings

sealed class TranslateScreen(val title: String, val route: String) {
    object MainScreen : TranslateScreen(ResStrings.nav_main, "nav_trans_main")
    object ImageTranslateScreen: TranslateScreen(ResStrings.image_translate, "nav_trans_img")
    object FavoriteScreen: TranslateScreen(ResStrings.favorite, "nav_trans_favorite")
    object SettingScreen : TranslateScreen(ResStrings.nav_settings, "nav_trans_settings")
    object PluginScreen : TranslateScreen(ResStrings.nav_plugin, "nav_trans_plugin")
    object ThanksScreen : TranslateScreen(ResStrings.nav_thanks, "nav_thanks")
    object AboutScreen : TranslateScreen(ResStrings.about, "nav_trans_about")
    object OpenSourceLibScreen: TranslateScreen(ResStrings.open_source_library, "nav_trans_open_source_lib")
    object SortResultScreen :     TranslateScreen(ResStrings.sort_result, "nav_trans_sort_result")
    object SelectLanguageScreen : TranslateScreen(ResStrings.select_language, "nav_trans_select_language")
    object UserProfileScreen :    TranslateScreen(ResStrings.user_profile, "nav_trans_user_profile")
    object TransProScreen:    TranslateScreen(ResStrings.trans_pro, "nav_trans_pro")
    object ThemeScreen : TranslateScreen(ResStrings.theme, "nav_trans_theme")
    object FloatWindowScreen: TranslateScreen(ResStrings.float_window, "nav_trans_float_window_screen")
    object AppRecommendationScreen: TranslateScreen(ResStrings.recommendation_app, "nav_app_recommendation")
    object LongTextTransScreen: TranslateScreen(ResStrings.long_text_trans, "nav_long_text_trans")

    object LongTextTransListScreen: TranslateScreen(ResStrings.long_text_trans, "nav_long_text_trans_list")
    object LongTextTransDetailScreen: TranslateScreen(ResStrings.long_text_trans, "nav_long_text_trans_detail?id={id}")
    object TextEditorScreen: TranslateScreen(ResStrings.edit_text, "nav_text_editor?action={action}")
    object DraftScreen: TranslateScreen(ResStrings.drafts, "nav_drafts")
    object ChatScreen: TranslateScreen(ResStrings.chat, "nav_chat")
    object BuyAIPointScreen: TranslateScreen(ResStrings.buy_ai_point, "nav_buy_ai_point?planName={planName}")
    object AnnualReportScreen : TranslateScreen(ResStrings.annual_report, "nav_annual_report")
    object TTSSettingsScreen: TranslateScreen("TTS", "nav_tts_settings")
    object TTSEditConfScreen: TranslateScreen("TTS", "nav_tts_edit_conf?id={id}")

    companion object {

        val Saver = { screen: TranslateScreen ->
            screen.route
        }

        val Restorer = { str: String ->
            when (str) {
                MainScreen.route -> MainScreen
                SettingScreen.route -> SettingScreen
                PluginScreen.route -> PluginScreen
                ThanksScreen.route -> ThanksScreen
                else -> MainScreen
            }
        }
    }

}