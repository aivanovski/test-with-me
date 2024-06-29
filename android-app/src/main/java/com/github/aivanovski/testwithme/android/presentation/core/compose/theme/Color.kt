package com.github.aivanovski.testwithme.android.presentation.core.compose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

@Immutable
data class AppColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val primaryCardBackground: Color,
    val secondaryCardBackground: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val testGreen: Color,
    val testRed: Color
)

val LightAppColors = AppColors(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFF_ececed),
    primaryCardBackground = Color(0xFF_ffffff),
    secondaryCardBackground = Color(0xFF_f2f4f7),
    primaryText = Color(0xFF_00000d),
    secondaryText = Color.Gray,
    testGreen = Color(0xFF_2c9066),
    testRed = Color(0xFF_f2473b)
)

val DarkAppColors = AppColors(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF_181c1e),
    primaryCardBackground = Color(0xFF_181c1e),
    secondaryCardBackground = Color(0xFF_f2f4f7),
    primaryText = Color.White,
    secondaryText = Color.Gray,
    testGreen = Color(0xFF1de5a9),
    testRed = Color(0xFFdd1445)
)
