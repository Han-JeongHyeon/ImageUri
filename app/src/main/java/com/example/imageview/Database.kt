package com.example.imageview

import androidx.room.*
import androidx.room.Database
import androidx.sqlite.db.SupportSQLiteOpenHelper

@Database(entities = [Image::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {

    abstract fun userDao(): Dao

}