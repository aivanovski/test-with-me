package com.github.aivanovski.testwithme.android.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.aivanovski.testwithme.android.data.db.converters.FlowSourceTypeConverter
import com.github.aivanovski.testwithme.android.data.db.converters.FlowStepConverter
import com.github.aivanovski.testwithme.android.data.db.converters.JobStatusConverter
import com.github.aivanovski.testwithme.android.data.db.converters.StepVerificationTypeConverter
import com.github.aivanovski.testwithme.android.data.db.converters.SyncStatusConverter
import com.github.aivanovski.testwithme.android.data.db.dao.LocalStepRunDao
import com.github.aivanovski.testwithme.android.data.db.dao.FlowEntryDao
import com.github.aivanovski.testwithme.android.data.db.dao.JobDao
import com.github.aivanovski.testwithme.android.data.db.dao.StepEntryDao
import com.github.aivanovski.testwithme.android.entity.db.LocalStepRun
import com.github.aivanovski.testwithme.android.entity.db.FlowEntry
import com.github.aivanovski.testwithme.android.entity.db.JobEntry
import com.github.aivanovski.testwithme.android.entity.db.StepEntry

@Database(
    entities = [
        StepEntry::class,
        FlowEntry::class,
        JobEntry::class,
        LocalStepRun::class
    ],
    version = 1
)
@TypeConverters(
    FlowStepConverter::class,
    StepVerificationTypeConverter::class,
    SyncStatusConverter::class,
    FlowSourceTypeConverter::class,
    JobStatusConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract val stepEntryDao: StepEntryDao
    abstract val flowEntryDao: FlowEntryDao
    abstract val runnerEntryDao: JobDao
    abstract val executionDataDao: LocalStepRunDao

    companion object {

        fun buildDatabase(
            context: Context
        ): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "testwithme.db"
            )
                .addTypeConverter(FlowStepConverter())
                .addTypeConverter(StepVerificationTypeConverter())
                .addTypeConverter(SyncStatusConverter())
                .addTypeConverter(FlowSourceTypeConverter())
                .addTypeConverter(JobStatusConverter())
                .build()
        }
    }
}