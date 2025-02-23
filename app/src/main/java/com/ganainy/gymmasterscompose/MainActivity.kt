package com.ganainy.gymmasterscompose

import MainScreen
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganainy.gymmasterscompose.ui.AppTheme
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Enable strict mode
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        setContent {
            AppTheme {
                // Obtain the ViewModel instance using Hilt
                val viewModel: MainViewModel = hiltViewModel()
                // Collect the login status state from the ViewModel
                val authState by viewModel.authState.collectAsState()
                // Pass the login status to the MainScreen composable
                MainScreen(authState = authState)
            }
        }
    }
}