package com.example.imageview

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.*
import androidx.room.Room
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val imageDao: Dao, application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext

    private val uriList: ArrayList<ImageData> = arrayListOf()
    private val imgNameList: ArrayList<Image> = arrayListOf()

    suspend fun select() = viewModelScope.async(Dispatchers.IO) {
        imageDao.getAll().map {
            val bitmap: Bitmap = BitmapFactory.decodeFile(it.imageName.substring(5))
            uriList.add(ImageData(bitmap))
        }

        return@async uriList
    }.await()

    suspend fun delete(position: Int): ArrayList<ImageData> {
        try {
            uriList.removeAt(position)

            imgNameList.clear()

            viewModelScope.async(Dispatchers.IO) {
                imageDao.getAll().map {
                    imgNameList.add(Image(it.id,it.imageName))
                }

                imageDao.delete(imgNameList[position])

                val flies = context.cacheDir.listFiles()
                for (i in flies!!.indices) {
                    if (flies[i].name == imgNameList[position].id) {
                        flies[i].delete()
                    }
                }
            }.await()
        } catch (e: Exception) { }

        return uriList
    }

    private fun saveBitmap(bitmap: Bitmap) {
        val tempFile = File(context.cacheDir, "$bitmap.png")

        val flies = context.cacheDir.listFiles()
        for (i in flies!!.indices) {
            if (flies[i].name == "$bitmap.png") {
                flies[i].delete()
            }
        }
        tempFile.createNewFile()
        val out = FileOutputStream(tempFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.close()

        viewModelScope.launch(Dispatchers.IO) {
            imageDao.insertAll(Image(tempFile.name, tempFile.toURI().toString()))
        }
    }

    fun getActivityResult(data: Intent?): ArrayList<ImageData> {
        val handler = Handler(Looper.getMainLooper())
        if (data?.clipData != null) {
            val clipData = data.clipData!!.itemCount

            if (clipData < 3) {
                handler.postDelayed({
                Toast.makeText(context, "사진은 최소 3장을 선택해야합니다.", Toast.LENGTH_LONG)
                    .show()
                }, 0)
            } else if (clipData > 10) {
                handler.postDelayed({
                    Toast.makeText(context, "사진은 최대 10장까지 선택 가능합니다.", Toast.LENGTH_LONG)
                        .show()
                }, 0)

            } else {
                for (i in 0 until clipData) {
                    val imageUri = data.clipData!!.getItemAt(i).uri

                    val resolver = context.contentResolver

                    val inputStream = resolver.openInputStream(imageUri)
                    val imgBitmap = BitmapFactory.decodeStream(inputStream)

                    saveBitmap(imgBitmap)

                    inputStream!!.close()
                    uriList.add(ImageData(imgBitmap))
                }

            }
        } else if (data?.data == null) {
            Toast.makeText(context, "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show()
        }

        return uriList
    }

}