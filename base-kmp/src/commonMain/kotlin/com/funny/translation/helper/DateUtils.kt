package com.funny.translation.helper

import java.util.Calendar
import java.util.TimeZone

data class FunnyDate(var year: Int = 0, var month: Int = 0, var day: Int = 0)
object DateUtils {
    const val TAG = "DateUtils"
    val isSpringFestival: Boolean by lazy{
        val time = today
        Log.d(TAG, "isSpringFestival: today is $time")
        when (time.year) {
            2022 -> when (time.month) {
                1 -> time.day == 31
                2 -> time.day in 1..7
                else -> false
            }
            2023 -> when (time.month) {
                1 -> time.day in 21..28
                else -> false
            }
            2024 -> when (time.month) {
                2 -> time.day in 9..16
                else -> false
            }
            2025 -> when (time.month) {
                1 -> time.day in 28..31
                2 -> time.day in 1..4
                else -> false
            }
            2026 -> when (time.month) {
                2 -> time.day in 16..23
                else -> false
            }
            else -> false
        }
    }

    private val today = FunnyDate().apply {
        val date = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"))
        day = date.get(Calendar.DATE)
        month = date.get(Calendar.MONTH) + 1
        year = date.get(Calendar.YEAR)
    }
}