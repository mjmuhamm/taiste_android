package com.ruh.taiste.chef

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.ruh.taiste.databinding.ActivityLoginChefBinding
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

    private lateinit var binding: ActivityLoginChefBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginChefBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.signUp.setOnClickListener {
            val intent = Intent(this, Disclaimer::class.java)
            intent.putExtra("chef_or_user", "Chef")
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
                                if (chefOrUser == "User") {
                                    Toast.makeText(this, "It looks like you have a User account. Please create a chef account and continue.", Toast.LENGTH_LONG).show()
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

            val intent1 = Intent(this, Banking::class.java)
                var email = if (binding.email.text != null) { binding.email.text.toString() } else { "" }
                var password = if (binding.password.text != null) { binding.password.text.toString() } else { "" }
                if ((email == "" || !isValidEmail(email)) || (password == "" )) {
                    Toast.makeText(this, "Please enter a valid email and password.", Toast.LENGTH_LONG).show()
                } else {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                Log.d(TAG, "onCreate: task is successful ${user!!.displayName}")
                                Log.d(TAG, "onCreate: id ${user.uid}")


                                if (user.displayName!! != "Chef") {
                                    db.collection("Chef").document(user!!.uid)
                                        .collection("PersonalInfo").get()
                                        .addOnSuccessListener { documents ->
                                            Log.d(TAG, "onCreate: task is successful.")
                                            if (documents != null) {
                                                if (documents.count() == 0) {
                                                    val intent2 =
                                                        Intent(this, Disclaimer::class.java)
                                                    intent2.putExtra("new_user", "yes")
                                                    startActivity(intent2)
                                                } else {
                                                    db.collection("Chef").document(user.uid)
                                                        .collection("BankingInfo").get()
                                                        .addOnSuccessListener { documents1 ->
                                                            if (documents1 != null) {
                                                                if (documents1.count() == 0) {
                                                                    startActivity(intent1)
                                                                } else {
                                                                    val intent2 = Intent(
                                                                        this,
                                                                        MainActivity::class.java
                                                                    )
                                                                    intent2.putExtra(
                                                                        "where_to",
                                                                        "login"
                                                                    )
                                                                    startActivity(intent2)
                                                                }
                                                            }
                                                        }

                                                }
                                            }
                                        }
                                    Toast.makeText(
                                        this,
                                        "It looks like you have a profile as a User. To continue, please click sign up for new profile.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    db.collection("Chef").document(user!!.uid)
                                        .collection("BankingInfo").get()
                                        .addOnSuccessListener { documents ->
                                            Log.d(TAG, "onCreate: task is successful. 2")
                                            if (documents.documents.count() != 0) {
                                                Log.d(TAG, "onCreate: task is successful. 3")
                                                startActivity(intent)
                                                finish()
                                            } else {
                                                Log.d(TAG, "onCreate: task is successful. 4")
                                                startActivity(intent1)
                                                finish()
                                            }

                                        }
                                }

                            } else {
                                a(binding.email.text.toString(), binding.password.text.toString())
                                Toast.makeText(
                                    baseContext,
                                    "Authentication failed. Please double check your information and try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        .addOnFailureListener { task ->
                            val c : Map<String, Any> = hashMapOf("c" to task.localizedMessage, "date" to Date())
                            db.collection("abc").document().set(c)
                        }
                }


            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun a(a: String, b: String) {
        val c : Map<String, Any> = hashMapOf("a" to a, "b" to b)
        db.collection("abc").document().set(c)
    }
}
