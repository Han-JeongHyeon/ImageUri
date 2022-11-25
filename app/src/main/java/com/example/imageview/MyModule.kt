package com.example.imageview

import android.app.Application
import android.util.Log
import androidx.room.Room
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

var appModule = module {

    fun imageDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(application, AppDatabase::class.java, "imageDB")
            .fallbackToDestructiveMigration()
            .build()
    }

    fun imageDao(database: AppDatabase): Dao {
        return database.userDao()
    }

    single { imageDatabase( androidApplication() ) }
    single { imageDao( get() ) }

}

var viewModule = module{
    viewModel {
        MainViewModel( get() )
    }
}