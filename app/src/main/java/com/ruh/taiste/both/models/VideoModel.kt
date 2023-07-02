package com.ruh.taiste.both.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoModel(
    val dataUri: String,
    val id: String,
    val videoDate: String,
    val user: String,
    val description: String,
    val views: Number,
    var liked: ArrayList<String>,
    var comments: Number,
    var shared: Number,
    val thumbnailUrl: String
    ) : Parcelable