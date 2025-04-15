package com.it342.chatifying

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.it342.chatifying.ui.theme.ChatifyingTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Only enable edge-to-edge if you want transparent system bars
        // enableEdgeToEdge()

        setContent {
            ChatifyingTheme { // This uses your Compose theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Your main app content
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    // Add your navigation system here
    Text("Welcome, Baby")
}