package com.ruh.taiste.both.models

import android.net.Uri

data class Search (
    val chefOrUser: String,
    val userImage: Uri,
    val userImageId: String,
    val userName: String,
    val userEmail: String,
    val userFullName: String
)