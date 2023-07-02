package com.ruh.taiste.user.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeedMenuItems (
    val chefEmail: String,
    val chefPassion: String,
    val chefUsername: String,
    val chefImageId: String,
    val chefImage: Uri?,
    val menuItemId: String,
    val itemImage: Uri,
    val itemTitle: String,
    val itemDescription: String,
    val itemPrice: String,
    val liked: ArrayList<String>,
    val itemOrders: Number,
    val itemRating: ArrayList<Number>,
    val date: String,
    val imageCount: Number,
    val itemCalories: String,
    val itemType: String,
    val city: String,
    val state: String,
    val user: String,
    val healthy: Number,
    val creative: Number,
    val vegan: Number,
    val burger: Number,
    val seafood: Number,
    val pasta: Number,
    val workout: Number,
    val lowCal: Number,
    val lowCarb: Number,

) : Parcelable

data class Filter(
    val local: Int,
    val region: Int,
    val nation: Int,
    val city: String,
    val state: String,
    val burger: Int,
    val creative: Int,
    val lowCal: Int,
    val lowCarb: Int,
    val pasta: Int,
    val healthy: Int,
    val vegan: Int,
    val seafood: Int,
    val workout: Int,
    val surpriseMe: Int
)

@Parcelize
data class PersonalChefInfo(
    val chefName: String,
    val chefEmail: String,
    val chefImageId: String,
    var chefImage: Uri,
    val city: String,
    val state: String,
    val zipCode: String,
    var signatureDishImage: Uri,
    val signatureDishId: String,
    var option1Title: String,
    var option2Title: String,
    var option3Title: String,
    var option4Title: String,
    val briefIntroduction: String,
    val howLongBeenAChef: String,
    val specialty: String,
    val whatHelpsYouExcel: String,
    val mostPrizedAccomplishment: String,
    val availability: String,
    val hourlyOrPerSession: String,
    val servicePrice: String,
    val trialRun: Int,
    val weeks: Int,
    val months: Int,
    val liked: ArrayList<String>,
    val itemOrders: Int,
    val itemRating: ArrayList<Double>,
    val expectations: Int,
    val chefRating: Int,
    val quality: Int,
    val documentId: String,
    val openToMenuRequests: String
) : Parcelable