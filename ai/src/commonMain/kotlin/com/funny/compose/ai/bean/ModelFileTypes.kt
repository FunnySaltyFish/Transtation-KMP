package com.funny.compose.ai.bean

import kotlinx.serialization.Serializable

/**
 * 模型支持的文件列表
 * @property text Boolean 是否支持文本
 * @property imageMimeList List<String> 支持的图片后缀
 * @property maxImageNum Int 最大图片数量
 * @property maxSingleImageSize Int 单张图片最大大小
 * @property fileSuffixes List<String> 额外支持的文件后缀
 * @constructor
 */
@Serializable
data class ModelFileTypes(
    val text: Boolean = true,
    val imageMimeList: Array<String> = emptyArray(),
    val maxImageNum: Int = 0,
    val maxSingleImageSize: Int = 10 * 1024 * 1024,
    val maxTotalFileSize: Int = 100 * 1024 * 1024,
    val fileSuffixes: List<String> = emptyList(),
) {
//    val supportText get() = value and TYPE_TEXT != 0
//    val supportImage get() = value and TYPE_IMAGE != 0
    val supportImage = imageMimeList.isNotEmpty()

    companion object {
//        val NONE = ModelFileTypes(0)
        val TEXT = ModelFileTypes()
        val IMAGE = ModelFileTypes(imageMimeList = arrayOf("image/*"), maxImageNum = 5)
//        val ALL = ModelFileTypes(TYPE_TEXT or TYPE_IMAGE)
    }
}

