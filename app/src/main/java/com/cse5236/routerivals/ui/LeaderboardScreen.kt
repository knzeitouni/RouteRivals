package com.cse5236.routerivals.ui

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun LeaderboardScreen() {
    LaunchedEffect(Unit) {
        Log.d("LeaderboardScreen", "Launched (onCreate equivalent)")
    }

    Text(text = "Leaderboard Screen")
}
