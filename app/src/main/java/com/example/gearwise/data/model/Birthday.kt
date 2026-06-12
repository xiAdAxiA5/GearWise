package com.example.gearwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.gearwise.util.ZodiacUtils

@Entity(tableName = "birthdays")
data class Birthday(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String = "cake",
    val birthDate: Long,               // epoch millis
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    /** 周岁年龄 */
    val age: Int get() = ZodiacUtils.calculateAge(birthDate)

    /** 生肖 */
    val zodiac: String get() = ZodiacUtils.chineseZodiac(birthDate)

    /** 星座 */
    val constellation: String get() = ZodiacUtils.westernConstellation(birthDate)

    /** 距离下次生日还有多少天 */
    val daysUntilNextBirthday: Long get() = ZodiacUtils.daysUntilNextBirthday(birthDate)
}
