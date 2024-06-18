package com.github.aivanovski.testwithme.android.presentation.screens.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.github.aivanovski.testwithme.android.presentation.core.ThemeProviderImpl
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.AppTheme
import com.github.aivanovski.testwithme.android.presentation.core.navigation.ScreenComponent

@Composable
fun RootScreen(rootComponent: RootScreenComponent) {
    val themeProvider = ThemeProviderImpl(LocalContext.current)

    AppTheme(theme = themeProvider.getCurrentTheme()) {
        Children(
            stack = rootComponent.childStack
        ) { (_, component) ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides rootComponent.viewModelStoreOwner
                ) {
                    (component as ScreenComponent).render()
                }
            }
        }

    }
}
