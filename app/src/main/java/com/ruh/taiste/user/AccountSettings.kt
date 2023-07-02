package com.ruh.taiste.user

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.ruh.taiste.R
import com.ruh.taiste.Start
import com.ruh.taiste.databinding.ActivityAccountSettingsUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.both.GuideToPurchasing
import com.ruh.taiste.both.PrivacyPolicyActivity
import com.ruh.taiste.both.ReportAnIssue
import com.ruh.taiste.both.TermsOfServiceActivity
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1

class AccountSettings : AppCompatActivity() {
    private lateinit var binding : ActivityAccountSettingsUserBinding

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val user = FirebaseAuth.getInstance().currentUser!!.email!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingsUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.logOut.setOnClickListener {
            val intent = Intent(this, Start::class.java)
            Firebase.auth.signOut()
            startActivity(intent)
            finishAffinity()
        }

        binding.yesPrivate.setOnClickListener {
            binding.yesPrivate.isSelected = true
            binding.noPrivate.isSelected = false

            binding.yesPrivate.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.yesPrivate.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.noPrivate.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.noPrivate.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }

        binding.personalInfo.setOnClickListener {
            val data : Map<String, Any> = hashMapOf("privatizeData" to "yes")
            db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).update(data)
            val intent = Intent(this, Personalize::class.java)
            intent.putExtra("new_or_edit", "edit")
            startActivity(intent)

        }

        binding.guideToPurchasing.setOnClickListener {
            val intent = Intent(this, GuideToPurchasing::class.java)
            startActivity(intent)

        }

        binding.noPrivate.setOnClickListener {
            val data : Map<String, Any> = hashMapOf("privatizeData" to "no")
            db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).update(data)
            binding.noPrivate.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.noPrivate.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.yesPrivate.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.yesPrivate.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }

        binding.termsOfService.setOnClickListener {
            val intent = Intent(this, TermsOfServiceActivity::class.java)
            startActivity(intent)
        }

        binding.dataPrivacy.setOnClickListener {
            val intent = Intent(this, PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }

        binding.reportAnIssue.setOnClickListener {
            val intent = Intent(this, ReportAnIssue::class.java)
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
                        db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid)
                            .collection("PersonalInfo").get().addOnSuccessListener { documents ->
                            if (documents != null) {
                                for (doc in documents.documents) {
                                    val data = doc.data
                                    if (data?.get("username") != null) {
                                        val username = data["username"] as String
                                        db.collection("Usernames").document(username).delete()
                                        db.collection("User")
                                            .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                            .delete()
                                        storage.reference.child("users/${FirebaseAuth.getInstance().currentUser!!.email!!}")
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