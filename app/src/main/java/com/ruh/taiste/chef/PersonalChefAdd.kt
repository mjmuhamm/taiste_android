package com.ruh.taiste.chef

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.R
import com.ruh.taiste.databinding.ActivityPersonalChefAddBinding
import com.ruh.taiste.databinding.PersonalChefPostBinding
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import com.ruh.taiste.user.models.PersonalChefInfo
import java.util.*
import kotlin.collections.ArrayList
private const val TAG = "PersonalChefAdd"
class PersonalChefAdd : AppCompatActivity() {
    private lateinit var binding: ActivityPersonalChefAddBinding

    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private var trialRun = 0
    private var weeks = 0
    private var months = 0
    private var chefName = ""
    private var city = ""
    private var state = ""
    private var zipCode = ""
    private var liked = ArrayList<String>()
    private var itemOrders = 0
    private var itemRating = ArrayList<Double>()
    private var documentId = UUID.randomUUID().toString()
    private var openToMenuRequests = "Yes"
    private var complete = ""
    private var signatureDishId = ""

    private var expectations = 0
    private var chefRating = 0
    private var quality = 0

    private var personalChefInfo : PersonalChefInfo? = null

    private var mostPrizedAccomplishment = ""
    private var lengthOfPersonalChef = ""
    private var specialty = ""
    private var whatHelpsYouExcel = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalChefAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(this)
            } else {
                isOnline1(this)
            }
        ) {

        if (FirebaseAuth.getInstance().currentUser != null) {
            loadPersonalChefInfo()
        } else {
            Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
        }
        } else {
            Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.signatureDishButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            if (FirebaseAuth.getInstance().currentUser != null) {
                val info: Map<String, Any> = hashMapOf(
                    "typeOfService" to "info",
                    "briefIntroduction" to binding.briefIntroduction.text.toString(),
                    "lengthOfPersonalChef" to "",
                    "specialty" to "",
                    "whatHelpsYouExcel" to "",
                    "trialRun" to this.trialRun,
                    "weeks" to this.weeks,
                    "momnths" to this.months,
                    "hourlyOrPerSession" to "perSession",
                    "servicePrice" to binding.servicePrice.text.toString(),
                    "expectations" to this.expectations,
                    "chefRating" to this.chefRating,
                    "quality" to this.quality,
                    "chefName" to chefName,
                    "mostPrizedAccomplishment" to "",
                    "chefImageId" to FirebaseAuth.getInstance().currentUser!!.uid,
                    "city" to city,
                    "state" to state,
                    "liked" to liked,
                    "itemOrders" to itemOrders,
                    "itemRating" to itemRating,
                    "openToMenuRequests" to ""
                )
                personalChefInfo = PersonalChefInfo(
                    chefName,
                    FirebaseAuth.getInstance().currentUser!!.email!!,
                    FirebaseAuth.getInstance().currentUser!!.uid,
                    Uri.EMPTY,
                    city,
                    state,
                    zipCode,
                    Uri.EMPTY,
                    "",
                    "",
                    "",
                    "",
                    binding.briefIntroduction.text.toString(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "perSession",
                    binding.servicePrice.text.toString(),
                    this.trialRun,
                    this.weeks,
                    this.months,
                    ArrayList(),
                    itemOrders,
                    itemRating,
                    expectations,
                    chefRating,
                    quality,
                    documentId,
                    openToMenuRequests
                )
                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .collection("Executive Items").document(documentId).update(info)

                val intent = Intent(this, MenuItemAdd::class.java)
                intent.putExtra("new_or_edit", "signature")
                intent.putExtra("city", city)
                intent.putExtra("state", state)
                intent.putExtra("zipCode", zipCode)
                intent.putExtra("chefUsername", chefName)
                intent.putExtra("type_of_item", "Signature Dish")
                intent.putExtra("type", "Executive Chef")
                intent.putExtra("personal_chef_item", personalChefInfo)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.option1Button.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            if (FirebaseAuth.getInstance().currentUser != null) {
                val info: Map<String, Any> = hashMapOf(
                    "typeOfService" to "info",
                    "briefIntroduction" to binding.briefIntroduction.text.toString(),
                    "lengthOfPersonalChef" to "",
                    "specialty" to "",
                    "whatHelpsYouExcel" to "",
                    "trialRun" to this.trialRun,
                    "weeks" to this.weeks,
                    "momnths" to this.months,
                    "hourlyOrPerSession" to "perSession",
                    "servicePrice" to binding.servicePrice.text.toString(),
                    "expectations" to this.expectations,
                    "chefRating" to this.chefRating,
                    "quality" to this.quality,
                    "chefName" to chefName,
                    "mostPrizedAccomplishment" to "",
                    "chefImageId" to FirebaseAuth.getInstance().currentUser!!.uid,
                    "city" to city,
                    "state" to state,
                    "liked" to liked,
                    "itemOrders" to itemOrders,
                    "itemRating" to itemRating,
                    "openToMenuRequests" to this.openToMenuRequests
                )
                personalChefInfo = PersonalChefInfo(
                    chefName,
                    FirebaseAuth.getInstance().currentUser!!.email!!,
                    FirebaseAuth.getInstance().currentUser!!.uid,
                    Uri.EMPTY,
                    city,
                    state,
                    zipCode,
                    Uri.EMPTY,
                    "",
                    "",
                    "",
                    "",
                    binding.briefIntroduction.text.toString(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "perSession",
                    binding.servicePrice.text.toString(),
                    this.trialRun,
                    this.weeks,
                    this.months,
                    ArrayList(),
                    itemOrders,
                    itemRating,
                    expectations,
                    chefRating,
                    quality,
                    documentId,
                    openToMenuRequests
                )
                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .collection("Executive Items").document(documentId).update(info)

                val intent = Intent(this, MenuItemAdd::class.java)
                intent.putExtra("new_or_edit", "option1")
                intent.putExtra("city", city)
                intent.putExtra("state", state)
                intent.putExtra("zipCode", zipCode)
                intent.putExtra("chefUsername", chefName)
                intent.putExtra("type_of_item", "Option 1")
                intent.putExtra("type", "Executive Chef")
                intent.putExtra("personal_chef_item", personalChefInfo)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.option2Button.setOnClickListener {
            if (binding.option1Text.text.toString() != "Add Option") {
                if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        isOnline(this)
                    } else {
                        isOnline1(this)
                    }
                ) {

                    if (FirebaseAuth.getInstance().currentUser != null) {
                        val info: Map<String, Any> = hashMapOf(
                            "typeOfService" to "info",
                            "briefIntroduction" to binding.briefIntroduction.text.toString(),
                            "lengthOfPersonalChef" to "",
                            "specialty" to "",
                            "whatHelpsYouExcel" to "",
                            "trialRun" to this.trialRun,
                            "weeks" to this.weeks,
                            "momnths" to this.months,
                            "hourlyOrPerSession" to "perSession",
                            "servicePrice" to binding.servicePrice.text.toString(),
                            "expectations" to this.expectations,
                            "chefRating" to this.chefRating,
                            "quality" to this.quality,
                            "chefName" to chefName,
                            "mostPrizedAccomplishment" to "",
                            "chefImageId" to FirebaseAuth.getInstance().currentUser!!.uid,
                            "city" to city,
                            "state" to state,
                            "liked" to liked,
                            "itemOrders" to itemOrders,
                            "itemRating" to itemRating,
                            "openToMenuRequests" to this.openToMenuRequests
                        )
                        personalChefInfo = PersonalChefInfo(
                            chefName,
                            FirebaseAuth.getInstance().currentUser!!.email!!,
                            FirebaseAuth.getInstance().currentUser!!.uid,
                            Uri.EMPTY,
                            city,
                            state,
                            zipCode,
                            Uri.EMPTY,
                            "",
                            "",
                            "",
                            "",
                            binding.briefIntroduction.text.toString(),
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "perSession",
                            binding.servicePrice.text.toString(),
                            this.trialRun,
                            this.weeks,
                            this.months,
                            ArrayList(),
                            itemOrders,
                            itemRating,
                            expectations,
                            chefRating,
                            quality,
                            documentId,
                            openToMenuRequests
                        )
                        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid)
                            .collection("Executive Items").document(documentId).update(info)

                        val intent = Intent(this, MenuItemAdd::class.java)
                        intent.putExtra("new_or_edit", "option2")
                        intent.putExtra("city", city)
                        intent.putExtra("state", state)
                        intent.putExtra("zipCode", zipCode)
                        intent.putExtra("chefUsername", chefName)
                        intent.putExtra("type_of_item", "Option 2")
                        intent.putExtra("type", "Executive Chef")
                        intent.putExtra("personal_chef_item", personalChefInfo)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this,
                            "Something went wrong. Please check your connection.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Seems to be a problem with your internet. Please check your connection.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(this, "Please insert option for Option 1 first.", Toast.LENGTH_LONG).show()
            }
        }

        binding.option3Button.setOnClickListener {
            if (binding.option2Text.text.toString() != "Add Option") {
                if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        isOnline(this)
                    } else {
                        isOnline1(this)
                    }
                ) {

                    if (FirebaseAuth.getInstance().currentUser != null) {
                        val info: Map<String, Any> = hashMapOf(
                            "typeOfService" to "info",
                            "briefIntroduction" to binding.briefIntroduction.text.toString(),
                            "lengthOfPersonalChef" to "",
                            "specialty" to "",
                            "whatHelpsYouExcel" to "",
                            "trialRun" to this.trialRun,
                            "weeks" to this.weeks,
                            "momnths" to this.months,
                            "hourlyOrPerSession" to "perSession",
                            "servicePrice" to binding.servicePrice.text.toString(),
                            "expectations" to this.expectations,
                            "chefRating" to this.chefRating,
                            "quality" to this.quality,
                            "chefName" to chefName,
                            "mostPrizedAccomplishment" to "",
                            "chefImageId" to FirebaseAuth.getInstance().currentUser!!.uid,
                            "city" to city,
                            "state" to state,
                            "liked" to liked,
                            "itemOrders" to itemOrders,
                            "itemRating" to itemRating,
                            "openToMenuRequests" to this.openToMenuRequests
                        )
                        personalChefInfo = PersonalChefInfo(
                            chefName,
                            FirebaseAuth.getInstance().currentUser!!.email!!,
                            FirebaseAuth.getInstance().currentUser!!.uid,
                            Uri.EMPTY,
                            city,
                            state,
                            zipCode,
                            Uri.EMPTY,
                            "",
                            "",
                            "",
                            "",
                            binding.briefIntroduction.text.toString(),
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "perSession",
                            binding.servicePrice.text.toString(),
                            this.trialRun,
                            this.weeks,
                            this.months,
                            ArrayList(),
                            itemOrders,
                            itemRating,
                            expectations,
                            chefRating,
                            quality,
                            documentId,
                            openToMenuRequests
                        )
                        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid)
                            .collection("Executive Items").document(documentId).update(info)

                        val intent = Intent(this, MenuItemAdd::class.java)
                        intent.putExtra("new_or_edit", "option3")
                        intent.putExtra("city", city)
                        intent.putExtra("state", state)
                        intent.putExtra("zipCode", zipCode)
                        intent.putExtra("item_label", "Executive Items")
                        intent.putExtra("chefUsername", chefName)
                        intent.putExtra("type_of_item", "Option 3")
                        intent.putExtra("type", "Executive Chef")
                        intent.putExtra("personal_chef_item", personalChefInfo)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this,
                            "Something went wrong. Please check your connection.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Seems to be a problem with your internet. Please check your connection.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(this, "Please add option for Option 2 first.", Toast.LENGTH_LONG).show()
            }
        }

        binding.option4Button.setOnClickListener {
            if (binding.option3Text.text.toString() != "Add Option") {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            if (FirebaseAuth.getInstance().currentUser != null) {
                val info: Map<String, Any> = hashMapOf(
                    "typeOfService" to "info",
                    "briefIntroduction" to binding.briefIntroduction.text.toString(),
                    "lengthOfPersonalChef" to "",
                    "specialty" to "",
                    "whatHelpsYouExcel" to "",
                    "trialRun" to this.trialRun,
                    "weeks" to this.weeks,
                    "momnths" to this.months,
                    "hourlyOrPerSession" to "perSession",
                    "servicePrice" to binding.servicePrice.text.toString(),
                    "expectations" to this.expectations,
                    "chefRating" to this.chefRating,
                    "quality" to this.quality,
                    "chefName" to chefName,
                    "mostPrizedAccomplishment" to "",
                    "chefImageId" to FirebaseAuth.getInstance().currentUser!!.uid,
                    "city" to city,
                    "state" to state,
                    "liked" to liked,
                    "itemOrders" to itemOrders,
                    "itemRating" to itemRating,
                    "openToMenuRequests" to this.openToMenuRequests
                )
                personalChefInfo = PersonalChefInfo(
                    chefName,
                    FirebaseAuth.getInstance().currentUser!!.email!!,
                    FirebaseAuth.getInstance().currentUser!!.uid,
                    Uri.EMPTY,
                    city,
                    state,
                    zipCode,
                    Uri.EMPTY,
                    "",
                    "",
                    "",
                    "",
                    binding.briefIntroduction.text.toString(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "perSession",
                    binding.servicePrice.text.toString(),
                    this.trialRun,
                    this.weeks,
                    this.months,
                    ArrayList(),
                    itemOrders,
                    itemRating,
                    expectations,
                    chefRating,
                    quality,
                    documentId,
                    openToMenuRequests
                )
                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .collection("Executive Items").document(documentId).update(info)

                val intent = Intent(this, MenuItemAdd::class.java)
                intent.putExtra("new_or_edit", "option4")
                intent.putExtra("city", city)
                intent.putExtra("state", state)
                intent.putExtra("zipCode", zipCode)
                intent.putExtra("chefUsername", chefName)
                intent.putExtra("type_of_item", "Option 4")
                intent.putExtra("personal_chef_item", personalChefInfo)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            } else {
                Toast.makeText(this, "Please add option for Option 3 first.", Toast.LENGTH_LONG).show()
            }
        }


        binding.saveButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            if (FirebaseAuth.getInstance().currentUser != null) {
                if (binding.briefIntroduction.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please provide a brief introduction of why people should believe in your ability.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.servicePrice.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please enter your price for this service.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val a = "%.2f".format(binding.servicePrice.text.toString().toDouble())
                    val info: Map<String, Any> = hashMapOf(
                        "typeOfService" to "info",
                        "briefIntroduction" to binding.briefIntroduction.text.toString(),
                        "lengthOfPersonalChef" to lengthOfPersonalChef,
                        "specialty" to specialty,
                        "whatHelpsYouExcel" to whatHelpsYouExcel,
                        "trialRun" to this.trialRun,
                        "weeks" to this.weeks,
                        "months" to this.months,
                        "hourlyOrPerSession" to "perSession",
                        "servicePrice" to binding.servicePrice.text.toString(),
                        "expectations" to this.expectations,
                        "chefRating" to this.chefRating,
                        "quality" to this.quality,
                        "chefName" to this.chefName,
                        "mostPrizedAccomplishment" to mostPrizedAccomplishment,
                        "chefEmail" to FirebaseAuth.getInstance().currentUser!!.email!!,
                        "city" to this.city,
                        "chefImageId" to FirebaseAuth.getInstance().currentUser!!.uid,
                        "state" to this.state,
                        "zipCode" to this.zipCode,
                        "liked" to this.liked,
                        "itemOrders" to this.itemOrders,
                        "itemRating" to this.itemRating,
                        "complete" to this.complete,
                        "signatureDishId" to this.signatureDishId,
                        "openToMenuRequests" to this.openToMenuRequests

                    )
                    db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .collection("Executive Items").document(documentId).update(info)

                    val intent = Intent(this, ExecutiveQuestionnaire::class.java)

                    if (complete == "yes") {
                        db.collection("Executive Items").document(documentId).update(info)
                    } else {
                        db.collection("Executive Items").document(documentId).set(info)
                    }
                    startActivity(intent)
                    finish()
                }

            } else {
                Toast.makeText(this, "Something went wrong. Please check your connection and try again.", Toast.LENGTH_LONG).show()
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }


        loadChefName()



    }

    private fun loadChefName() {
        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("PersonalInfo").get().addOnSuccessListener { documents ->
            for (doc in documents.documents) {
                val data = doc.data

                val chefName = data?.get("chefName") as String
                this.chefName = chefName
            }
        }
    }

    private fun loadPersonalChefInfo() {
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(this)
            } else {
                isOnline1(this)
            }
        ) {

        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Executive Items").get().addOnSuccessListener { documents ->
            if (documents != null) {
                if (documents.count() == 0) {
                    val info: Map<String, Any> = hashMapOf("typeOfService" to "info", "briefIntroduction" to "", "lengthOfPersonalChef" to "", "specialty" to "", "whatHelpsYouExcel" to "", "trialRun" to trialRun, "weeks" to weeks, "months" to months, "hourlyOrPerSession" to "perSession", "servicePrice" to "", "expectations" to 0, "chefRating" to 0, "quality" to 0, "chefName" to chefName, "mostPrizedAccomplishment" to "", "chefImageId" to FirebaseAuth.getInstance().currentUser!!.uid, "chefEmail" to FirebaseAuth.getInstance().currentUser!!.email!!, "city" to city, "state" to state, "zipCode" to zipCode, "liked" to liked, "itemOrders" to 0, "itemRating" to itemRating, "complete" to "", "signatureDishId" to "", "openToMenuRequests" to "Yes")
                    personalChefInfo = PersonalChefInfo(chefName, FirebaseAuth.getInstance().currentUser!!.email!!, FirebaseAuth.getInstance().currentUser!!.uid, Uri.EMPTY, city, state, zipCode, Uri.EMPTY, "", "", "", "", "", "", "", "", "", "", "", "", "", trialRun, weeks, months, ArrayList(), itemOrders, ArrayList<Double>(), 0, 0, 0, documentId, openToMenuRequests)
                    db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Executive Items").document(documentId).set(info)
                }
                for (doc in documents.documents) {
                    val data = doc.data

                    val typeOfService = data?.get("typeOfService") as String
                    Log.d(TAG, "loadPersonalChefInfo: personal chef happening $typeOfService" )
                    if (typeOfService == "info") {
                        val briefIntroduction = data["briefIntroduction"] as String
                        val lengthOfPersonalChef = data["lengthOfPersonalChef"] as String
                        val specialty = data["specialty"] as String
                        val servicePrice = data["servicePrice"] as String
                        val expectations = data["expectations"] as Number
                        val chefRating = data["chefRating"] as Number
                        val quality = data["quality"] as Number
                        val chefName = data["chefName"] as String
                        val whatHelpsYouExcel = data["whatHelpsYouExcel"] as String
                        val mostPrizedAccomplishment = data["mostPrizedAccomplishment"] as String
                        val weeks = data["weeks"] as Number
                        val months = data["months"] as Number
                        val trialRun = data["trialRun"] as Number
                        val hourlyOrPerSession = data["hourlyOrPerSession"] as String
                        val liked = data["liked"] as ArrayList<String>
                        val itemOrders = data["itemOrders"] as Number
                        val itemRating = data["itemRating" ] as  ArrayList<Double>
                        val complete = data["complete"] as String
                        val openToMenuRequests = data["openToMenuRequests"] as String
                        val email = data["chefEmail"] as String
                        binding.briefIntroduction.setText(briefIntroduction)

                        this.complete = complete
                        if (briefIntroduction != "Brief Introduction, and why people should believe in your ability.") {
                            binding.briefIntroduction.setTextColor(ContextCompat.getColor(this, R.color.main))
                        } else {
                            binding.briefIntroduction.setText("")
                        }
                        this.liked = liked
                        this.expectations = expectations.toInt()
                        this.chefRating = chefRating.toInt()
                        this.quality = quality.toInt()
                        this.openToMenuRequests = openToMenuRequests

                        this.mostPrizedAccomplishment = mostPrizedAccomplishment
                        this.lengthOfPersonalChef = lengthOfPersonalChef
                        this.specialty = specialty
                        this.whatHelpsYouExcel = whatHelpsYouExcel


                        binding.servicePrice.setText(servicePrice)
                        this.complete = complete
                        this.documentId = doc.id
                        val availability = ""

                        this.personalChefInfo = PersonalChefInfo(chefName, email, FirebaseAuth.getInstance().currentUser!!.uid, Uri.EMPTY, city, state, zipCode, Uri.EMPTY, "", "", "", "", "", briefIntroduction, lengthOfPersonalChef, specialty, whatHelpsYouExcel, mostPrizedAccomplishment, availability, hourlyOrPerSession, servicePrice, trialRun.toInt(), weeks.toInt(), months.toInt(), liked, itemOrders.toInt(), itemRating, expectations.toInt(), chefRating.toInt(), quality.toInt(), doc.id, openToMenuRequests)

                    } else {
                        val itemTitle = data["itemTitle"] as String
                        val chefEmail = data["chefEmail"] as String
                        Log.d(TAG, "loadPersonalChefInfo: $chefEmail")
                        Log.d(TAG, "loadPersonalChefInfo: ${doc.id}")
                        when (typeOfService) {
                            "Signature Dish" -> {
                                this.signatureDishId = doc.id
                                storage.reference.child("chefs/${chefEmail}/Executive Items/${doc.id}0.png").downloadUrl.addOnSuccessListener { uri ->

                                    Glide.with(this).load(uri).placeholder(R.drawable.default_profile).into(binding.signatureImage)
                                    if (this.personalChefInfo != null) {
                                        this.personalChefInfo!!.signatureDishImage = uri
                                    }
                                }
                            }
                            "Option 1" -> {
                                binding.option1Text.text = itemTitle.take(18)
                                binding.option1Text.setTextColor(ContextCompat.getColor(this, R.color.main))
                                binding.option2Button.isEnabled = true
                                if (this.personalChefInfo != null) {
                                    this.personalChefInfo!!.option1Title = itemTitle
                                }
                            }
                            "Option 2" -> {
                                binding.option2Text.text = itemTitle.take(18)
                                binding.option1Text.setTextColor(ContextCompat.getColor(this, R.color.main))
                                binding.option3Button.isEnabled = true
                                if (this.personalChefInfo != null) {
                                    this.personalChefInfo!!.option2Title = itemTitle
                                }
                            }
                            "Option 3" -> {
                                binding.option3Text.text = itemTitle.take(18)
                                binding.option1Text.setTextColor(ContextCompat.getColor(this, R.color.main))
                                binding.option4Button.isEnabled = true
                                if (this.personalChefInfo != null) {
                                    this.personalChefInfo!!.option3Title = itemTitle
                                }
                            }
                            "Option 4" -> {
                                binding.option1Text.setTextColor(ContextCompat.getColor(this, R.color.main))
                                binding.option4Text.text = itemTitle.take(18)
                                if (this.personalChefInfo != null) {
                                    this.personalChefInfo!!.option4Title = itemTitle
                                }
                            }
                        }

                    }


                }
            }
        }
        } else {
            Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }
    }

}