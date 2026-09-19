package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.data.PreferencesManager
import com.example.ui.home.HomeScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.theme.KlassTheme
import com.example.ui.theme.MyApplicationTheme

enum class KlassScreen {
    SPLASH,
    ONBOARDING,
    HOME
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KlassTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KlassApp()
                }
            }
        }
    }
}

@Composable
fun KlassApp() {
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager(context) }
    var currentScreen by remember { mutableStateOf(KlassScreen.SPLASH) }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            KlassScreen.SPLASH -> {
                SplashScreen(
                    onSplashFinished = {
                        currentScreen = if (!prefsManager.hasCompletedOnboarding) {
                            KlassScreen.ONBOARDING
                        } else {
                            KlassScreen.HOME
                        }
                    }
                )
            }
            KlassScreen.ONBOARDING -> {
                OnboardingScreen(
                    onFinished = {
                        prefsManager.hasCompletedOnboarding = true
                        currentScreen = KlassScreen.HOME
                    }
                )
            }
            KlassScreen.HOME -> {
                HomeScreen(
                    onReplayTour = {
                        currentScreen = KlassScreen.ONBOARDING
                    }
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Klass") }
}

