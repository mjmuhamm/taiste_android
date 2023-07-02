package com.ruh.taiste.both

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ruh.taiste.R
import com.ruh.taiste.chef.isValidEmail
import com.ruh.taiste.databinding.ActivityCreateUserBinding
import com.ruh.taiste.user.Personalize
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern


private const val TAG = "CreateUser"
class CreateUser : AppCompatActivity() {
    private lateinit var binding: ActivityCreateUserBinding

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser?.email

    private var chefOrUser = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        chefOrUser = intent.getStringExtra("chef_or_user").toString()

        binding.newUser.setOnClickListener {

            binding.newUserView.visibility = View.VISIBLE
            binding.currentUserView.visibility = View.GONE

            binding.newUser.isSelected = true
            binding.currentUser.isSelected = false

            binding.newUser.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.newUser.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.currentUser.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.currentUser.setBackgroundColor(ContextCompat.getColor(this, R.color.white))

        }

        binding.currentUser.setOnClickListener {
            binding.newUserView.visibility = View.GONE
            binding.currentUserView.visibility = View.VISIBLE

            binding.newUser.isSelected = false
            binding.currentUser.isSelected = true

            binding.newUser.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.newUser.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.currentUser.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.currentUser.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
        }

        binding.signInButton.setOnClickListener {
            val profileUpdates = userProfileChangeRequest {
                displayName = "ChefAndUser"
            }

            val intent = Intent(this, Personalize::class.java)
            auth.signInWithEmailAndPassword(binding.currentUserEmail.text.toString(), binding.currentUserPassword.text.toString())

                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")


                        val user = auth.currentUser
                        user!!.updateProfile(profileUpdates)
                            .addOnCompleteListener { task1 ->
                                if (task1.isSuccessful) {
                                    Log.d(TAG, "User profile updated.")
                                }
                            }
                        Toast.makeText(this, "Your account has been created.", Toast.LENGTH_LONG).show()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()

                    }
                }


            startActivity(intent)
            finish()
        }

        binding.signUpButton.setOnClickListener {
            if (binding.newUserEmail.text.isEmpty() || !isValidEmail(binding.newUserEmail.text.toString()))  {
                Toast.makeText(this, "Please enter your email in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (binding.newUserPassword.text.isEmpty()) {
                Toast.makeText(this, "Please enter a password in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (binding.newUserPassword.text !=  binding.newUserConfirmPassword.text) {
                Toast.makeText(this, "Your passwords do not match. Please try again.", Toast.LENGTH_LONG).show()
            } else if (isValidPassword(binding.newUserPassword.text.toString()) == false) {
                Toast.makeText(this, "Please make sure that your password consists of at least 8 characters, 1 number, 1 letter, and 1 special character.", Toast.LENGTH_LONG).show()
            } else {
            val profileUpdates = userProfileChangeRequest {
                displayName = chefOrUser
            }

            if (auth.currentUser != null) {
                AlertDialog.Builder(this)
                    .setTitle("It Looks you already have an account. Do you want to delete this one?")
                    .setMessage("Are you sure you want to delete your account?")
                    // if the dialog is cancelable
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, _ ->
                        auth.currentUser!!.delete().addOnSuccessListener {
                            val intent = Intent(this, VerifyEmail::class.java)
                            auth.createUserWithEmailAndPassword(binding.newUserEmail.text.toString(), binding.newUserPassword.text.toString())
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "createUserWithEmail:success")
                                        val user = auth.currentUser
                                        user!!.sendEmailVerification()
                                        user.updateProfile(profileUpdates)

                                            .addOnCompleteListener { task1 ->
                                                if (task1.isSuccessful) {
                                                    Toast.makeText(this, "Please check your email to verify email address.", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        dialog.dismiss()
                                        startActivity(intent)
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                        Toast.makeText(baseContext, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show()

                                    }
                                }
                        }

                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            } else {
                val intent = Intent(this, VerifyEmail::class.java)
                auth.createUserWithEmailAndPassword(
                    binding.newUserEmail.text.toString(),
                    binding.newUserPassword.text.toString()
                )
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success")
                            val user = auth.currentUser
                            user!!.sendEmailVerification()
                            user.updateProfile(profileUpdates)
                                .addOnCompleteListener { task1 ->
                                    if (task1.isSuccessful) {
                                        Toast.makeText(
                                            this,
                                            "Please check your email to verify email address.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            startActivity(intent)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }
            }
            }


        }

    }

}
fun isValidPassword(password: String?): Boolean {

    val passwordREGEX = Pattern.compile("^" +
            "(?=.*[0-9])" +         //at least 1 digit
            "(?=.*[a-z])" +         //at least 1 lower case letter
            "(?=.*[A-Z])" +         //at least 1 upper case letter
            "(?=.*[a-zA-Z])" +      //any letter
            "(?=.*[@#$%^&+=])" +    //at least 1 special character
            "(?=\\S+$)" +           //no white spaces
            ".{8,}" +               //at least 8 characters
            "$");
    return passwordREGEX.matcher(password).matches()
}