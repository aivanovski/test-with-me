package com.github.aivanovski.testwithme.android.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.defaultComponentContext
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootScreenComponent
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val component = RootScreenComponent(
            componentContext = defaultComponentContext()
        )

        setContent {
            RootScreen(rootComponent = component)
        }
    }
}