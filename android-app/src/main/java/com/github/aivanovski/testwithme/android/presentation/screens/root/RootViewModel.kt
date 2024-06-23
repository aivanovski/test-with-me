package com.github.aivanovski.testwithme.android.presentation.screens.root

import androidx.lifecycle.ViewModel
import com.github.aivanovski.testwithme.utils.StringUtils
import kotlinx.coroutines.flow.MutableStateFlow

class RootViewModel : ViewModel() {

    val state = MutableStateFlow(StringUtils.EMPTY)
}