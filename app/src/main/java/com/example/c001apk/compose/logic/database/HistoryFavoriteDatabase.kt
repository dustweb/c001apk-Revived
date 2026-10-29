package com.example.c001apk.compose.logic.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.c001apk.compose.logic.dao.HistoryFavoriteDao
import com.example.c001apk.compose.logic.model.FeedEntity


@Database(version = 2, entities = [FeedEntity::class])
abstract class HistoryFavoriteDatabase : RoomDatabase() {
    abstract fun historyFavoriteDao(): HistoryFavoriteDao

    companion object {
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE FeedEntity ADD COLUMN mediaJson TEXT DEFAULT NULL")
            }
        }
    }
}