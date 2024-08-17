package com.funny.translation.translate.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.funny.translation.translate.bean.UpdateInfo

@Preview
@Composable
fun AppUpdateDialogPreview(modifier: Modifier = Modifier) {
    AppUpdateDialog(
        modifier = modifier,
        updateInfo = UpdateInfo(
            version_code = 64,
            version_name = "2.8.1",
            apk_size = 16000000,
            update_log = """
                全新大版本来袭！
                - 新增 朗读新增 Sambert/CosyVoice 音色，实时流式合成，音色自然优美，欢迎在设置-朗读设置或长按朗读按钮配置
                - 新增 悬浮窗可以选择用什么引擎（入口：侧边栏-悬浮窗）
                - 新增 对话翻译支持上传图片
                  - 当前支持 GPT-4-Turbo、GPT-4o、Claude3.x、Gemini 1.5-*
                  - 图片同样消耗AI点数，同样按Token计费，根据不同模型、图片大小等计算实际消耗，单张图片可能在 85 ~ 1000+ Token 不等
                - 新增 对话翻译支持设置上下文长度
                  - 大模型对话实际上是一次性提交多少条消息来组成全部内容，这次更新允许手动设定最高提交多少消息。此值越大模型上下文越丰富，但相应消耗也越大
                - 新增 文本翻译结果页面支持一键返回+清空输入
                - 新增 大模型列表支持搜索
                - 优化 略微优化悬浮窗样式
                - 优化 登录注册页面去除花里胡哨的背景
                - 修复 可能修复了插件管理页面的崩溃
                - 修复 上传头像时崩溃的问题
                - 修复 主题页面未选择图片时显示异常且点击崩溃的问题
                - 修复 上个版本无法应用内更新的
                - 修复 VIP倒计时文本始终为白色的问题
                - 修复 删除收藏就崩溃的问题
            """.trimIndent(),
            apk_url = "https://api.funnysaltyfish.fun/trans/v1/app_update/get_apk?file_name=funny_translationv2.8.1_common.apk",
            force_update = false,
            should_update = true,
            apk_md5 = "b598acee140646a1e216f6d64602fe1d"
        )
    )
}