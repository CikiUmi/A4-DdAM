package com.ddam_a1.gestornotas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ddam_a1.gestornotas.ui.navigation.NotasNavHost
import com.ddam_a1.gestornotas.ui.theme.GestorNotasTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestorNotasTheme {
                NotasNavHost()
                }
            }
        }
    }