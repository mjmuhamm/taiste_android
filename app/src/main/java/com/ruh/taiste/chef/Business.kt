package com.ruh.taiste.chef

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.ruh.taiste.databinding.ActivityBusinessBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import java.util.*
import kotlin.collections.ArrayList

class Business : AppCompatActivity() {
    private lateinit var binding: ActivityBusinessBinding

    private val db = Firebase.firestore

    private var newOrEdit = ""
    private var originalLocation = ""
    private var documentId = UUID.randomUUID().toString()


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBusinessBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.progressBar.isVisible = false

        newOrEdit = intent.getStringExtra("new_or_edit").toString()

        if (newOrEdit == "edit") {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            if (FirebaseAuth.getInstance().currentUser != null) {
                loadInfo()
            } else {
                Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            binding.saveButton.text = "Update"
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.saveButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            if (FirebaseAuth.getInstance().currentUser != null) {
                if (binding.passion.text.isEmpty()) {
                    Toast.makeText(this, "Please enter a brief introduction of your passion for cooking.", Toast.LENGTH_LONG).show()
                } else if (binding.streetAddress.text.isEmpty()) {
                    Toast.makeText(this, "Please enter a street address.", Toast.LENGTH_LONG).show()
                } else if (binding.city.text.isEmpty()) {
                    Toast.makeText(this, "Please enter a city.", Toast.LENGTH_LONG).show()
                } else if (binding.state.text.isEmpty()) {
                    Toast.makeText(this, "Please enter a state.", Toast.LENGTH_LONG).show()
                } else if (binding.zipCode.text.isEmpty()) {
                    Toast.makeText(this, "Please enter a zipCode.", Toast.LENGTH_LONG).show()
                } else if (stateFilter(binding.state.text.toString()) != "good") {
                    Toast.makeText(this, "Please enter the abbreviation of your state.", Toast.LENGTH_LONG).show()
                } else {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("where_to", "home")
                    val intent1 = Intent(this, Banking::class.java)
                    if (binding.streetAddress.text.isEmpty() || binding.city.text.isEmpty() || binding.state.text.isEmpty() || binding.zipCode.text.isEmpty()) {
                        Toast.makeText(
                            this,
                            "Please enter your business street address.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        if (newOrEdit != "edit") {
                            val data: Map<String, Any> = hashMapOf(
                                "chefPassion" to binding.passion.text.toString(),
                                "city" to binding.city.text.toString(),
                                "state" to binding.state.text.toString(),
                                "zipCode" to binding.zipCode.text.toString()
                            )
                            val data1: Map<String, Any> = hashMapOf(
                                "streetAddress" to binding.streetAddress.text.toString(),
                                "city" to binding.city.text.toString(),
                                "state" to binding.state.text.toString(),
                                "zipCode" to binding.zipCode.text.toString()
                            )
                            db.collection("Chef")
                                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                .collection("BusinessInfo").document().set(data1)
                            db.collection("Chef")
                                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                .collection("PersonalInfo").get()
                                .addOnSuccessListener { documents ->
                                    for (doc in documents.documents) {

                                        db.collection("Chef")
                                            .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                            .collection("PersonalInfo").document(doc.id)
                                            .update(data)
                                    }
                                }
                            startActivity(intent1)
                        } else {
                            val array: MutableList<String> =
                                mutableListOf("Cater Items", "Executive Items", "MealKit Items")

                            for (i in array) {
                                db.collection("Chef")
                                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .collection(i).get().addOnSuccessListener { documents ->
                                        for (doc in documents!!.documents) {
                                            val data: Map<String, Any> = hashMapOf(
                                                "city" to binding.city.text.toString(),
                                                "state" to binding.state.text.toString(),
                                                "zipCode" to binding.zipCode.text.toString()
                                            )

                                            db.collection("Chef")
                                                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                                .collection(i).document(doc.id).update(data)
                                            db.collection(i).document(doc.id).update(data)

                                        }
                                    }
                            }
                            val data: Map<String, Any> = hashMapOf(
                                "chefPassion" to binding.passion.text.toString(),
                                "city" to binding.city.text.toString(),
                                "state" to binding.state.text.toString(),
                                "zipCode" to binding.zipCode.text.toString()
                            )
                            db.collection("Chef")
                                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                .collection("BusinessInfo").document(documentId).update(data)
                            db.collection("Chef")
                                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                .collection("PersonalInfo").get()
                                .addOnSuccessListener { documents ->
                                    for (doc in documents.documents) {

                                        db.collection("Chef")
                                            .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                            .collection("PersonalInfo").document(doc.id)
                                            .update(data)
                                    }
                                }
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun stateFilter(state: String) : String {
        val stateAbbr : MutableList<String> = arrayListOf("AL", "AK", "AZ", "AR", "AS", "CA", "CO", "CT", "DE", "DC", "FL", "GA", "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "PR", "RI", "SC", "SD", "TN", "TX", "TT", "UT", "VT", "VA", "VI", "WA", "WY", "WV", "WI", "WY")

        for (i in 0 until stateAbbr.size) {
            val a = stateAbbr[i].lowercase()
            if (a == state.lowercase()) {
                return "good"
            }
        }

        return "not good"
    }

    private fun loadInfo() {
        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("BusinessInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val passion = data?.get("chefPassion") as String
                    val streetAddress = data["streetAddress"] as String
                    val city = data["city"] as String
                    val state = data["state"] as String
                    val zipCode = data["zipCode"] as String
                    documentId = doc.id

                    binding.passion.setText(passion)
                    binding.streetAddress.setText(streetAddress)
                    binding.city.setText(city)
                    binding.state.setText(state)
                    binding.zipCode.setText(zipCode)
                    originalLocation = "$city, $state"

                }
            }
        }
    }

}