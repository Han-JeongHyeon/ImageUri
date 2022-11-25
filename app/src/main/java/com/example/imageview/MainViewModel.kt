package com.example.imageview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.lifecycle.*
import androidx.room.Room
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import kotlin.concurrent.timer

class MainViewModel(private val context: Context) : ViewModel() {

    val uriList: ArrayList<ImageData> = arrayListOf()
    val imgNameList: ArrayList<String> = arrayListOf()

    var dao: Dao? = null

    fun setDB() {

    }

    fun delete(context: Context, position: Int) {
        try {
            imgNameList.clear()

            viewModelScope.launch(Dispatchers.IO) {
                dao!!.getAll().map {
                    imgNameList.add(it.imageName)
                }
                dao!!.delete(Image(imgNameList[position]))

                val flies = context.cacheDir.listFiles()
                for (i in flies!!.indices) {
                    if (flies[i].name == imgNameList[position]) {
                        flies[i].delete()
                    }
                }
            }
        } catch (e: Exception) { }
    }

    fun insert(Uri: String) {
        dao!!.insertAll(Image(Uri))
    }

    fun saveBitmap(bitmap: Bitmap, imgName: String) {
        val tempFile = File(context.cacheDir, "$imgName.png")
        try {
            val flies = context.cacheDir.listFiles()
            for (i in flies!!.indices) {
                if (flies[i].name == "$imgName.png") {
                    flies[i].delete()
                }
            }
            tempFile.createNewFile()
            val out = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.close()
        } catch (e: Exception) { }

        insert(tempFile.toURI().toString())
    }

}