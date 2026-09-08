package com.ddam_a1.gestorrecordatorios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ddam_a1.gestorrecordatorios.ui.navigation.RecordatoriosNavHost
import com.ddam_a1.gestorrecordatorios.ui.theme.GestorRecordatoriosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestorRecordatoriosTheme {
                RecordatoriosNavHost()
                }
            }
        }
    }