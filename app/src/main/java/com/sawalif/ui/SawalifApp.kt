package com.sawalif.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sawalif.ui.screens.*
import com.sawalif.ui.theme.GoldPrimary

data class NavItem(val route: String, val icon: ImageVector, val label: String)

@Composable
fun SawalifApp() {
    val navController = rememberNavController()
    val navItems = listOf(
        NavItem("home", Icons.Filled.Home, "الرئيسية"),
        NavItem("trend", Icons.Filled.TrendingUp, "الترند"),
        NavItem("explore", Icons.Filled.Explore, "استكشاف"),
        NavItem("notifications", Icons.Filled.Notifications, "الإشعارات"),
        NavItem("profile", Icons.Filled.Person, "حسابي")
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F0F1A),
                contentColor = GoldPrimary
            ) {
                navItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldPrimary,
                            selectedTextColor = GoldPrimary,
                            unselectedIconColor = Color(0xFF8A8A9A),
                            unselectedTextColor = Color(0xFF8A8A9A),
                            indicatorColor = Color(0xFF1A1A2E)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { HomeScreen() }
            composable("trend") { TrendScreen() }
            composable("explore") { ExploreScreen() }
            composable("notifications") { NotificationsScreen() }
            composable("profile") { ProfileScreen() }
        }
    }
}
