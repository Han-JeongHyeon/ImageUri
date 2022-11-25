package com.example.imageview

import android.R.attr.bitmap
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.imageview.databinding.ActivityMainBinding
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject


class MainActivity() : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    val viewModel: MainViewModel by inject()

    var viewPagerPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.setDB()

        binding.viewPager2.adapter = Adapter(viewModel.select(), baseContext)

        binding.loadBtn.setOnClickListener {
            navigatePhotos()
        }

        binding.deleteBtn.setOnClickListener {
            try {
                val uri = viewModel.delete(baseContext, viewPagerPosition)
                binding.viewPager2.adapter = Adapter(uri, baseContext)
            } catch (e: Exception) { }
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

        binding.viewPager2.adapter = Adapter(viewModel.getActivityResult(data), baseContext)

    }

}