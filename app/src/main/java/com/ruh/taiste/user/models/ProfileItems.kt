package com.ruh.taiste.user.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserOrders(

    val chefEmail: String,
    val chefUsername: String,
    val chefImageId: String,
    val chefImage: Uri?,
    val city: String,
    val state: String,
    val eventDates: ArrayList<String>,
    val itemTitle: String,
    val itemDescription: String,
    val itemPrice: String,
    val menuItemId: String,
    val itemImage: Uri,
    val orderDate: String,
    val orderUpdate: String,
    val totalCostOfEvent: Number,
    val travelFee: String,
    val typeOfService: String,
    val imageCount: Number,
    val liked: ArrayList<String>,
    val itemOrders: Number,
    val itemRating: ArrayList<Number>,
    val itemCalories: Number,
    val documentId: String,
    val signatureDishId: String

) : Parcelable

@Parcelize
data class UserChefs(

    val chefEmail: String,
    val chefImageId: String,
    val chefImage: Uri?,
    val chefName: String,
    val chefPassion: String,
    var timesLiked: Number,
    var chefLiked: ArrayList<String>,
    var chefOrders: Number,
    var chefRating: ArrayList<Number>

) : Parcelable

@Parcelize
data class UserLikes(

    val chefEmail: String,
    val chefUsername: String,
    val chefImageId: String,
    val chefImage: Uri?,
    val city: String,
    val state: String,
    val itemType: String,
    val itemTitle: String,
    val itemDescription: String,
    val itemPrice: String,
    val itemImage: Uri,
    val imageCount: Number,
    val liked: ArrayList<String>,
    val itemOrders: Number,
    val itemRating: ArrayList<Number>,
    val itemCalories: Number,
    val documentId: String,
    val user: String,
    val signatureDishId: String

) : Parcelable

@Parcelize
data class UserReviews(

    val chefEmail: String,
    val chefImageId: String,
    val chefImage: Uri?,
    val chefName: String,
    val date: String,
    val documentID: String,
    val itemTitle: String,
    val itemType: String,
    val liked: ArrayList<String>,
    val reviewItemID: String,
    val user: String,
    val userChefRating: Number,
    val userExpectationsRating: Number,
    val userImageId: String,
    val userQualityRating: Number,
    val userRecommendation: Number,
    val userReviewTextField: String

) : Parcelable

data class Usernames(
    val username: String,
    val email: String
)