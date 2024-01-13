package com.funny.translation.translate.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log

/**
 * @author MrLiu
 * @date 2020/5/15
 * desc 广播接收者
 */
class NetworkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
        // 特殊注意：如果if条件生效，那么证明当前是有连接wifi或移动网络的，如果有业务逻辑最好把else场景酌情考虑进去！
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            setNetworkState(context)
        }
    }

    companion object {
        private var WIFI_TIME: Long = 0
        private var ETHERNET_TIME: Long = 0
        private var NONE_TIME: Long = 0
        var networkType = -3
            private set
        private const val TAG = "TAG"
        fun setNetworkState(context: Context) {
            val time = System.currentTimeMillis()
            if (time != WIFI_TIME && time != ETHERNET_TIME && time != NONE_TIME) {
                val netWorkState = getNetWorkState(context)
                Log.d(TAG, "onReceive: networkState : $netWorkState")
                if (netWorkState == 0 && networkType != 0) {
                    WIFI_TIME = time
                    networkType = netWorkState
                    Log.e(TAG, "wifi：$time")
                } else if (netWorkState == 1 && networkType != 1) {
                    ETHERNET_TIME = time
                    networkType = netWorkState
                    Log.e(TAG, "数据网络：$time")
                } else if (netWorkState == -1 && networkType != -1) {
                    NONE_TIME = time
                    networkType = netWorkState
                    Log.e(TAG, "无网络：$time")
                }
            }
        }

        private const val NETWORK_NONE = -1 //无网络连接
        private const val NETWORK_WIFI = 0 //wifi
        private const val NETWORK_MOBILE = 1 //数据网络

        //判断网络状态与类型
        fun getNetWorkState(context: Context): Int {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                if (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) {
                    return NETWORK_WIFI
                } else if (activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                    return NETWORK_MOBILE
                }
            } else {
                return NETWORK_NONE
            }
            return NETWORK_NONE
        }
    }
}
