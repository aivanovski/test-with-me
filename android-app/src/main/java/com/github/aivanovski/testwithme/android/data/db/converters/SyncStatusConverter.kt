package com.github.aivanovski.testwithme.android.data.db.converters

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.github.aivanovski.testwithme.android.entity.SyncStatus

@ProvidedTypeConverter
class SyncStatusConverter {

    @TypeConverter
    fun fromDatabaseValue(value: String?): SyncStatus? =
        value?.let { SyncStatus.fromName(value) }

    @TypeConverter
    fun toDatabaseValue(status: SyncStatus?): String? = status?.name
}