package com.ruh.taiste.both.models

import android.net.Uri

data class Notifications(
    val notification: String,
    val date: String,
    val documentId: String
)

data class MessageRequests(
    val chefOrUser: String,
    val userName: String,
    val userImageId: String,
    val userImage: Uri,
    val userEmail: String,
    val date: String
)

data class MessageNotifications(
    val chefOrUser: String,
    val userImageId: String,
    val userEmail: String,
    val userImage: Uri,
    val userName: String,
    val date: String,
    val documentId: String
)

