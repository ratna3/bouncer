package com.example.bouncer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.bouncer.data.local.CredentialStore
import com.example.bouncer.theme.BouncerTheme
import com.example.bouncer.ui.DeviceListScreen
import com.example.bouncer.ui.LoginSetupScreen

enum class AppScreen {
    LOGIN_SETUP,
    DEVICE_LIST
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BouncerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BouncerApp()
                }
            }
        }
    }
}

@Composable
fun BouncerApp() {
    val context = LocalContext.current
    val credentialStore = remember { CredentialStore(context) }

    var currentScreen by remember {
        mutableStateOf(
            if (credentialStore.hasCredentials()) AppScreen.DEVICE_LIST else AppScreen.LOGIN_SETUP
        )
    }

    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
        when (screen) {
            AppScreen.LOGIN_SETUP -> {
                LoginSetupScreen(
                    onSaved = {
                        currentScreen = AppScreen.DEVICE_LIST
                    }
                )
            }
            AppScreen.DEVICE_LIST -> {
                DeviceListScreen(
                    onChangeCredentials = {
                        currentScreen = AppScreen.LOGIN_SETUP
                    }
                )
            }
        }
    }
}
