package com.sawalif

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sawalif.data.models.UserRole
import com.sawalif.data.repository.FirebaseRepository
import com.sawalif.ui.screens.*
import com.sawalif.ui.theme.SawalifTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SawalifTheme {
                SawalifApp()
            }
        }
    }
}

@Composable
fun SawalifApp() {
    val navController = rememberNavController()
    val startDestination = if (FirebaseRepository.isLoggedIn()) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable("home") {
            HomeScreen(
                navController = navController,
                onLogout = {
                    FirebaseRepository.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("compose") {
            ComposeScreen(navController = navController)
        }

        composable("comments/{postId}") { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            CommentsScreen(postId = postId, navController = navController)
        }

        composable("profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ProfileScreen(userId = userId, navController = navController)
        }

        composable("chat/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ChatScreen(otherUserId = userId, navController = navController)
        }

        composable("admin") {
            AdminScreen(navController = navController)
        }
    }
}
