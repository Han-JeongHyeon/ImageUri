package com.example.imageview

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.net.URI
import java.net.URL

@Entity
data class Image(
    @PrimaryKey val imageUri: String
)
