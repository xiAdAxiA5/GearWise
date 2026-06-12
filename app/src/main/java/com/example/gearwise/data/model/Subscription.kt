package com.example.gearwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.concurrent.TimeUnit

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String = "subscriptions",
    val appName: String = "",
    val price: Double = 0.0,
    val billingCycle: String = "monthly", // "weekly"|"monthly"|"yearly"|"custom"
    val startDate: Long,                   // epoch millis
    val nextBillingDate: Long,             // epoch millis
    val isActive: Boolean = true,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    /** 日均可乐成本 = price / days_in_cycle */
    val dailyCost: Double
        get() {
            val cycleDays = when (billingCycle) {
                "weekly" -> 7
                "yearly" -> 365
                "custom" -> 30
                else -> 30 // monthly default
            }
            return if (cycleDays > 0) price / cycleDays else price
        }

    /** 月均成本 */
    val monthlyCost: Double get() = dailyCost * 30
}
