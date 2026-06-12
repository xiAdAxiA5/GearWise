package com.example.gearwise.util

import java.util.*
import java.util.concurrent.TimeUnit

object ZodiacUtils {

    private val zodiacAnimals = listOf("猴", "鸡", "狗", "猪", "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊")

    /** 生肖 */
    fun chineseZodiac(birthTimestamp: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = birthTimestamp }
        return zodiacAnimals[cal.get(Calendar.YEAR) % 12]
    }

    /** 周岁年龄 */
    fun calculateAge(birthTimestamp: Long): Int {
        val birthCal = Calendar.getInstance().apply { timeInMillis = birthTimestamp }
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) age--
        return age
    }

    /** 星座 */
    fun westernConstellation(birthTimestamp: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = birthTimestamp }
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return when {
            (month == 3 && day >= 21) || (month == 4 && day <= 19) -> "白羊座"
            (month == 4 && day >= 20) || (month == 5 && day <= 20) -> "金牛座"
            (month == 5 && day >= 21) || (month == 6 && day <= 20) -> "双子座"
            (month == 6 && day >= 21) || (month == 7 && day <= 22) -> "巨蟹座"
            (month == 7 && day >= 23) || (month == 8 && day <= 22) -> "狮子座"
            (month == 8 && day >= 23) || (month == 9 && day <= 22) -> "处女座"
            (month == 9 && day >= 23) || (month == 10 && day <= 22) -> "天秤座"
            (month == 10 && day >= 23) || (month == 11 && day <= 21) -> "天蝎座"
            (month == 11 && day >= 22) || (month == 12 && day <= 21) -> "射手座"
            (month == 12 && day >= 22) || (month == 1 && day <= 19) -> "摩羯座"
            (month == 1 && day >= 20) || (month == 2 && day <= 18) -> "水瓶座"
            else -> "双鱼座"
        }
    }

    /** 距离下次生日天数 */
    fun daysUntilNextBirthday(birthTimestamp: Long): Long {
        val birthCal = Calendar.getInstance().apply { timeInMillis = birthTimestamp }
        val today = Calendar.getInstance()
        val nextBirthday = Calendar.getInstance().apply {
            set(Calendar.MONTH, birthCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, birthCal.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (nextBirthday.before(today)) {
            nextBirthday.add(Calendar.YEAR, 1)
        }
        return TimeUnit.MILLISECONDS.toDays(nextBirthday.timeInMillis - today.timeInMillis)
    }
}
