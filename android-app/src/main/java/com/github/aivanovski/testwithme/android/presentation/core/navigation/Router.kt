package com.github.aivanovski.testwithme.android.presentation.core.navigation

import com.github.aivanovski.testwithme.android.presentation.screens.Screen

interface Router {
    fun setRoot(screen: Screen)
    fun navigateTo(screen: Screen)
    fun exit()
}