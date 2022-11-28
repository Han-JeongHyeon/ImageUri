package com.example.imageview

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

@HiltAndroidApp
class MyApplication: Application() {

}