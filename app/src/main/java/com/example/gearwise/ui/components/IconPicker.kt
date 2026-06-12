package com.example.gearwise.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/** 预设图标库 */
data class IconOption(val name: String, val icon: ImageVector, val label: String)

val iconOptions = listOf(
    // 设备
    IconOption("smartphone", Icons.Outlined.Smartphone, "手机"),
    IconOption("laptop", Icons.Outlined.Laptop, "电脑"),
    IconOption("tablet", Icons.Outlined.TabletAndroid, "平板"),
    IconOption("headphones", Icons.Outlined.Headphones, "耳机"),
    IconOption("watch", Icons.Outlined.Watch, "手表"),
    IconOption("camera", Icons.Outlined.CameraAlt, "相机"),
    IconOption("keyboard", Icons.Outlined.Keyboard, "键盘"),
    IconOption("mouse", Icons.Outlined.Mouse, "鼠标"),
    IconOption("speaker", Icons.Outlined.Speaker, "音箱"),
    IconOption("tv", Icons.Outlined.Tv, "电视"),
    IconOption("router", Icons.Outlined.Router, "路由"),
    IconOption("devices", Icons.Outlined.DevicesOther, "其他"),

    // 生活
    IconOption("event", Icons.Outlined.Event, "事件"),
    IconOption("cake", Icons.Outlined.Cake, "生日"),
    IconOption("favorite", Icons.Outlined.Favorite, "爱心"),
    IconOption("star", Icons.Outlined.Star, "星标"),
    IconOption("flag", Icons.Outlined.Flag, "旗帜"),
    IconOption("home", Icons.Outlined.Home, "家"),
    IconOption("flight", Icons.Outlined.Flight, "飞机"),
    IconOption("directions_car", Icons.Outlined.DirectionsCar, "汽车"),
    IconOption("local_offer", Icons.Outlined.LocalOffer, "标签"),
    IconOption("palette", Icons.Outlined.Palette, "调色板"),
    IconOption("music_note", Icons.Outlined.MusicNote, "音乐"),
    IconOption("pets", Icons.Outlined.Pets, "宠物"),

    // 活动
    IconOption("check_circle", Icons.Outlined.CheckCircle, "打卡"),
    IconOption("fitness", Icons.Outlined.FitnessCenter, "健身"),
    IconOption("book", Icons.Outlined.Book, "读书"),
    IconOption("school", Icons.Outlined.School, "学习"),
    IconOption("work", Icons.Outlined.Work, "工作"),
    IconOption("brush", Icons.Outlined.Brush, "画画"),
    IconOption("restaurant", Icons.Outlined.Restaurant, "美食"),
    IconOption("local_cafe", Icons.Outlined.LocalCafe, "咖啡"),
    IconOption("sports", Icons.Outlined.SportsBasketball, "运动"),
    IconOption("self_improvement", Icons.Outlined.SelfImprovement, "冥想"),
    IconOption("water_drop", Icons.Outlined.WaterDrop, "喝水"),

    // 财务
    IconOption("subscriptions", Icons.Outlined.Subscriptions, "订阅"),
    IconOption("payments", Icons.Outlined.Payments, "支付"),
    IconOption("savings", Icons.Outlined.Savings, "储蓄"),
    IconOption("credit_card", Icons.Outlined.CreditCard, "信用卡"),
    IconOption("account_balance", Icons.Outlined.AccountBalance, "银行"),
    IconOption("receipt", Icons.Outlined.Receipt, "账单"),

    // 目标
    IconOption("emoji_events", Icons.Outlined.EmojiEvents, "奖杯"),
    IconOption("rocket", Icons.Outlined.Rocket, "火箭"),
    IconOption("trending_up", Icons.Outlined.TrendingUp, "上升"),
    IconOption("explore", Icons.Outlined.Explore, "探索"),
    IconOption("lightbulb", Icons.Outlined.Lightbulb, "灵感"),
    IconOption("edit_note", Icons.Outlined.EditNote, "日记"),
)

@Composable
fun IconPickerDialog(
    currentIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(4.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("选择图标") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(iconOptions) { option ->
                    val isSelected = option.name == currentIcon
                    Surface(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { onIconSelected(option.name) },
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(
                            0.5.dp, MaterialTheme.colorScheme.onSurface
                        ) else null
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = option.label,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}

fun getIconByName(name: String): ImageVector {
    return iconOptions.find { it.name == name }?.icon ?: Icons.Outlined.DevicesOther
}
