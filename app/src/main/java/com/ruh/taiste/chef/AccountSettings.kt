package com.ruh.taiste.chef

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.ruh.taiste.Start
import com.ruh.taiste.databinding.ActivityAccountSettingsBinding
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.both.*
import com.ruh.taiste.chef.menu_item_post_guide.GuideToCaterItems
import com.ruh.taiste.chef.menu_item_post_guide.GuideToExecutiveItems
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1

class AccountSettings : AppCompatActivity() {
    private lateinit var binding : ActivityAccountSettingsBinding

    private var db = Firebase.firestore
    private var storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.personalInfo.setOnClickListener {
            val intent = Intent(this, Personal::class.java)
            intent.putExtra("new_or_edit", "edit")
            startActivity(intent)
        }

        binding.businessInfo.setOnClickListener {
            val intent = Intent(this, Business::class.java)
            intent.putExtra("new_or_edit", "edit")
            startActivity(intent)
        }

        binding.guideToPosting.setOnClickListener {
            val intent = Intent(this, GuideToPosting::class.java)
            startActivity(intent)
        }

        binding.termsOfService.setOnClickListener {
            val intent = Intent(this, TermsOfServiceActivity::class.java)
            startActivity(intent)
        }
        binding.regulationAgreement.setOnClickListener {
            val intent = Intent(this, Disclaimer::class.java)
            intent.putExtra("new_or_edit", "edit")
            startActivity(intent)
        }

        binding.dataPrivacy.setOnClickListener {
            val intent = Intent(this, PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }

        binding.bankingInfo.setOnClickListener {
            val intent = Intent(this, Banking::class.java)
            intent.putExtra("new_or_edit", "edit")
            startActivity(intent)
        }

        binding.reportAnIssue.setOnClickListener {
            val intent = Intent(this, ReportAnIssue::class.java)
            startActivity(intent)
        }

        binding.logOut.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            val intent = Intent(this, Start::class.java)
            Firebase.auth.signOut()
            startActivity(intent)
            finishAffinity()
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.caterItems.setOnClickListener {
            val intent = Intent(this, GuideToCaterItems::class.java)
            startActivity(intent)
        }

        binding.executiveItems.setOnClickListener {
            val intent = Intent(this, GuideToExecutiveItems::class.java)
            startActivity(intent)
        }

        binding.deleteAccount.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            val intent = Intent(this, Start::class.java)
            AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                // if the dialog is cancelable
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, _ ->
                    if (FirebaseAuth.getInstance().currentUser != null) {
                    db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("PersonalInfo").get().addOnSuccessListener { documents ->
                        if (documents != null) {
                            for (doc in documents.documents) {
                                val data = doc.data
                                if (data?.get("username") != null) {
                                    val username = data["username"] as String
                                    db.collection("Usernames").document(username).delete()
                                    db.collection("Chef")
                                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                        .delete()
                                    storage.reference.child("chefs/${FirebaseAuth.getInstance().currentUser!!.uid}")
                                        .delete()
                                    FirebaseAuth.getInstance().currentUser!!.delete()
                                    Toast.makeText(
                                        this,
                                        "Sorry to see you go. Hope to see you back.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    startActivity(intent)
                                    finishAffinity()

                                    dialog.dismiss()
                                }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
                    }


                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

    }

}