package com.ruh.taiste.both.models

import android.net.Uri

data class Messages(
    val chefOrUser: String,
    val homeOrAway: String,
    val user: String,
    val userImage: Uri,
    val date: String,
    val message: String,
    val userEmail: String,
    val documentId: String,
    val travelFee: String
)