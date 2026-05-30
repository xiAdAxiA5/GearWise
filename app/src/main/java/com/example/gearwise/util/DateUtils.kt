package com.example.gearwise.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val currencyFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.CHINA).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }

    /** 格式化时间戳为日期字符串 */
    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))

    /** 格式化金额: ¥1,234.56 */
    fun formatCurrency(amount: Double): String = "¥${currencyFormat.format(amount)}"

    /** 获取今天零点的毫秒时间戳 */
    fun todayTimestamp(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
