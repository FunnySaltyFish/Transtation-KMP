package com.funny.translation.helper

import com.eygraber.uri.Uri
import com.funny.translation.AppConfig
import com.funny.translation.kmp.KMPContext
import com.funny.translation.network.ServiceCreator
import com.funny.translation.network.api
import com.funny.translation.network.apiNoCall
import com.funny.translation.network.service.UserService


object UserUtils {
    private const val TAG = "UserUtils"
    // 头像最高尺寸
    internal const val TARGET_AVATAR_SIZE = 256

    val userService by lazy(LazyThreadSafetyMode.PUBLICATION){
        ServiceCreator.create(UserService::class.java)
    }

    private val VALID_POSTFIX = arrayOf("163.com", "qq.com", "gmail.com", "126.com", "sina.com", "sohu.com", "hotmail.com", "yahoo.com", "foxmail.com", "funnysaltyfish.fun", "outlook.com")

    fun isValidUsername(username: String): Boolean {
        return "^[\\w\\u4e00-\\u9fff]{3,16}\$".toRegex().matches(username)
    }

    fun isValidEmail(email: String): Boolean {
        // 只允许主流邮箱后缀
        if (VALID_POSTFIX.all { !email.endsWith(it) }) {
            return false
        }
        return "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*\$".toRegex().matches(email)
    }

    // 密码校验
    // 长度8-16位，包含且必须至少包含大小写字母和数字，可以包含 [].#*
    fun isValidPassword(password: String): Boolean {
        if (password.length < 8 || password.length > 16) {
            return false
        }
        return "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d\\[\\]\\.#*]{8,16}\$".toRegex().matches(password)
    }

    // 邀请码，8 位，字母加数字
    fun isValidInviteCode(inviteCode: String): Boolean {
        return "^[a-zA-Z0-9]{8}\$".toRegex().matches(inviteCode)
    }

    fun anonymousEmail(email: String): String {
        val arr = email.split("@")
        if (arr.size == 2){
            val prefix = arr[0]
            val suffix = arr[1]
            return anonymousString(prefix) + "@" + suffix
        }
        return anonymousString(email)
    }

    private fun anonymousString(str: String): String {
        return if (str.length > 3){
            str.take(3) + "***" + str.takeLast(3)
        } else {
            "$str***"
        }
    }


    /**
     * 注册
     * @param username String
     * @param password String 密码格式： did#encrypted_info#iv
     * @param password_type String
     * @param email String
     * @return UserBean?
     */
    suspend fun login(
        username: String,
        // 密码格式： did#encrypted_info#iv
        password: String,
        passwordType: String,
        email: String,
        verifyCode: String
    ) = api(userService::login, username, password, passwordType, email, "", verifyCode, AppConfig.androidId)

    suspend fun register(
        username: String,
        password: String,
        passwordType: String,
        email: String,
        verifyCode: String,
        phone: String,
        inviteCode: String,
        onSuccess: SimpleAction,
    ): Unit? {
        val checkEmail = apiNoCall(userService::verifyEmail, email, verifyCode) {
            fail {
                throw SignUpException(it.displayErrorMsg)
            }
        }
        checkEmail.call(true)

        return api(userService::register, username, password, passwordType, email, phone, inviteCode, rethrowErr = true) {
            addSuccess {
                onSuccess()
            }
            fail {
                throw SignUpException(it.displayErrorMsg)
            }
        }
    }
}

expect suspend fun UserUtils.uploadUserAvatar(context: KMPContext, imgUri: Uri, filename: String, width: Int, height: Int, uid: Int) : String

class SignInException(s: String) : Exception(s)
class SignUpException(s: String) : Exception(s)
