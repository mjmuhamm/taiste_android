package com.ruh.taiste.user

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.ruh.taiste.both.CreateUser
import com.ruh.taiste.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import java.util.*

private const val TAG = "Login"
class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var binding: ActivityLoginBinding
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        auth = Firebase.auth
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.signup.setOnClickListener {
            val intent = Intent(this, Personalize::class.java)
            intent.putExtra("chef_or_user", "User")
            startActivity(intent)
        }

        binding.forgottenPasswordButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            if (!binding.email.text.isEmpty()) {
                db.collection("Usernames").get().addOnSuccessListener { documents ->
                    if (documents != null) {
                        for (doc in documents.documents) {
                            val data = doc.data

                            val email = data?.get("email") as String
                            val chefOrUser = data["chefOrUser"] as String

                            if (email == binding.email.text.toString()) {
                                if (chefOrUser == "Chef") {
                                    Toast.makeText(this, "It looks like you have a Chef account. Please create a user account and continue.", Toast.LENGTH_LONG).show()
                                } else {
                                    Firebase.auth.sendPasswordResetEmail(binding.email.text.toString())
                                        .addOnCompleteListener { task ->
                                            if (!task.isSuccessful) {
                                                Toast.makeText(this, "An error has occurred. Please try again later.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                }
                            }
                        }
                    }
                }
                Toast.makeText(this, "If you have an account with us, an email has been sent to the one provided.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Please enter your email in the space above and try again.", Toast.LENGTH_LONG).show()
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.login.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("where_to", "login")
            auth.signInWithEmailAndPassword(binding.email.text.toString(), binding.password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser


                            if (auth.currentUser!!.displayName!! == "User") {
                                startActivity(intent)
                                finish()
                            } else {
                                db.collection("User")
                                    .document(auth.currentUser!!.uid)
                                    .collection("PersonalInfo").get()
                                    .addOnSuccessListener { documents ->
                                        if (documents != null) {
                                            if (documents.count() > 0) {
                                                val user = auth.currentUser
                                                val profileUpdates = userProfileChangeRequest {
                                                    displayName = "User"
                                                }
                                                user!!.updateProfile(profileUpdates)
                                                val intent1 = Intent(this, MainActivity::class.java)
                                                startActivity(intent1)
                                            } else {
                                                val intent1 = Intent(this, Personalize::class.java)
                                                intent1.putExtra("new_chef", "yes")
                                                startActivity(intent1)
                                            }
                                        } else {
                                            val intent1 = Intent(this, Personalize::class.java)
                                            intent1.putExtra("new_chef", "yes")
                                            startActivity(intent1)
                                        }
                                    }
                            }

                    } else {
                        Toast.makeText(baseContext, "Authentication failed. Please double check your information and try again.", Toast.LENGTH_SHORT).show()
                    }
                }

                .addOnFailureListener { task ->
                    val c : Map<String, Any> = hashMapOf("c" to task.localizedMessage, "date" to Date())
                    db.collection("abc").document().set(c)

                }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun a(a: String, b: String, c: String) {
        val c : Map<String, Any> = hashMapOf("a" to a, "b" to b, "c" to c)

    }

}