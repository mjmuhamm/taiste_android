package com.ruh.taiste.both.models

import android.net.Uri

data class Reviews (
    val chefEmail: String,
    val chefImageId: String,
    val date: String,
    val itemType: String,
    val itemTitle: String,
    val liked: ArrayList<String>,
    var numberOfReplies: Number,
    val menuItemId: String,
    val reviewItemID: String,
    val user: String,
    val userChefRating: Number,
    val userExpectationsRating: Number,
    val userImageId: String,
    val userImage: Uri?,
    val userQualityRating: Number,
    val userRecommendation: Number,
    val userReviewTextField: String,
)

data class Replies (
    val chefOrUser: String,
    val date: String,
    val message: String,
    val milliSecondDate: String,
    val replyToUser: String,
    val liked: ArrayList<String>,
    val reviewItemID: String,
    val user: String,
    val userImageId: String,
    val userImage: Uri?,
    val itemType: String,
    val menuItemId: String,
    val documentId: String,
)