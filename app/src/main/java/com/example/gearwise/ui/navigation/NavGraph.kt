package com.example.gearwise.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.gearwise.ui.screens.addedit.AddEditItemScreen
import com.example.gearwise.ui.screens.birthdays.BirthdayScreen
import com.example.gearwise.ui.screens.countdown.CountdownScreen
import com.example.gearwise.ui.screens.detail.ItemDetailScreen
import com.example.gearwise.ui.screens.diary.DiaryScreen
import com.example.gearwise.ui.screens.habits.HabitScreen
import com.example.gearwise.ui.screens.list.ItemListScreen
import com.example.gearwise.ui.screens.plans.PlanScreen
import com.example.gearwise.ui.screens.settings.SettingsScreen
import com.example.gearwise.ui.screens.subscriptions.SubscriptionScreen

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("devices", "设备", Icons.Outlined.Smartphone),
    BottomNavItem("countdown", "计时", Icons.Outlined.Schedule),
    BottomNavItem("birthdays", "生日", Icons.Outlined.Cake),
    BottomNavItem("habits", "打卡", Icons.Outlined.CheckCircle),
    BottomNavItem("diary", "日记", Icons.Outlined.EditNote),
    BottomNavItem("subscriptions", "订阅", Icons.Outlined.Subscriptions),
    BottomNavItem("plans", "计划", Icons.Outlined.Flag)
)

@Composable
fun GearWiseNavGraph() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route } || currentRoute == "settings"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(20.dp)) },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Visible) },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo("devices") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "devices",
            modifier = Modifier.padding(innerPadding)
        ) {
            // === 设备列表 ===
            composable("devices") {
                ItemListScreen(
                    onItemClick = { id -> navController.navigate("device_detail/$id") },
                    onAddClick = { navController.navigate("device_add") },
                    onSettingsClick = { navController.navigate("settings") }
                )
            }

            // === 设备详情 ===
            composable("device_detail/{itemId}", arguments = listOf(navArgument("itemId") { type = NavType.LongType })) { entry ->
                val id = entry.arguments?.getLong("itemId") ?: return@composable
                ItemDetailScreen(
                    itemId = id,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { navController.navigate("device_edit/$id") },
                    onDeleted = { navController.popBackStack("devices", false) }
                )
            }

            // === 设备添加/编辑 ===
            composable("device_add?itemId={itemId}", arguments = listOf(navArgument("itemId") { type = NavType.LongType; defaultValue = -1L })) { entry ->
                val id = entry.arguments?.getLong("itemId") ?: -1L
                AddEditItemScreen(
                    itemId = if (id == -1L) null else id,
                    onBackClick = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable("device_edit/{itemId}", arguments = listOf(navArgument("itemId") { type = NavType.LongType })) { entry ->
                val id = entry.arguments?.getLong("itemId") ?: return@composable
                AddEditItemScreen(
                    itemId = id,
                    onBackClick = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            // === 计时 ===
            composable("countdown") { CountdownScreen() }

            // === 生日 ===
            composable("birthdays") { BirthdayScreen() }

            // === 打卡 ===
            composable("habits") { HabitScreen() }

            // === 日记 ===
            composable("diary") { DiaryScreen() }

            // === 订阅 ===
            composable("subscriptions") { SubscriptionScreen() }

            // === 计划 ===
            composable("plans") { PlanScreen() }

            // === 设置 ===
            composable("settings") { SettingsScreen(onBackClick = { navController.popBackStack() }) }
        }
    }
}
