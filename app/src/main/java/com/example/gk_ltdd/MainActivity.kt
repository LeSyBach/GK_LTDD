package com.example.gk_ltdd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gk_ltdd.ui.screens.*
import com.example.gk_ltdd.ui.theme.GK_LTDDTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GK_LTDDTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser != null) "user_list" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("user_list") {
            UserListScreen(navController = navController)
        }
        composable("add_user") {
            AddEditUserScreen(navController = navController, userId = null)
        }
        composable("edit_user/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            AddEditUserScreen(navController = navController, userId = userId)
        }
    }
}