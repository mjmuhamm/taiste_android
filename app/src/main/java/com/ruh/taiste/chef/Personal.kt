package com.ruh.taiste.chef

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.ruh.taiste.R
import com.ruh.taiste.databinding.ActivityPersonalBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.both.isValidPassword
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import com.ruh.taiste.user.userImage
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

private const val TAG = "Personal"
class Personal : AppCompatActivity() {
    private lateinit var binding: ActivityPersonalBinding

    private var db = Firebase.firestore
    private var storage = Firebase.storage
    private lateinit var auth: FirebaseAuth

    private var usernames : MutableList<String> = ArrayList()
    private var emails : MutableList<String> = arrayListOf()

    private var documentId = UUID.randomUUID().toString()
    private var changeImage = ""

    private var newOrEdit = ""
    private var newUser = ""
    private var chefImage = Uri.EMPTY

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        binding.progressBar.isVisible = false
        newOrEdit = intent.getStringExtra("new_or_edit").toString()

        newUser = intent.getStringExtra("new_user").toString()

        if (newUser != "yes") {
            binding.email.setText(FirebaseAuth.getInstance().currentUser!!.email!!)
            binding.password.setText("**********")
            binding.confirmPassword.setText("**********")
            binding.email.isEnabled = false
            binding.password.isEnabled = false
            binding.confirmPassword.isEnabled = false
        }
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(this)
            } else {
                isOnline1(this)
            }
        ) {

        if (newOrEdit == "edit") {
            if (FirebaseAuth.getInstance().currentUser != null) {
                loadInfo()
            } else {
                Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            binding.saveButton.text = "Update"
        }
        if (FirebaseAuth.getInstance().currentUser != null) {
            loadUsernames()
        } else {
            Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
        }
        } else {
            Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
            binding.educationLayout.visibility = View.VISIBLE
            if (chefImage != null) {
                Glide.with(this).load(chefImage).placeholder(R.drawable.default_profile).into(binding.userImage)
            }



        binding.editImage.setOnClickListener {
            ImagePicker.with(this)
                .crop()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                .start()

        }

        binding.saveButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

                val intent1 = Intent(this, MainActivity::class.java)
                intent.putExtra("where_to", "home")
                val intent2 = Intent(this, Business::class.java)

                if (newUser != "yes") {
                    if (binding.fullName.text.isEmpty()) {
                        Toast.makeText(this, "Please enter your full name in the allotted text.", Toast.LENGTH_LONG).show()
                    } else if (binding.chefName.text.isEmpty() || binding.chefName.text.contains(" ") || (binding.chefName.length() < 4)) {
                        Toast.makeText(this, "Please enter your chefName with no spaces, and at least 4 characters.", Toast.LENGTH_LONG).show()
                    } else if (usernames.contains(binding.email.text.toString())) {
                        Toast.makeText(this, "This username is already taken.", Toast.LENGTH_LONG).show()
                    } else if (binding.email.text.isEmpty() || !isValidEmail(binding.email.text)) {
                        Toast.makeText(this, "Please enter your valid email.", Toast.LENGTH_LONG).show()
                    } else if (searchForSpecialChara(binding.chefName.text.toString())) {
                        Toast.makeText(this, "Please remove all special characters from your chefName.", Toast.LENGTH_LONG).show()
                    } else if (emails.contains(binding.email.text.toString())) {
                        Toast.makeText(this, "This is email is already taken. If this is your email, please log in.", Toast.LENGTH_LONG).show()
                    } else if (!isValidPassword(binding.password.text.toString()) && (binding.password.text.isEmpty() || binding.confirmPassword.text.isEmpty())) {
                        Toast.makeText(this, "Please make sure password has 1 uppercase letter, 1 special character, 1 number, 1 lowercase letter, and matches with the second insert.", Toast.LENGTH_LONG).show()
                    } else if (binding.education.text.isEmpty()) {
                        Toast.makeText(this, "Please enter your education. Can be 'Self-Educated'", Toast.LENGTH_LONG).show()
                    } else {
                        val data: Map<String, Any> = hashMapOf("fullName" to binding.fullName.text.toString(), "chefName" to binding.chefName.text.toString(), "email" to binding.email.text.toString(), "education" to binding.education.text.toString(), "city" to "", "state" to "", "zipCode" to "")
                        var profilePic = ""
                        val data2: Map<String, Any> = hashMapOf("chefOrUser" to "Chef", "chargeForPayout" to 0.0, "notifications" to "", "notificationToken" to "", "profilePic" to profilePic)

                        val user = auth.currentUser

                        val profileUpdates = userProfileChangeRequest {
                            displayName = "Chef"
                        }

                        user!!.updateProfile(profileUpdates)


                        if (chefImage != Uri.EMPTY) {
                            profilePic = "yes"
                            storage.reference.child("chefs/${binding.email.text}/profileImage/${user.uid}.png")
                                .putFile(chefImage!!)
                        } else {
                            val imageUri = Uri.parse("android.resource://com.ruh.taiste/" + R.drawable.default_profile)
                            profilePic = "no"
                            storage.reference.child("chefs/${binding.email.text}/profileImage/${user.uid}.png")
                                .putFile(imageUri)
                        }
                        db.collection("Chef").document(user.uid)
                            .collection("PersonalInfo").document()
                            .set(data)
                        db.collection("Chef").document(user.uid)
                            .set(data2)
                        startActivity(intent2)
                    }
                } else {
                    if (newOrEdit != "edit") {
                        if (binding.fullName.text.isEmpty()) {
                            Toast.makeText(this, "Please enter your full name in the allotted text.", Toast.LENGTH_LONG).show()
                        } else if (binding.chefName.text.isEmpty() || binding.chefName.text.contains(" ") || (binding.chefName.length() < 4)) {
                            Toast.makeText(this, "Please enter your chefName with no spaces, and at least 4 characters.", Toast.LENGTH_LONG).show()
                        } else if (usernames.contains(binding.email.text.toString())) {
                            Toast.makeText(this, "This username is already taken.", Toast.LENGTH_LONG).show()
                        } else if (binding.email.text.isEmpty() || !isValidEmail(binding.email.text)) {
                            Toast.makeText(this, "Please enter your valid email.", Toast.LENGTH_LONG).show()
                        } else if (searchForSpecialChara(binding.chefName.text.toString())) {
                            Toast.makeText(this, "Please remove all special characters from your chefName.", Toast.LENGTH_LONG).show()
                        } else if (emails.contains(binding.email.text.toString())) {
                            Toast.makeText(this, "This is email is already taken. If this is your email, please log in.", Toast.LENGTH_LONG).show()
                        } else if (!isValidPassword(binding.password.text.toString()) && (binding.password.text.isEmpty() || binding.confirmPassword.text.isEmpty())) {
                            Toast.makeText(this, "Please make sure password has 1 uppercase letter, 1 special character, 1 number, 1 lowercase letter, and matches with the second insert.", Toast.LENGTH_LONG).show()
                        } else if (binding.education.text.isEmpty()) {
                            Toast.makeText(this, "Please enter your education. Can be 'Self-Educated'", Toast.LENGTH_LONG).show()
                        } else {
                            val data: Map<String, Any> = hashMapOf("fullName" to binding.fullName.text.toString(), "chefName" to binding.chefName.text.toString(), "email" to binding.email.text.toString(), "education" to binding.education.text.toString(), "chefPassion" to "", "city" to "", "state" to "", "zipCode" to "")
                            val data1: Map<String, Any> = hashMapOf("username" to binding.chefName.text.toString(), "email" to binding.email.text.toString(), "chefOrUser" to "Chef", "fullName" to binding.fullName.text.toString())
                            var profilePic = ""
                            val data2: Map<String, Any> = hashMapOf("chefOrUser" to "Chef", "chargeForPayout" to 0.0, "notifications" to "", "notificationToken" to "", "profilePic" to profilePic)
                            auth.createUserWithEmailAndPassword(
                                binding.email.text.toString(),
                                binding.password.text.toString()
                            )
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {

                                        val user = auth.currentUser

                                        val profileUpdates = userProfileChangeRequest {
                                            displayName = "Chef"
                                        }

                                        user!!.updateProfile(profileUpdates)
                                            .addOnCompleteListener { task1 ->
                                                if (task1.isSuccessful) {
                                                    Log.d(TAG, "User profile updated.")
                                                    if (chefImage != Uri.EMPTY) {
                                                        profilePic = "yes"
                                                        storage.reference.child("chefs/${binding.email.text}/profileImage/${user.uid}.png")
                                                            .putFile(chefImage!!)
                                                    } else {
                                                        val imageUri = Uri.parse("android.resource://com.ruh.taiste/" + R.drawable.default_profile)
                                                        profilePic = "no"
                                                        storage.reference.child("chefs/${binding.email.text}/profileImage/${user.uid}.png")
                                                            .putFile(imageUri)
                                                    }
                                                    db.collection("Chef").document(user.uid)
                                                        .collection("PersonalInfo").document()
                                                        .set(data)
                                                    db.collection("Usernames").document(user.uid)
                                                        .set(data1)
                                                    db.collection("Chef").document(user.uid)
                                                        .set(data2)
                                                    startActivity(intent2)
                                                } else {
                                                    Toast.makeText(
                                                        this,
                                                        "Something went wrong. Please try again later.",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Something went wrong. Please try again later.",
                                            Toast.LENGTH_LONG
                                        ).show()

                                    }
                                }
                        }
                    } else {
                        if (binding.fullName.text.isEmpty()) {
                            Toast.makeText(
                                this,
                                "Please enter your full name in the allotted text.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else if (binding.chefName.text.isEmpty() || binding.chefName.text.contains(
                                " "
                            )
                        ) {
                            Toast.makeText(
                                this,
                                "Please enter your chefName with no spaces.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else if (binding.email.text.isEmpty() || !isValidEmail(binding.email.text)) {
                            Toast.makeText(
                                this,
                                "Please enter your valid email.",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        } else if (isValidPassword(binding.password.text.toString()) && (binding.password.text.isEmpty() || binding.confirmPassword.text.isEmpty())) {
                            Toast.makeText(
                                this,
                                "Please make sure password has 1 uppercase letter, 1 special character, 1 number, 1 lowercase letter, and matches with the second insert.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else if (binding.education.text.isEmpty()) {
                            Toast.makeText(
                                this,
                                "Please enter your education. Can be 'Self-Educated'",
                                Toast.LENGTH_LONG
                            ).show()
                        } else if (chefImage == null) {
                            Toast.makeText(this, "Please add a profile image.", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            if (!binding.password.text.contains("*")) {
                                if (!isValidPassword(binding.password.text.toString()) || (binding.password.text != binding.confirmPassword.text)) {
                                    Toast.makeText(
                                        this,
                                        "Please make sure password has 1 uppercase letter, 1 special character, 1 number, 1 lowercase letter, and matches with the second insert.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    auth.currentUser!!.updatePassword(binding.password.text.toString())
                                        .addOnCompleteListener {
                                            val data: Map<String, Any> = hashMapOf(
                                                "fullName" to binding.fullName.text.toString(),
                                                "chefName" to binding.chefName.text.toString(),
                                                "email" to binding.email.text,
                                                "education" to binding.education.text.toString(),
                                                "city" to "",
                                                "state" to "",
                                                "zipCode" to ""
                                            )
                                            val data1: Map<String, Any> = hashMapOf(
                                                "userName" to binding.chefName.text.toString(),
                                                "email" to binding.email.text.toString(),
                                                "chefOrUser" to "Chef",
                                                "fullName" to binding.fullName.text.toString()
                                            )

                                            db.collection("Chef").document(auth.currentUser!!.uid)
                                                .collection("PersonalInfo").document().update(data)
                                            db.collection("Usernames")
                                                .document(auth.currentUser!!.uid)
                                                .update(data1)
                                            startActivity(intent1)
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

    private fun loadUsernames() {
        db.collection("Usernames").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {

                    val data = doc.data

                    val username = data?.get("username") as String
                    val email = data["email"] as String
                    if (usernames.isEmpty()) {
                        usernames.add(username)
                        emails.add(email)
                    } else {
                        val index = usernames.indexOfFirst { it == username }
                        if (index == -1) {
                            usernames.add(doc.id)
                            emails.add(email)
                        }
                    }

                }
            }
        }
    }

    private fun loadInfo() {

        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("PersonalInfo").get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    for (doc in documents.documents) {
                        val data = doc.data

                        if (data?.get("fullName") != null) {
                            val fullName = data["fullName"] as String
                            val chefName = data["chefName"] as String
                            val email = data["email"] as String
                            val education = data["education"] as String

                            storage.reference.child("chefs/${FirebaseAuth.getInstance().currentUser!!.email!!}/profileImage/${FirebaseAuth.getInstance().currentUser!!.uid}.png").downloadUrl.addOnSuccessListener { imageUri ->
                                Glide.with(this).load(imageUri).placeholder(R.drawable.default_profile).into(binding.userImage)
                            }
                            binding.fullName.setText(fullName)
                            binding.chefName.setText(chefName)
                            binding.email.setText(email)
                            binding.password.setText("*********")
                            binding.confirmPassword.setText("*********")
                            documentId = doc.id
                            binding.education.setText(education)

                        }
                    }
                }

            }
    }
    private fun searchForSpecialChara(search: String) : Boolean {
        val p: Pattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE)
        val m: Matcher = p.matcher(search)
        val b: Boolean = m.find()

        if (b) {
            Log.d(TAG, "There is a special character in my string")
            return true
        } else {
            return false
        }

    }







    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    super.onActivityResult(requestCode, resultCode, data)
    when (resultCode) {
        Activity.RESULT_OK -> {
            //Image Uri will not be null for RESULT_OK
            val uri: Uri = data?.data!!

            chefImage = uri
            if (newOrEdit == "edit") {
                val data5 : Map<String, Any> = hashMapOf("profilePic" to "yes")
                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).update(data5)
                storage.reference.child("chefs/${FirebaseAuth.getInstance().currentUser!!.email!!}/profileImage/${FirebaseAuth.getInstance().currentUser!!.uid}.png")
                    .putFile(uri)
            }
            Glide.with(this).load(uri).placeholder(R.drawable.default_profile).into(binding.userImage)
        }
        ImagePicker.RESULT_ERROR -> {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        }
        else -> {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}
}