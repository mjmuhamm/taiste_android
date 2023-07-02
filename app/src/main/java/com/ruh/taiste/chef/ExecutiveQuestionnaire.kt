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
import com.ruh.taiste.databinding.ActivityExecutiveQuestionnaireBinding
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import com.ruh.taiste.user.models.PersonalChefInfo

class ExecutiveQuestionnaire : AppCompatActivity() {
    private lateinit var binding : ActivityExecutiveQuestionnaireBinding
    private val db = Firebase.firestore
    private val storage = Firebase.storage


    private var trialRun = 0
    private var weeks = 0
    private var months = 0
    private var menuRequests = "Yes"

    private var chefName = ""
    private var city = ""
    private var state = ""
    private var zipCode = ""


    private var documentId = ""
    private var complete = ""

   private var briefIntroduction = ""
   private var servicePrice = ""
   private var expectations = 0
   private var chefRating = 0
   private var quality = 0
   private var hourlyOrPerSession = ""
   private var liked = ArrayList<String>()
   private var itemOrders = 0
   private var itemRating = ArrayList<Double>()
    private var signatureDishId = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExecutiveQuestionnaireBinding.inflate(layoutInflater)
        setContentView(binding.root)


        loadPersonalChefInfo()
        binding.trialRun.setOnClickListener {
            if (binding.trialRun.isSelected) {
                binding.trialRun.isSelected = false
                trialRun = 0
                binding.trialRun.setTextColor(ContextCompat.getColor(this, R.color.main))
                binding.trialRun.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            } else {
                binding.trialRun.isSelected = true
                trialRun = 1
                binding.trialRun.setTextColor(ContextCompat.getColor(this, com.aminography.primedatepicker.R.color.white))
                binding.trialRun.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            }
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.weeks.setOnClickListener {
            if (binding.weeks.isSelected) {
                binding.weeks.isSelected = false
                weeks = 0
                binding.weeks.setTextColor(ContextCompat.getColor(this, R.color.main))
                binding.weeks.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            } else {
                binding.weeks.isSelected = true
                weeks = 1
                binding.weeks.setTextColor(ContextCompat.getColor(this, com.aminography.primedatepicker.R.color.white))
                binding.weeks.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            }
        }

        binding.months.setOnClickListener {
            if (binding.months.isSelected) {
                binding.months.isSelected = false
                months = 0
                binding.months.setTextColor(ContextCompat.getColor(this, R.color.main))
                binding.months.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            } else {
                binding.months.isSelected = true
                months = 1
                binding.months.setTextColor(
                    ContextCompat.getColor(
                        this,
                        com.aminography.primedatepicker.R.color.white
                    )
                )
                binding.months.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            }
        }


        binding.menuRequestsYes.setOnClickListener{
            menuRequests = "Yes"
            binding.menuRequestsYes.setTextColor(ContextCompat.getColor(this, com.aminography.primedatepicker.R.color.white))
            binding.menuRequestsYes.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.menuRequestsNo.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.menuRequestsNo.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }

        binding.menuRequestsNo.setOnClickListener {
            menuRequests = "No"
            binding.menuRequestsNo.setTextColor(ContextCompat.getColor(this, com.aminography.primedatepicker.R.color.white))
            binding.menuRequestsNo.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.menuRequestsYes.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.menuRequestsYes.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }

        binding.saveButton.setOnClickListener {
            if (binding.lengthOfPersonalChef.text.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter the length in which you have been a personal chef.",
                Toast.LENGTH_LONG
            ).show()
        } else if (binding.whatHelpsYouExcel.text.isEmpty()) {
            Toast.makeText(this, "Please share what helps you excel?", Toast.LENGTH_LONG)
                .show()
        } else if (binding.mostPrizedAccomplishment.text.isEmpty()) {
            Toast.makeText(
                this,
                "Please list your most prized accomplishment.",
                Toast.LENGTH_LONG
            ).show()
        } else if (this.weeks == 0 && this.months == 0 && this.trialRun == 0) {
            Toast.makeText(
                this,
                "Please select the times in which you are willing to service consumers: Trial Runs, Weeks, or Months.",
                Toast.LENGTH_LONG
            ).show()
        } else if (binding.specialty.text.isEmpty()) {
            Toast.makeText(this, "Please enter your specialty.", Toast.LENGTH_LONG).show()
        } else {
                val info: Map<String, Any> = hashMapOf(
                    "typeOfService" to "info",
                    "briefIntroduction" to this.briefIntroduction,
                    "lengthOfPersonalChef" to binding.lengthOfPersonalChef.text.toString(),
                    "specialty" to binding.specialty.text.toString(),
                    "whatHelpsYouExcel" to binding.whatHelpsYouExcel.text.toString(),
                    "trialRun" to this.trialRun,
                    "weeks" to this.weeks,
                    "months" to this.months,
                    "chefImageId" to FirebaseAuth.getInstance().currentUser!!.uid,
                    "hourlyOrPerSession" to "perSession",
                    "servicePrice" to this.servicePrice,
                    "expectations" to this.expectations,
                    "chefRating" to this.chefRating,
                    "quality" to this.quality,
                    "chefName" to this.chefName,
                    "mostPrizedAccomplishment" to binding.mostPrizedAccomplishment.text.toString(),
                    "chefEmail" to FirebaseAuth.getInstance().currentUser!!.email!!,
                    "city" to this.city,
                    "state" to this.state,
                    "zipCode" to this.zipCode,
                    "liked" to this.liked,
                    "itemOrders" to this.itemOrders,
                    "itemRating" to this.itemRating,
                    "complete" to "yes",
                    "signatureDishId" to this.signatureDishId,
                    "openToMenuRequests" to this.menuRequests
                )
                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .collection("Executive Items").document(documentId).update(info)

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("where_to", "home")

                if (complete == "yes") {
                    db.collection("Executive Items").document(documentId).update(info)
                    startActivity(intent)
                    finish()
                } else {
                    db.collection("Executive Items").document(documentId).set(info)
                    startActivity(intent)
                    finish()
                }
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

                    for (doc in documents.documents) {
                        val data = doc.data

                        val typeOfService = data?.get("typeOfService") as String

                        if (typeOfService == "info") {
                            val lengthOfPersonalChef = data["lengthOfPersonalChef"] as String
                            val specialty = data["specialty"] as String
                            val whatHelpsYouExcel = data["whatHelpsYouExcel"] as String
                            val mostPrizedAccomplishment = data["mostPrizedAccomplishment"] as String
                            val weeks = data["weeks"] as Number
                            val months = data["months"] as Number
                            val trialRun = data["trialRun"] as Number
                            val complete = data["complete"] as String
                            val openToMenuRequests = data["openToMenuRequests"] as String
                            val briefIntroduction = data["briefIntroduction"] as String
                            val servicePrice = data["servicePrice"] as String
                            val expectations = data["expectations"] as Number
                            val chefRating = data["chefRating"] as Number
                            val signatureDishId = data["signatureDishId"] as String
                            val quality = data["quality"] as Number
                            val chefName = data["chefName"] as String
                            val liked = data["liked"] as ArrayList<String>
                            val itemOrders = data["itemOrders"] as Number
                            val itemRating = data["itemRating" ] as  ArrayList<Double>

                            this.signatureDishId = signatureDishId
                            this.briefIntroduction = briefIntroduction
                            this.servicePrice = servicePrice
                            this.expectations = expectations.toInt()
                            this.chefRating = chefRating.toInt()
                            this.quality = quality.toInt()
                            this.chefName = chefName
                            this.hourlyOrPerSession = "perSession"
                            this.liked = liked
                            this.itemOrders = itemOrders.toInt()
                            this.itemRating = itemRating

                            this.complete = complete
                            this.documentId = doc.id

                            binding.lengthOfPersonalChef.setText(lengthOfPersonalChef)
                            binding.mostPrizedAccomplishment.setText(mostPrizedAccomplishment)
                            binding.specialty.setText(specialty)
                            binding.whatHelpsYouExcel.setText(whatHelpsYouExcel)
                            this.trialRun = trialRun.toInt()
                            this.weeks = weeks.toInt()
                            this.months = months.toInt()

                            if (trialRun.toInt() != 0) {
                                binding.trialRun.setTextColor(ContextCompat.getColor(this, com.aminography.primedatepicker.R.color.white))
                                binding.trialRun.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                            }
                            if (weeks.toInt() != 0) {
                                binding.weeks.setTextColor(ContextCompat.getColor(this, com.aminography.primedatepicker.R.color.white))
                                binding.weeks.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                            }
                            if (months.toInt() != 0) {
                                binding.months.setTextColor(ContextCompat.getColor(this, com.aminography.primedatepicker.R.color.white))
                                binding.months.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                            }
                            this.menuRequests = openToMenuRequests

                            if (openToMenuRequests == "Yes") {
                                binding.menuRequestsYes.setTextColor(ContextCompat.getColor(this, com.aminography.primedatepicker.R.color.white))
                                binding.menuRequestsYes.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                                binding.menuRequestsNo.setTextColor(ContextCompat.getColor(this, R.color.main))
                                binding.menuRequestsNo.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                            } else {
                                binding.menuRequestsNo.setTextColor(ContextCompat.getColor(this, com.aminography.primedatepicker.R.color.white))
                                binding.menuRequestsNo.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                                binding.menuRequestsYes.setTextColor(ContextCompat.getColor(this, R.color.main))
                                binding.menuRequestsYes.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
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