package com.dvhamham.manager.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.dvhamham.manager.ui.about.AboutScreen
import com.dvhamham.manager.ui.favorites.FavoritesScreen
import com.dvhamham.manager.ui.map.MapScreen
import com.dvhamham.manager.ui.map.MapViewModel
import com.dvhamham.manager.ui.permissions.PermissionsScreen
import com.dvhamham.manager.ui.settings.SettingsScreen
import com.dvhamham.manager.ui.theme.StatusBarModernDark
import androidx.compose.foundation.isSystemInDarkTheme
import com.dvhamham.manager.ui.theme.StatusBarDark
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun AppNavGraph(
    navController: NavHostController,
) {
    val mapViewModel: MapViewModel = viewModel()
    val systemUiController = rememberSystemUiController()
    val isDarkTheme = isSystemInDarkTheme()
    val statusBarColor = if (isDarkTheme) StatusBarDark else StatusBarModernDark
    SideEffect {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = !isDarkTheme
        )
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Permissions.route,
    ) {
        composable(route = Screen.About.route) {
            AboutScreen(navController = navController)
        }
        composable(route = Screen.Favorites.route) {
            FavoritesScreen(navController = navController, mapViewModel)
        }
        composable(route = Screen.Map.route) {
            MapScreen(navController = navController, mapViewModel)
        }
        composable(route = Screen.Permissions.route) {
            PermissionsScreen(navController = navController)
        }
        composable(route = Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}

@Composable
fun MainNavGraph(
    navController: NavHostController,
) {
    val mapViewModel: MapViewModel = viewModel()
    val systemUiController = rememberSystemUiController()
    val isDarkTheme = isSystemInDarkTheme()
    val statusBarColor = if (isDarkTheme) StatusBarDark else StatusBarModernDark
    SideEffect {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = !isDarkTheme
        )
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Permissions.route,
    ) {
        composable(route = Screen.Permissions.route) {
            PermissionsScreen(navController = navController)
        }
        composable(route = Screen.About.route) {
            AboutScreen(navController = navController)
        }
        composable(route = Screen.Favorites.route) {
            FavoritesScreen(navController = navController, mapViewModel)
        }
        composable(route = Screen.Map.route) {
            MapScreen(navController = navController, mapViewModel)
        }
        composable(route = Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}

@Composable
fun MainNavGraphWithBottomBar(
    navController: NavHostController,
) {
    val mapViewModel: MapViewModel = viewModel()
    val systemUiController = rememberSystemUiController()
    val isDarkTheme = isSystemInDarkTheme()
    val statusBarColor = if (isDarkTheme) StatusBarDark else StatusBarModernDark
    SideEffect {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = !isDarkTheme
        )
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Map.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = Screen.Favorites.route) {
                FavoritesScreen(navController = navController, mapViewModel)
            }
            composable(route = Screen.Map.route) {
                MapScreen(navController = navController, mapViewModel)
            }
            composable(route = Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavGraphWithBottomBarAndPermissions(
    navController: NavHostController,
) {
    val mapViewModel: MapViewModel = viewModel()
    val systemUiController = rememberSystemUiController()
    val isDarkTheme = isSystemInDarkTheme()
    val statusBarColor = if (isDarkTheme) StatusBarDark else StatusBarModernDark
    SideEffect {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = !isDarkTheme
        )
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Permissions.route,
    ) {
        composable(route = Screen.Permissions.route) {
            PermissionsScreen(navController = navController)
        }
        composable(route = Screen.About.route) {
            AboutScreen(navController = navController)
        }
        composable(route = Screen.Favorites.route) {
            Scaffold(
                bottomBar = {
                    BottomNavigationBar(navController = navController)
                }
            ) { innerPadding ->
                FavoritesScreen(navController = navController, mapViewModel, modifier = Modifier.padding(innerPadding))
            }
        }
        composable(route = Screen.Map.route) {
            Scaffold(
                bottomBar = {
                    BottomNavigationBar(navController = navController)
                }
            ) { innerPadding ->
                MapScreen(navController = navController, mapViewModel, modifier = Modifier.padding(innerPadding))
            }
        }
        composable(route = Screen.Settings.route) {
            Scaffold(
                bottomBar = {
                    BottomNavigationBar(navController = navController)
                }
            ) { innerPadding ->
                SettingsScreen(navController = navController, modifier = Modifier.padding(innerPadding))
            }
        }
    }
}
