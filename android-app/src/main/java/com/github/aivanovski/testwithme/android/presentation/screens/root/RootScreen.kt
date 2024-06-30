package com.github.aivanovski.testwithme.android.presentation.screens.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.github.aivanovski.testwithme.android.presentation.core.compose.ThemedScreenPreview
import com.github.aivanovski.testwithme.android.presentation.core.compose.rememberOnClickedCallback
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.AppTheme
import com.github.aivanovski.testwithme.android.presentation.core.compose.theme.LightTheme
import com.github.aivanovski.testwithme.android.presentation.core.navigation.ScreenComponent
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.RootIntent
import com.github.aivanovski.testwithme.android.presentation.screens.root.model.TopBarState

@Composable
fun RootScreen(
    rootComponent: RootScreenComponent
) {
    val viewModel = rootComponent.viewModel
    val topBarState by rootComponent.viewModel.topBarState.collectAsState()

    RootScreen(
        topBarState = topBarState,
        onIntent = viewModel::sendIntent
    ) {
        Children(
            stack = rootComponent.childStack
        ) { (_, component) ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = AppTheme.theme.colors.background
            ) {
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides component as ViewModelStoreOwner
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
    topBarState: TopBarState,
    onIntent: (intent: RootIntent) -> Unit,
    content: @Composable () -> Unit
) {
    val onBackClicked = rememberOnClickedCallback {
        onIntent.invoke(RootIntent.NavigateBack)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.theme.colors.background,
                    titleContentColor = AppTheme.theme.colors.primaryText,
                ),
                title = {
                    Text(
                        text = topBarState.title,
                        color = AppTheme.theme.colors.primaryText
                    )
                },
                navigationIcon = {
                    if (topBarState.isBackVisible) {
                        IconButton(
                            onClick = onBackClicked
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                tint = AppTheme.theme.colors.primaryText,
                                contentDescription = null
                            )
                        }
                    }
                },
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding()),
            color = AppTheme.theme.colors.background
        ) {
            content.invoke()
        }
    }
}

@Composable
@Preview
fun RootScreenLightPreview() {
    ThemedScreenPreview(theme = LightTheme) {
        RootScreen(
            topBarState = newTopBarState(),
            onIntent = {},
            content = {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text("SCREEN CONTENT")
                }
            }
        )
    }
}


private fun newTopBarState(): TopBarState =
    TopBarState(
        title = "Top Bar Title",
        isBackVisible = true
    )