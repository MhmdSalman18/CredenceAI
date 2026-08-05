package com.credenceai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.credenceai.app.presentation.ui.ThemeViewModel
import com.credenceai.app.presentation.ui.screens.MainScreen
import com.credenceai.app.ui.theme.CredenceAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()

            CredenceAITheme(darkTheme = isDarkMode) {
                MainScreen()
            }
        }
    }
}

