package com.ruh.taiste.both.models

import android.net.Uri

data class Comments(
    val chefOrUser: String,
    val comment: String,
    val date: String,
    val repliedToUser: String,
    var secondComments: Number,
    val userImageId: String,
    var liked: ArrayList<String>,
    val username: String,
    val userImage: Uri?,
    val documentId: String
)

data class SecondComments(
    val chefOrUser: String,
    val comment: String,
    val date: String,
    val repliedToUser: String,
    val user: String,
    val userImageId: String,
    val userImage: Uri?,
    var secondComments: Number,
    val liked: ArrayList<String>,
    val username: String,
    val documentId: String
)