package com.ruh.taiste.user.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckoutItems(
    val chefEmail: String,
    val chefImageId: String,
    val chefUsername: String,
    val chefImage: Uri?,
    val menuItemId: String,
    val itemTitle: String,
    val itemDescription: String,
    val datesOfEvent: ArrayList<String>,
    val timesForDatesOfEvent: ArrayList<String>,
    val travelExpenseOption: String,
    val totalCostOfEvent: Number,
    val priceToChef: Number,
    val quantityOfEvent: String,
    val unitPrice: String,
    val distance: String,
    val location: String,
    val latitudeOfEvent: String,
    val longitudeOfEvent: String,
    val notesToChef: String,
    val typeOfService: String,
    val typeOfEvent: String,
    val city: String,
    val state: String,
    val user: String,
    val documentId: String,
    val imageCount : Number,
    val liked: ArrayList<String>,
    val itemOrders: Number,
    val itemRating: ArrayList<Number>,
    val itemCalories: Number,
    val allergies: String,
    val additionalMenuItems: String,
    val signatureDishId: String,
    val userNotification: String,
    val chefNotification: String
) : Parcelable


data class Credits(
    var refund : Number,
    val orderDate: String,
    val itemTitle: String,
    val paymentIntent: String,
    var credits: Number,
    var creditsApplied: String,
    var creditAmount: Number,
    val documentId: String
)