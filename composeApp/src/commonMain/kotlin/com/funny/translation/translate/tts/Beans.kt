package com.funny.translation.translate.tts

import com.funny.translation.strings.ResStrings
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = GenderSerializer::class)
enum class Gender(
    private val value: Int
) {
    // Male:   0001
    // Female: 0010
    // All:    0011
    Male(1), Female(2), All(3);

    /**
     * 当前的gender是否包含另一个
     * @param other Gender
     * @return Boolean
     */
    fun contains(other: Gender) = (value and other.value) == other.value

    operator fun plus(other: Gender) =
        entries.find { it.value == (this.value or other.value) } ?: All

    operator fun minus(other: Gender) =
        entries.find { it.value == (this.value and other.value.inv()) } ?: All

    val displayName
        get() = when (this) {
            Male -> ResStrings.male
            Female -> ResStrings.female
            else -> name
        }

    override fun toString(): String {
        // Retrofit 会调用 toString() 方法，所以这里返回的是小写
        return name.lowercase()
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
            else -> Gender.All
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