package com.example.nuehue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nuehue.presentation.pairing.AlreadyPairedScreen
import com.example.nuehue.presentation.pairing.PairingScreen
import com.example.nuehue.presentation.pairing.PairingViewModel
import com.example.nuehue.presentation.ui.theme.NueHueTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NueHueTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: PairingViewModel = hiltViewModel()
) {
    val credentialsState by viewModel.credentialsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCredentials()
    }

    when (val state = credentialsState) {
        is com.example.nuehue.domain.model.Resource.Success -> {
            if (state.data != null) {
                AlreadyPairedScreen()
            } else {
                PairingScreen()
            }
        }
        is com.example.nuehue.domain.model.Resource.Loading -> {
            androidx.compose.material3.CircularProgressIndicator()
        }
        is com.example.nuehue.domain.model.Resource.Error -> {
            PairingScreen()
        }
        null -> {
            androidx.compose.material3.CircularProgressIndicator()
        }
    }
}
