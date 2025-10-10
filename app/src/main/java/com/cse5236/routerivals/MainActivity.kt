package com.cse5236.routerivals

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.cse5236.routerivals.ui.FriendsScreen
import com.cse5236.routerivals.ui.HomeScreen
import com.cse5236.routerivals.ui.LeaderboardScreen
import com.cse5236.routerivals.ui.theme.RouteRivalsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("MainActivity", "onCreate")

        setContent {
            RouteRivalsTheme {
                MainApp()
            }
        }
    }

    override fun onStart() { super.onStart(); Log.d("MainActivity", "onStart") }
    override fun onResume() { super.onResume(); Log.d("MainActivity", "onResume") }
    override fun onPause() { super.onPause(); Log.d("MainActivity", "onPause") }
    override fun onStop() { super.onStop(); Log.d("MainActivity", "onStop") }
    override fun onDestroy() { super.onDestroy(); Log.d("MainActivity", "onDestroy") }
}

@Composable
fun MainApp() {
    var currentScreen by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            BottomNavBar(currentScreen) { selected ->
                currentScreen = selected
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                "home" -> HomeScreen()
                "friends" -> FriendsScreen()
                "leaderboard" -> LeaderboardScreen()
            }
        }
    }
}

@Composable
fun BottomNavBar(currentScreen: String, onTabSelected: (String) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            selected = currentScreen == "home",
            onClick = { onTabSelected("home") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Group, contentDescription = "Friends") },
            selected = currentScreen == "friends",
            onClick = { onTabSelected("friends") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Star, contentDescription = "Leaderboard") },
            selected = currentScreen == "leaderboard",
            onClick = { onTabSelected("leaderboard") }
        )
    }
}
