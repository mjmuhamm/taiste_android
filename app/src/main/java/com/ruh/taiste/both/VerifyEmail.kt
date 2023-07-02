package com.ruh.taiste.both

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.ruh.taiste.chef.Personal
import com.ruh.taiste.databinding.ActivityVerifyEmailBinding
import com.ruh.taiste.user.Personalize
import com.google.firebase.auth.FirebaseAuth
import com.ruh.taiste.Start

class VerifyEmail : AppCompatActivity() {
    private lateinit var binding : ActivityVerifyEmailBinding

    private var userType = FirebaseAuth.getInstance().currentUser!!.displayName!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.sendVerificationAgain.setOnClickListener {
            FirebaseAuth.getInstance().currentUser!!.sendEmailVerification().addOnSuccessListener {
                Toast.makeText(this, "Email verification sent. Please check your email and verify to continue.", Toast.LENGTH_LONG).show()
            }
        }

        binding.continueButton.setOnClickListener {

            val intent = Intent(this, Personalize::class.java)
            val intent1 = Intent(this, Personal::class.java)
            if (FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
                if (userType == "Chef") {
                    startActivity(intent1)
                } else {
                    startActivity(intent)
                }
                finish()

            } else {
                Toast.makeText(this, "Please check your email and verify to continue.", Toast.LENGTH_LONG).show()
            }
        }


    }
}