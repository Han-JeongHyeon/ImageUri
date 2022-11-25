package com.example.imageview

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import androidx.viewpager2.widget.ViewPager2
import com.example.imageview.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream
import java.util.*


class MainActivity() : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    val uriList: ArrayList<ImageData> = arrayListOf()
    val imgNameList: ArrayList<String> = arrayListOf()

    val viewModel: MainViewModel by inject()

    var viewPagerPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

//        try {
//            lifecycleScope.launch(Dispatchers.IO) {
//                dao!!.getAll().map {
//                    val bitmap: Bitmap = BitmapFactory.decodeFile(it.imageName.substring(5))
//                    uriList.add(ImageData(bitmap))
//                }
//            }
//            binding.viewPager2.adapter = Adapter(uriList, baseContext)
//        } catch (e: Exception) { }

//        viewModel.setDB()

        val db = Room.databaseBuilder(
            baseContext,
            AppDatabase::class.java, "imageDB"
        ).build()

        db.userDao()

        binding.loadBtn.setOnClickListener {
            navigatePhotos()
        }

        binding.deleteBtn.setOnClickListener {
            uriList.removeAt(viewPagerPosition)
            binding.viewPager2.adapter = Adapter(uriList, baseContext)
            viewModel.delete(baseContext, viewPagerPosition)
        }

        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewPagerPosition = position
            }
        })

    }

    @SuppressLint("IntentReset")
    private fun navigatePhotos() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, 2222)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data?.clipData != null) {
            val clipData = data.clipData!!.itemCount

            if (clipData < 3) {
                Toast.makeText(applicationContext, "사진은 최소 3장을 선택해야합니다.", Toast.LENGTH_LONG)
                    .show()
            } else if (clipData > 10) {
                Toast.makeText(applicationContext, "사진은 최대 10장까지 선택 가능합니다.", Toast.LENGTH_LONG)
                    .show()
            } else {
                for (i in 0 until clipData) {
                    val imageUri = data.clipData!!.getItemAt(i).uri

                    val resolver = contentResolver

                    val inputStream = resolver.openInputStream(imageUri)
                    val imgBitmap = BitmapFactory.decodeStream(inputStream)
                    val imgName = imgBitmap.toString().substring(imgBitmap.toString().indexOf("@")+1)

                    viewModel.saveBitmap(imgBitmap, imgName)
//                    viewModel.insert()

                    inputStream!!.close()
                    uriList.add(ImageData(imgBitmap))
                }

                binding.viewPager2.adapter = Adapter(uriList, baseContext)
            }
        } else if (data?.data == null) {
            Toast.makeText(applicationContext, "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show()
        }
    }



}