package com.ganainy.gymmasterscompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganainy.gymmasterscompose.ui.theme.AppTheme
import com.ganainy.gymmasterscompose.ui.theme.navigation.MainScreen
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

