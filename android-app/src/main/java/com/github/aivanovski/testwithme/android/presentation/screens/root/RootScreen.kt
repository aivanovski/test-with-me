package com.github.aivanovski.testwithme.android.presentation.screens.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.github.aivanovski.testwithme.android.presentation.core.ThemeProviderImpl
import com.github.aivanovski.testwithme.android.presentation.core.compose.ThemedScreenPreview
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.AppTheme
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.LightTheme
import com.github.aivanovski.testwithme.android.presentation.core.navigation.ScreenComponent
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.RootState

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RootScreen(
    state: RootState,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.theme.materialColors.primaryContainer,
                    titleContentColor = AppTheme.theme.materialColors.primary,
                ),
                title = {
                    Text(
                        text = state.title
                    )
                }
            )

        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding()),
            color = MaterialTheme.colorScheme.background,
        ) {
            content.invoke()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun RootScreenLightPreview() {
    ThemedScreenPreview(theme = LightTheme) {

    }
}
