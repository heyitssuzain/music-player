package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.AuraApp
import com.example.ui.AuraViewModel
import com.example.ui.theme.AuraTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AuraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuraTheme {
                AuraApp(viewModel = viewModel)
            }
        }
    }
}
