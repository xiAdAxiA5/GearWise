package com.example.gearwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.concurrent.TimeUnit

@Entity(tableName = "electronic_items")
data class ElectronicItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,       // "手机" | "电脑" | "平板" | "耳机" | "其他"
    val brand: String,
    val model: String,
    val purchaseDate: Long,     // epoch millis, truncated to start of day
    val purchasePrice: Double,  // 购买价格 (yuan)
    val accessoryCost: Double = 0.0,  // 配件支出
    val repairCost: Double = 0.0,     // 维修支出
    val isSold: Boolean = false,
    val soldDate: Long? = null,       // epoch millis, truncated to start of day
    val soldPrice: Double? = null,    // 出售价格 (yuan)
    val notes: String = ""
) {
    /**
     * 已持有天数：
     * - 已出售：出售日期 - 购买日期
     * - 未出售：今天 - 购买日期
     * 最小返回 1，避免除以零
     */
    val daysHeld: Long
        get() {
            val endDate = if (isSold && soldDate != null) soldDate else System.currentTimeMillis()
            val days = TimeUnit.MILLISECONDS.toDays(endDate - purchaseDate)
            return days.coerceAtLeast(1)
        }

    /**
     * 实际持有成本：
     * - 已出售：购买价格 + 配件支出 + 维修支出 - 出售价格
     * - 未出售：购买价格 + 配件支出 + 维修支出
     */
    val totalCost: Double
        get() {
            val base = purchasePrice + accessoryCost + repairCost
            return if (isSold && soldPrice != null) base - soldPrice else base
        }

    /**
     * 日均使用成本 = 实际持有成本 / 持有天数
     */
    val dailyCost: Double
        get() = totalCost / daysHeld

    /**
     * 月均使用成本 = 日均成本 × 30
     */
    val monthlyCost: Double
        get() = dailyCost * 30
}
