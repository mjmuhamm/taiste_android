package com.ruh.taiste.both

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.ruh.taiste.R
import com.ruh.taiste.databinding.ActivityReportAnIssueBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import java.text.SimpleDateFormat
import java.util.*

class ReportAnIssue : AppCompatActivity() {
    private lateinit var binding : ActivityReportAnIssueBinding

    private val db = Firebase.firestore

    private var issueWithEvent = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportAnIssueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.yesButton.setOnClickListener {
            binding.specificEventLayout.visibility = View.VISIBLE
            issueWithEvent = 1
            binding.yesButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.yesButton.setBackgroundColor(ContextCompat.getColor(this, com.aminography.primedatepicker.R.color.red700))
            binding.noButton.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.noButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }

        binding.noButton.setOnClickListener {
            binding.specificEventLayout.visibility = View.GONE
            issueWithEvent = 0
            binding.yesButton.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.yesButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.noButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.noButton.setBackgroundColor(ContextCompat.getColor(this, R.color.main))
        }

        binding.submitButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            if (binding.briefDescription.text.isEmpty()) {
                Toast.makeText(this, "Please enter a brief description of the event in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (binding.detailedDescription.text.isEmpty()) {
                Toast.makeText(this, "Please enter a detailed description of the event in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (issueWithEvent == 1 && binding.itemTitle.text.isEmpty()) {
                Toast.makeText(this, "Please provide the item title of the event", Toast.LENGTH_LONG).show()
            } else if (binding.email.text.isEmpty() && binding.phoneNumber.text.isEmpty()) {
                Toast.makeText(this, "Please provide an email or phone number for us to reach you.", Toast.LENGTH_LONG).show()
            } else if (issueWithEvent == 2) {
                Toast.makeText(this, "Please select whether this issue associates with an event.", Toast.LENGTH_LONG).show()
            } else {
                saveData()
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }


    }

    @SuppressLint("SimpleDateFormat")
    val sdf = SimpleDateFormat("MM-dd-yyy")

    private fun saveData() {
        val itemTitle = if (issueWithEvent == 1) { binding.itemTitle.text.toString() } else { "" }
        val data: Map<String, Any> = hashMapOf("issueWithEvent" to issueWithEvent, "itemTitle" to itemTitle, "briefDescription" to binding.briefDescription.text.toString(), "detailedDescription" to binding.detailedDescription.text.toString(), "email" to binding.email.text.toString(), "phone" to binding.phoneNumber.text.toString(), "user" to FirebaseAuth.getInstance().currentUser!!.uid, "date" to "${sdf.format(Date())}")
        db.collection("Issues").document().set(data).addOnSuccessListener {
            Toast.makeText(this, "Thank you for your report. Someone from our team will reach out to you really soon.", Toast.LENGTH_LONG).show()
            onBackPressedDispatcher.onBackPressed()
        }



    }
}