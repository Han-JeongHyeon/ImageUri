package com.example.imageview

import android.R.attr
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import androidx.viewpager2.widget.ViewPager2
import com.example.imageview.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.String
import java.util.*
import kotlin.Exception
import kotlin.Int
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val uriList: ArrayList<ImageData> = arrayListOf()
    var removePosition = 0

    var dao: Dao? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = Room.databaseBuilder(
            applicationContext,
            Database::class.java, "imageBD"
        ).build()

        dao = db.userDao()

        var imagea: List<Image>? = null

        binding.btn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val aa = dao!!.getAll()
                Log.d("TAG", "a $aa")
                uriList.add(ImageData(aa[0].imageUri.toUri()))
                withContext(Dispatchers.Main) {
                    binding.viewPager2.adapter = Adapter(uriList, baseContext)
                }
            }
        }

//        uriList.add(image)

        binding.loadBtn.setOnClickListener {
            navigatePhotos()
        }

        binding.deleteBtn.setOnClickListener {
            uriList.removeAt(removePosition)
//            binding.viewPager2.adapter = Adapter(uriList, baseContext)
        }

        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                Log.d("TAG", "$position")
                removePosition = position
            }

        })

    }

    @SuppressLint("IntentReset")
    private fun navigatePhotos() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        startActivityForResult(intent, 2222)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data?.data == null) {
            Toast.makeText(applicationContext, "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show()
        } else {
            val clipData: ClipData? = data.clipData

            if (clipData!!.itemCount < 2) {
                Toast.makeText(applicationContext, "사진은 최소 3장을 선택해야합니다.", Toast.LENGTH_LONG)
                    .show()
            } else if (clipData.itemCount > 10) {
                Toast.makeText(applicationContext, "사진은 최대 10장까지 선택 가능합니다.", Toast.LENGTH_LONG)
                    .show()
            } else {
                for (i in 0 until clipData.itemCount) {
                    val imageUri = clipData.getItemAt(i).uri
//                    CoroutineScope(Dispatchers.IO).launch {
//                        dao!!.insertAll(Image(contentResolver.getType(imageUri)!!))
//                    }
//                    uriList.add(ImageData(imageUri))
                }

//                binding.viewPager2.adapter = Adapter(uriList, baseContext)
            }
        }
    }
}