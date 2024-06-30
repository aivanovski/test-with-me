package com.github.aivanovski.testwithme.android.presentation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StartArgs(
    val flowUid: String?
) : Parcelable {

    companion object {
        val EMPTY = StartArgs(
            flowUid = null
        )
    }
}