package com.github.aivanovski.testwithme.android.presentation.core.navigation

import com.github.aivanovski.testwithme.android.presentation.screens.Screen

interface Router {
    fun navigateTo(screen: Screen)
    fun exit()
}