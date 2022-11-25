package com.example.imageview

import android.annotation.SuppressLint
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
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import kotlin.concurrent.timer

class MainViewModel(private val context: Context, private val imageDao: Dao) : ViewModel() {

    val uriList: ArrayList<ImageData> = arrayListOf()
    val imgNameList: ArrayList<String> = arrayListOf()

    fun setDB() = viewModelScope.launch(Dispatchers.IO) {
        imageDao.getAll()
    }

    fun select(): ArrayList<ImageData> {
        viewModelScope.launch(Dispatchers.IO) {
            imageDao.getAll().map {
                val bitmap: Bitmap = BitmapFactory.decodeFile(it.imageName.substring(5))
                uriList.add(ImageData(bitmap))
            }
        }

        return uriList
    }

    fun delete(context: Context, position: Int): ArrayList<ImageData> {
        uriList.removeAt(position)

        imgNameList.clear()

        viewModelScope.launch(Dispatchers.IO) {
            imageDao.getAll().map {
                imgNameList.add(it.imageName)
            }

            imageDao.delete(Image(imgNameList[position]))

            val flies = context.cacheDir.listFiles()
            for (i in flies!!.indices) {
                if (flies[i].name == imgNameList[position]) {
                    flies[i].delete()
                }
            }
        }

        return uriList
    }

    fun insert(Uri: String) = viewModelScope.launch(Dispatchers.IO) {
        imageDao.insertAll(Image(Uri))
    }

    fun saveBitmap(bitmap: Bitmap, imgName: String) {
        val tempFile = File(context.cacheDir, "$imgName.png")

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

        insert(tempFile.toURI().toString())
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
                    val imgName = imgBitmap.toString().substring(imgBitmap.toString().indexOf("@")+1)

                    saveBitmap(imgBitmap, imgName)

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