package com.example.imageview

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface Dao {
    @Query("SELECT * FROM image")
    fun getAll(): List<Image>

    @Insert
    fun insertAll(vararg image: Image)

    @Delete
    fun delete(image: Image)
}