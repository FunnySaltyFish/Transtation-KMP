package com.funny.translation.translate.ui

import com.funny.translation.strings.ResStrings

sealed class TranslateScreen(val title: String, val route: String) {
    data object MainScreen : TranslateScreen(ResStrings.nav_main, "nav_trans_main")
    data object ImageTranslateScreen: TranslateScreen(ResStrings.image_translate, "nav_trans_img")
    data object FavoriteScreen: TranslateScreen(ResStrings.favorite, "nav_trans_favorite")
    data object SettingScreen : TranslateScreen(ResStrings.nav_settings, "nav_trans_settings")
    data object PluginScreen : TranslateScreen(ResStrings.nav_plugin, "nav_trans_plugin")
    data object ThanksScreen : TranslateScreen(ResStrings.nav_thanks, "nav_thanks")
    data object AboutScreen : TranslateScreen(ResStrings.about, "nav_trans_about")
    data object OpenSourceLibScreen: TranslateScreen(ResStrings.open_source_library, "nav_trans_open_source_lib")
    data object SortResultScreen :     TranslateScreen(ResStrings.sort_result, "nav_trans_sort_result")
    data object SelectLanguageScreen : TranslateScreen(ResStrings.select_language, "nav_trans_select_language")
    data object UserProfileScreen :    TranslateScreen(ResStrings.user_profile, "nav_trans_user_profile")
    data object TransProScreen:    TranslateScreen(ResStrings.trans_pro, "nav_trans_pro")
    data object ThemeScreen : TranslateScreen(ResStrings.theme, "nav_trans_theme")
    data object FloatWindowScreen: TranslateScreen(ResStrings.float_window, "nav_trans_float_window_screen")
    data object AppRecommendationScreen: TranslateScreen(ResStrings.recommendation_app, "nav_app_recommendation")
    data object LongTextTransScreen: TranslateScreen(ResStrings.long_text_trans, "nav_long_text_trans")
    data object LongTextTransListScreen: TranslateScreen(ResStrings.long_text_trans, "nav_long_text_trans_list")
    data object LongTextTransDetailScreen: TranslateScreen(ResStrings.long_text_trans, "nav_long_text_trans_detail?id={id}")
    data object TextEditorScreen: TranslateScreen(ResStrings.edit_text, "nav_text_editor?action={action}")
    data object DraftScreen: TranslateScreen(ResStrings.drafts, "nav_drafts")
    data object ChatScreen: TranslateScreen(ResStrings.chat, "nav_chat")
    data object BuyAIPointScreen: TranslateScreen(ResStrings.buy_ai_point, "nav_buy_ai_point?planName={planName}")
    data object AnnualReportScreen : TranslateScreen(ResStrings.annual_report, "nav_annual_report")
    data object TTSSettingsScreen: TranslateScreen("TTS", "nav_tts_settings")
    data object TTSEditConfScreen: TranslateScreen("TTS", "nav_tts_edit_conf?id={id}")
    data object VoiceChatScreen: TranslateScreen("VoiceChat", "nav_voice_chat")
    data object ModelManageScreen: TranslateScreen("ModelManage", "nav_model_manage")

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