package com.funny.translation.helper

import java.net.NetworkInterface
import java.util.Enumeration

object MacAddressUtils {
    fun getMacAddress(): String {
        return try {
            val networkInterfaces: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface: NetworkInterface = networkInterfaces.nextElement()
                if (networkInterface.isLoopback || networkInterface.isVirtual) {
                    continue
                }
                val macAddressBytes: ByteArray = networkInterface.hardwareAddress ?: continue
                val macAddressStringBuilder = StringBuilder()
                for (b in macAddressBytes) {
                    macAddressStringBuilder.append(String.format("%02X", b))
                    macAddressStringBuilder.append(":")
                }
                if (macAddressStringBuilder.isNotEmpty()) {
                    macAddressStringBuilder.deleteCharAt(macAddressStringBuilder.length - 1)
                }
                return macAddressStringBuilder.toString()
            }
            ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}