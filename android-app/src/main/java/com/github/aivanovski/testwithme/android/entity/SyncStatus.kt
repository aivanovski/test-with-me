package com.github.aivanovski.testwithme.android.entity

enum class SyncStatus {
    NONE,
    WAITING_FOR_SYNC,
    SYNCED,
    FAILURE;

    companion object {
        fun fromName(name: String): SyncStatus? {
            return values().firstOrNull { status -> status.name == name }
        }
    }
}