package com.funny.translation.translate.bean

@JvmInline
value class FileSize(val size: Long) {

    companion object {
        fun fromBytes(bytes: Long): FileSize {
            return FileSize(bytes)
        }

        fun fromKilobytes(kilobytes: Long): FileSize {
            return FileSize(kilobytes * 1024)
        }

        fun fromMegabytes(megabytes: Long): FileSize {
            return FileSize(megabytes * 1024 * 1024)
        }

        fun fromGigabytes(gigabytes: Long): FileSize {
            return FileSize(gigabytes * 1024 * 1024 * 1024)
        }
    }

    override fun toString(): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / 1024 / 1024} MB"
            else -> "${size / 1024 / 1024 / 1024} GB"
        }
    }
}

fun Int.bytes(): FileSize {
    return FileSize.fromBytes(this.toLong())
}