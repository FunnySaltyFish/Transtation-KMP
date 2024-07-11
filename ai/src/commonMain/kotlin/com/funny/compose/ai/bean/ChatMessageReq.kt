package com.funny.compose.ai.bean

import kotlinx.serialization.Serializable

@Serializable
sealed interface ChatMessageReq {
    val role: String
    val content: Any

    companion object {
        fun text(content: String, role: String = "user") = Text(role, content)
        fun vision(content: Vision, role: String = "user") = Vision(role, content.content)
    }

    @Serializable
    data class Text(
        override val role: String,
        override val content: String,
    ): ChatMessageReq

    /*
    "content": [
        {"type": "text", "text": "What’s in this image?"},
        {
          "type": "image_url",
          "image_url": {
            "url": "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg",
          },
        },
      ],
     */
    @Serializable
    data class Vision(
        override val role: String,
        override val content: List<Content>,
    ): ChatMessageReq {
        @Serializable
        class Content(
            val type: String,
            val text: String? = null,
            val image_url: ImageUrl? = null,
        ) {
            @Serializable
            class ImageUrl(
                val url: String,
            )
        }
    }
}