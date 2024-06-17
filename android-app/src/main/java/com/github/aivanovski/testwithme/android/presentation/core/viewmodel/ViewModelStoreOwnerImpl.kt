package com.github.aivanovski.testwithme.android.presentation.core.viewmodel

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.arkivanov.essenty.instancekeeper.InstanceKeeper

class ViewModelStoreOwnerImpl : ViewModelStoreOwner, InstanceKeeper.Instance {
    override val viewModelStore: ViewModelStore = ViewModelStore()

    override fun onDestroy() {
        viewModelStore.clear()
    }
}