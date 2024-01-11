package com.funny.translation.translate.utils

import com.funny.translation.kmp.KMPContext


expect object QQUtils {
    /****************
     *
     * 发起添加群流程。群号：FunnyTranslation内测交流(857362450) 的 key 为： mlEwPbkeUQMuwoyp44lROPeD938exo56
     * 调用 joinQQGroup(mlEwPbkeUQMuwoyp44lROPeD938exo56) 即可发起手Q客户端申请加群 FunnyTranslation内测交流(857362450)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回false表示呼起失败
     */
    fun joinQQGroup(context: KMPContext, key: String = "mlEwPbkeUQMuwoyp44lROPeD938exo56"): Boolean
}