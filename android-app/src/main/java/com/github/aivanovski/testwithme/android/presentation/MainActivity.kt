package com.github.aivanovski.testwithme.android.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import com.arkivanov.decompose.defaultComponentContext
import com.github.aivanovski.testwithme.android.presentation.core.ThemeProviderImpl
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.AppTheme
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootScreenComponent
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val component = RootScreenComponent(
            componentContext = defaultComponentContext(),
            onExitNavigation = {
                finish()
            }
        )

        setContent {
            val themeProvider = ThemeProviderImpl(LocalContext.current)

            AppTheme(theme = themeProvider.getCurrentTheme()) {
                RootScreen(
                    rootComponent = component
                )
            }
        }
    }
}