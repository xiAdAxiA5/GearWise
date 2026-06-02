package com.example.gearwise.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gearwise.ui.screens.addedit.AddEditItemScreen
import com.example.gearwise.ui.screens.detail.ItemDetailScreen
import com.example.gearwise.ui.screens.list.ItemListScreen
import com.example.gearwise.ui.screens.settings.SettingsScreen

object Routes {
    const val LIST = "list"
    const val DETAIL = "detail/{itemId}"
    const val ADD_EDIT = "addedit?itemId={itemId}"
    const val SETTINGS = "settings"

    fun detail(itemId: Long) = "detail/$itemId"
    fun addEdit(itemId: Long? = null) = if (itemId != null) "addedit?itemId=$itemId" else "addedit"
}

@Composable
fun GearWiseNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LIST
    ) {
        // 首页 - 设备列表
        composable(Routes.LIST) {
            ItemListScreen(
                onItemClick = { itemId -> navController.navigate(Routes.detail(itemId)) },
                onAddClick = { navController.navigate(Routes.addEdit()) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }

        // 设备详情
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: return@composable
            ItemDetailScreen(
                itemId = itemId,
                onBackClick = { navController.popBackStack() },
                onEditClick = { navController.navigate(Routes.addEdit(itemId)) },
                onDeleted = {
                    navController.popBackStack(Routes.LIST, false)
                }
            )
        }

        // 添加 / 编辑设备
        composable(
            route = Routes.ADD_EDIT,
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: -1L
            AddEditItemScreen(
                itemId = if (itemId == -1L) null else itemId,
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        // 设置
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
