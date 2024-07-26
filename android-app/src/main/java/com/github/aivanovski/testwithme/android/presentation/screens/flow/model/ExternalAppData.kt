package com.github.aivanovski.testwithme.android.presentation.screens.flow.model

import android.graphics.Bitmap
import com.github.aivanovski.testwithme.android.entity.AppVersion

data class ExternalAppData(
    val packageName: String,
    val appVersion: AppVersion,
    val icon: Bitmap?
)