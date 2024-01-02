package com.funny.translation

import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.MacAddressUtils
import java.util.UUID


// desktop JVM
private const val KEY_UUID = "uuid"
internal actual fun getDid(): String {
    return "jvm-mac-" + MacAddressUtils.getMacAddress().ifEmpty {
        "ivm-uuid-" + if (DataSaverUtils.contains(KEY_UUID)) {
            DataSaverUtils.readData(KEY_UUID, UUID.randomUUID().toString())
        } else {
            val uuid = UUID.randomUUID().toString()
            DataSaverUtils.saveData(KEY_UUID, uuid)
            uuid
        }
    }
}


internal actual fun getVersionName(): String {
    return BuildConfig.VERSION_NAME
}

internal actual fun getVersionCode(): Int {
    return BuildConfig.VERSION_CODE
}



