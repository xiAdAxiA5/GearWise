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
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.gearwise.ui.screens.addedit.AddEditItemScreen
import com.example.gearwise.ui.screens.detail.ItemDetailScreen
import com.example.gearwise.ui.screens.habits.HabitsScreen
import com.example.gearwise.ui.screens.life.LifeScreen
import com.example.gearwise.ui.screens.list.ItemListScreen
import com.example.gearwise.ui.screens.settings.SettingsScreen
import com.example.gearwise.ui.screens.subscriptions.SubscriptionScreen

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem("devices", "设备", Icons.Outlined.Smartphone),
    BottomNavItem("life", "生活", Icons.Outlined.Cake),
    BottomNavItem("habits", "打卡", Icons.Outlined.CheckCircle),
    BottomNavItem("subscriptions", "订阅", Icons.Outlined.Subscriptions)
)

@Composable
fun GearWiseNavGraph() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

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
                            icon = { Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(22.dp)) },
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
        NavHost(navController = navController, startDestination = "devices", modifier = Modifier.padding(innerPadding)) {
            // === 设备 ===
            composable("devices") {
                ItemListScreen(
                    onItemClick = { id -> navController.navigate("device_detail/$id") },
                    onAddClick = { navController.navigate("device_add") },
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable("device_detail/{itemId}", arguments = listOf(navArgument("itemId") { type = NavType.LongType })) { entry ->
                val id = entry.arguments?.getLong("itemId") ?: return@composable
                ItemDetailScreen(itemId = id, onBackClick = { navController.popBackStack() }, onEditClick = { navController.navigate("device_edit/$id") }, onDeleted = { navController.popBackStack("devices", false) })
            }
            composable("device_add?itemId={itemId}", arguments = listOf(navArgument("itemId") { type = NavType.LongType; defaultValue = -1L })) { entry ->
                val id = entry.arguments?.getLong("itemId") ?: -1L
                AddEditItemScreen(itemId = if (id == -1L) null else id, onBackClick = { navController.popBackStack() }, onSaved = { navController.popBackStack() })
            }
            composable("device_edit/{itemId}", arguments = listOf(navArgument("itemId") { type = NavType.LongType })) { entry ->
                val id = entry.arguments?.getLong("itemId") ?: return@composable
                AddEditItemScreen(itemId = id, onBackClick = { navController.popBackStack() }, onSaved = { navController.popBackStack() })
            }

            // === 生活（计时+生日+日记）===
            composable("life") { LifeScreen() }

            // === 打卡（习惯+计划）===
            composable("habits") { HabitsScreen() }

            // === 订阅 ===
            composable("subscriptions") { SubscriptionScreen() }

            // === 设置 ===
            composable("settings") { SettingsScreen(onBackClick = { navController.popBackStack() }) }
        }
    }
}
