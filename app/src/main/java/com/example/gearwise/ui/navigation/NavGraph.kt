package com.example.gearwise.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gearwise.ui.screens.addedit.AddEditItemScreen
import com.example.gearwise.ui.screens.detail.ItemDetailScreen
import com.example.gearwise.ui.screens.list.ItemListScreen
import com.example.gearwise.ui.screens.settings.SettingsScreen

@Composable
fun GearWiseNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "devices") {
        composable("devices") {
            ItemListScreen(
                onItemClick = { id -> navController.navigate("device_detail/$id") },
                onAddClick = { navController.navigate("device_add") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable("device_detail/{itemId}", arguments = listOf(navArgument("itemId") { type = NavType.LongType })) { entry ->
            val id = entry.arguments?.getLong("itemId") ?: return@composable
            ItemDetailScreen(
                itemId = id,
                onBackClick = { navController.popBackStack() },
                onEditClick = { navController.navigate("device_edit/$id") },
                onDeleted = { navController.popBackStack("devices", false) }
            )
        }
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
        composable("settings") {
            SettingsScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
