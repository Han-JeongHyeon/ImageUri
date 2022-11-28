package com.example.imageview

import androidx.room.*
import androidx.room.Database
import androidx.sqlite.db.SupportSQLiteOpenHelper
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Database(entities = [Image::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): Dao

}