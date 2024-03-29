package com.funny.translation.translate.tts

import com.funny.translation.kmp.strings.ResStrings
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = GenderSerializer::class)
enum class Gender {
    Male, Female, All, Unknown;

    /**
     * 当前的gender是否包含另一个
     * @param other Gender
     * @return Boolean
     */
    fun contains(other: Gender) = when (this) {
        All -> true
        else -> this == other
    }

    val displayName
        get() = when (this) {
            Male -> ResStrings.male
            Female -> ResStrings.female
            else -> name
        }
}

// male -> Male
// female -> Female
// all -> All
// others -> Unknown
class GenderSerializer : KSerializer<Gender> {
    override val descriptor = PrimitiveSerialDescriptor("Gender", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): Gender {
        return when (decoder.decodeString()) {
            "male" -> Gender.Male
            "female" -> Gender.Female
            "all" -> Gender.All
            else -> Gender.Unknown
        }
    }

    override fun serialize(encoder: Encoder, value: Gender) {
        encoder.encodeString(value.name.lowercase())
    }
}

@Serializable
data class Speaker(
    @SerialName("full_name") val fullName: String,
    @SerialName("short_name") val shortName: String,
    @SerialName("gender") val gender: Gender,
    @SerialName("locale") val locale: String
)