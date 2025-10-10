package com.cse5236.routerivals.ui

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun HomeScreen() {
    LaunchedEffect(Unit) {
        Log.d("HomeScreen", "Launched (onCreate equivalent)")
    }

    Text(text = "Home Screen")
}
