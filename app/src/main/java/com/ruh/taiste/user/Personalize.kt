package com.ruh.taiste.user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.R
import com.ruh.taiste.both.isValidPassword
import com.ruh.taiste.chef.isValidEmail
import com.ruh.taiste.databinding.ActivityPersonalizeBinding
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


private const val TAG = "Personalize"
class Personalize : AppCompatActivity() {
    private lateinit var binding : ActivityPersonalizeBinding

    private val db = Firebase.firestore
    private var storage = Firebase.storage

    private lateinit var auth: FirebaseAuth

    private var personalizeDocumentId = ""
    private var preferenceDocumentId = ""
    private var pictureId = UUID.randomUUID().toString()

    private var originalUsername = "user${UUID.randomUUID().toString().take(5)}"
    private var imageChanged = ""

    private var userImage = Uri.EMPTY

    private var local = 1
    private var region = 0
    private var nation = 0

    private var surpriseMe = 0
    private var burger = 0
    private var creative = 0
    private var healthy = 0
    private var lowCal = 0
    private var lowCarb = 0
    private var vegan = 0
    private var workout = 0
    private var seafood = 0
    private var pasta = 0

    private var usernames : MutableList<String> = arrayListOf()
    private var emails : MutableList<String> = arrayListOf()

    var newOrEdit = "new"

    var newChef = ""

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalizeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = Firebase.auth
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(this)
            } else {
                isOnline1(this)
            }
        ) {
            loadUsernames()
        } else {
            Toast.makeText(this, "Please make sure to be connected to the internet before proceeding.", Toast.LENGTH_LONG).show()
        }

        binding.progressBar.isVisible = false


        newChef = intent.getStringExtra("new_chef").toString()
        if (newChef == "yes") {
            binding.email.setText(FirebaseAuth.getInstance().currentUser!!.email!!)
            binding.password.setText("*********")
            binding.confirmPassword.setText("*********")
            binding.email.isEnabled = false
            binding.password.isEnabled = false
            binding.confirmPassword.isEnabled = false
        }
        newOrEdit = intent.getStringExtra("new_or_edit").toString()
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(this)
            } else {
                isOnline1(this)
            }
        ) {
            loadUsernames()
        }
        if (newOrEdit == "edit") {
            binding.email.isEnabled = false
            loadPersonalInfo()
        }
        else {
            Toast.makeText(this, "Please make sure to be connected to the internet before proceeding.", Toast.LENGTH_LONG).show()
        }
        binding.local.setOnClickListener {
            local = 1
            region = 0
            nation = 0
            binding.local.isSelected = true
            binding.region.isSelected = false
            binding.nation.isSelected = false

            binding.cityLayout.visibility = View.VISIBLE
            binding.city.visibility = View.VISIBLE

            binding.local.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.local.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.region.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.region.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.nation.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.nation.setBackgroundColor(ContextCompat.getColor(this, R.color.white))

        }

        binding.region.setOnClickListener {
            local = 0
            region = 1
            nation = 0
            binding.local.isSelected = false
            binding.region.isSelected = true
            binding.nation.isSelected = false

            binding.cityLayout.visibility = View.VISIBLE
            binding.city.visibility = View.GONE

            binding.local.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.local.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.region.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.region.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.nation.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.nation.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }

        binding.nation.setOnClickListener {
            local = 0
            region = 0
            nation = 1
            binding.local.isSelected = false
            binding.region.isSelected = false
            binding.nation.isSelected = true

            binding.cityLayout.visibility = View.GONE

            binding.local.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.local.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.region.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.region.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.nation.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.nation.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))

        }

        binding.surpriseMe.setOnClickListener {
            binding.surpriseMe.isSelected = !binding.surpriseMe.isSelected
            surpriseMe = if (binding.surpriseMe.isSelected) {
                binding.surpriseMe.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.surpriseMe.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.surpriseMe.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.surpriseMe.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }

        binding.workout.setOnClickListener {
            binding.workout.isSelected = !binding.workout.isSelected
            workout = if (binding.workout.isSelected) {
                binding.workout.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.workout.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.workout.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.workout.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }

        binding.creative.setOnClickListener {
            binding.creative.isSelected = !binding.creative.isSelected
            creative = if (binding.creative.isSelected) {
                binding.creative.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.creative.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.creative.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.creative.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }

        binding.healthy.setOnClickListener {
            binding.healthy.isSelected = !binding.healthy.isSelected
            healthy = if (binding.healthy.isSelected) {
                binding.healthy.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.healthy.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.healthy.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.healthy.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }

        binding.lowCal.setOnClickListener {
            binding.lowCal.isSelected = !binding.lowCal.isSelected
            lowCal = if (binding.lowCal.isSelected) {
                binding.lowCal.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.lowCal.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.lowCal.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.lowCal.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }

        binding.lowCarb.setOnClickListener {
            binding.lowCarb.isSelected = !binding.lowCarb.isSelected
            lowCarb = if (binding.lowCarb.isSelected) {
                binding.lowCarb.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.lowCarb.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.lowCarb.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.lowCarb.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }

        binding.vegan.setOnClickListener {
            binding.vegan.isSelected = !binding.vegan.isSelected
            vegan = if (binding.vegan.isSelected) {
                binding.vegan.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.vegan.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.vegan.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.vegan.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }

        binding.pasta.setOnClickListener {
            binding.pasta.isSelected = !binding.pasta.isSelected
            pasta = if (binding.pasta.isSelected) {
                binding.pasta.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.pasta.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.pasta.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.pasta.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }

        binding.seafood.setOnClickListener {
            binding.seafood.isSelected = !binding.seafood.isSelected
            seafood = if (binding.seafood.isSelected) {
                binding.seafood.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.seafood.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.seafood.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.seafood.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }

        binding.burger.setOnClickListener {

            binding.burger.isSelected = !binding.burger.isSelected
            burger = if (binding.burger.isSelected) {
                binding.burger.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.burger.setTextColor(ContextCompat.getColor(this, R.color.white))
                1
            } else {
                binding.burger.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.burger.setTextColor(ContextCompat.getColor(this, R.color.main))
                0
            }
        }

        binding.editImage.setOnClickListener {
            ImagePicker.with(this)
                .crop()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(
                    1080,
                    1080
                )    //Final image resolution will be less than 1080 x 1080(Optional)
                .start()

        }

        binding.saveButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {
                saveData()
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadPersonalInfo() {
        db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("PersonalInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    personalizeDocumentId = doc.id
                    val fullName = data?.get("fullName") as String
                    val userName = data["userName"] as String
                    val city = data["city"] as String
                    val state = data["state"] as String
                    val email = data["email"] as String
                    val local = data["local"] as Number
                    val region = data["region"] as Number
                    val nation = data["nation"] as Number
                    val burger = data["burger"] as Number
                    val creative = data["creative"] as Number
                    val healthy = data["healthy"] as Number
                    val lowCal = data["lowCal"] as Number
                    val lowCarb = data["lowCarb"] as Number
                    val pasta = data["pasta"] as Number
                    val seafood = data["seafood"] as Number
                    val vegan = data["vegan"] as Number
                    val workout = data["workout"] as Number
                    val surpriseMe = data["surpriseMe"] as Number


                    originalUsername = userName
                    binding.fullName.setText(fullName)
                    binding.userName.setText(userName)
                    binding.city.setText(city)
                    binding.state.setText(state)
                    binding.email.setText(email)
                    binding.password.setText("*********")
                    binding.confirmPassword.setText("*********")

                    this.local = local.toInt()
                    this.region = region.toInt()
                    this.nation = nation.toInt()
                    this.burger = burger.toInt()
                    this.creative = creative.toInt()
                    this.healthy = healthy.toInt()
                    this.lowCal = lowCal.toInt()
                    this.lowCarb = lowCarb.toInt()
                    this.pasta = pasta.toInt()
                    this.seafood = seafood.toInt()
                    this.vegan = vegan.toInt()
                    this.workout = workout.toInt()
                    this.surpriseMe = surpriseMe.toInt()



                    if (local.toInt() == 1) {
                        binding.local.isSelected = true
                        binding.cityLayout.visibility = View.VISIBLE
                        binding.city.visibility = View.VISIBLE

                        binding.local.setTextColor(ContextCompat.getColor(this, R.color.white))
                        binding.local.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                    } else {
                        binding.local.isSelected = false
                        binding.local.setTextColor(ContextCompat.getColor(this, R.color.main))
                        binding.local.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    }

                    if (region.toInt() == 1) {
                        binding.region.isSelected = true
                        binding.nation.isSelected = false

                        binding.cityLayout.visibility = View.VISIBLE
                        binding.city.visibility = View.GONE

                        binding.region.setTextColor(ContextCompat.getColor(this, R.color.white))
                        binding.region.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))

                    } else {
                        binding.region.isSelected = false
                        binding.region.setTextColor(ContextCompat.getColor(this, R.color.main))
                        binding.region.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    }

                    if (nation.toInt() == 1) {
                        binding.cityLayout.visibility = View.GONE
                        binding.nation.isSelected = true
                        binding.nation.setTextColor(ContextCompat.getColor(this, R.color.white))
                        binding.nation.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                    } else {
                        binding.nation.isSelected = false
                        binding.nation.setTextColor(ContextCompat.getColor(this, R.color.main))
                        binding.nation.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    }

                    if (surpriseMe.toInt() == 1) {
                        binding.surpriseMe.isSelected = true
                        binding.surpriseMe.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                        binding.surpriseMe.setTextColor(ContextCompat.getColor(this, R.color.white))
                    } else {
                        binding.surpriseMe.isSelected = false
                        binding.surpriseMe.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                        binding.surpriseMe.setTextColor(ContextCompat.getColor(this, R.color.main))
                    }

                    if (burger.toInt() == 1) {
                        binding.burger.isSelected = true
                        binding.burger.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                        binding.burger.setTextColor(ContextCompat.getColor(this, R.color.white))
                    } else {
                        binding.burger.isSelected = false
                        binding.burger.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                        binding.burger.setTextColor(ContextCompat.getColor(this, R.color.main))
                    }

                    if (creative.toInt() == 1) {
                        binding.creative.isSelected = true
                        binding.creative.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                        binding.creative.setTextColor(ContextCompat.getColor(this, R.color.white))
                    } else {
                        binding.creative.isSelected = false
                        binding.creative.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                        binding.creative.setTextColor(ContextCompat.getColor(this, R.color.main))
                    }

                    if (healthy.toInt() == 1) {
                        binding.healthy.isSelected = true
                        binding.healthy.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                        binding.healthy.setTextColor(ContextCompat.getColor(this, R.color.white))
                    } else {
                        binding.healthy.isSelected = false
                        binding.healthy.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                        binding.healthy.setTextColor(ContextCompat.getColor(this, R.color.main))
                    }

                    if (lowCarb.toInt() == 1) {
                        binding.lowCarb.isSelected = true
                        binding.lowCarb.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                        binding.lowCarb.setTextColor(ContextCompat.getColor(this, R.color.white))
                    } else {
                        binding.lowCarb.isSelected = false
                        binding.lowCarb.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                        binding.lowCarb.setTextColor(ContextCompat.getColor(this, R.color.main))
                    }

                    if (pasta.toInt() == 1) {
                        binding.pasta.isSelected = true
                        binding.pasta.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                        binding.pasta.setTextColor(ContextCompat.getColor(this, R.color.white))
                    } else {
                        binding.pasta.isSelected = false
                        binding.pasta.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                        binding.pasta.setTextColor(ContextCompat.getColor(this, R.color.main))
                    }

                    if (seafood.toInt() == 1) {
                        binding.seafood.isSelected = true
                        binding.seafood.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                        binding.seafood.setTextColor(ContextCompat.getColor(this, R.color.white))
                    } else {
                        binding.seafood.isSelected = false
                        binding.seafood.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                        binding.seafood.setTextColor(ContextCompat.getColor(this, R.color.main))
                    }

                    if (vegan.toInt() == 1) {
                        binding.vegan.isSelected = true
                        binding.vegan.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                        binding.vegan.setTextColor(ContextCompat.getColor(this, R.color.white))
                    } else {
                        binding.vegan.isSelected = false
                        binding.vegan.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                        binding.vegan.setTextColor(ContextCompat.getColor(this, R.color.main))
                    }

                    if (workout.toInt() == 1) {
                        binding.workout.isSelected = true
                        binding.workout.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                        binding.workout.setTextColor(ContextCompat.getColor(this, R.color.white))
                    } else {
                        binding.workout.isSelected = false
                        binding.workout.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                        binding.workout.setTextColor(ContextCompat.getColor(this, R.color.main))
                    }

                    if (lowCal.toInt() == 1) {
                        binding.lowCal.isSelected = true
                        binding.lowCal.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                        binding.lowCal.setTextColor(ContextCompat.getColor(this, R.color.white))
                    } else {
                        binding.lowCal.isSelected = false
                        binding.lowCal.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                        binding.lowCal.setTextColor(ContextCompat.getColor(this, R.color.main))
                    }


                        storage.reference.child("users/${FirebaseAuth.getInstance().currentUser!!.email!!}/profileImage/${FirebaseAuth.getInstance().currentUser!!.uid}.png").downloadUrl.addOnSuccessListener { userUri ->
                            userImage = userUri
                            Glide.with(this).load(userUri).placeholder(R.drawable.default_profile).into(binding.userImage)

                    }
                }
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

    private fun saveData() {
        val city = if (binding.city.text.isEmpty()) { "" } else { binding.city.text.toString() }
        val state = if (binding.state.text.isEmpty()) { "" } else { binding.state.text.toString() }
        if (newChef == "yes") {
            if (binding.fullName.text.isEmpty()) {
                Toast.makeText(this, "Please enter your full name", Toast.LENGTH_LONG).show()
            } else if (binding.userName.text.isEmpty() || (binding.userName.text.length < 4)) {
                Toast.makeText(this, "Please enter a username with at least 4 characters.", Toast.LENGTH_LONG).show()
            } else if (usernames.contains(binding.userName.text.toString())) {
                Toast.makeText(this, "This username already exists. Please try again.", Toast.LENGTH_LONG).show()
            } else if (binding.email.text.isEmpty() || !isValidEmail(binding.email.text.toString())) {
                Toast.makeText(this, "Please enter your valid email.", Toast.LENGTH_LONG).show()
            } else if (searchForSpecialChara(binding.userName.text.toString())) {
                Toast.makeText(this, "Please remove all special characters from your userName.", Toast.LENGTH_LONG).show()
            } else if (emails.contains(binding.email.text.toString())) {
                Toast.makeText(this, "This is email is already taken. If this is your email, please log in.", Toast.LENGTH_LONG).show()
            } else if (binding.userName.text.isEmpty() || binding.userName.text.contains(" ")) {
                Toast.makeText(this, "Please enter your userName with no spaces.", Toast.LENGTH_LONG).show()
            } else if (!isValidPassword(binding.password.text.toString()) && (binding.password.text.isEmpty() || binding.confirmPassword.text.isEmpty())) {
                Toast.makeText(this, "Please make sure password has 1 uppercase letter, 1 special character, 1 number, 1 lowercase letter, and matches with the second insert.", Toast.LENGTH_LONG).show()
            } else if (local == 1 && (binding.city.text.isEmpty() || binding.state.text.isEmpty())) {
                Toast.makeText(this, "Please enter a city and state.", Toast.LENGTH_LONG).show()
            } else if (region == 1 && (binding.state.text.isEmpty())) {
                Toast.makeText(this, "Please enter a state", Toast.LENGTH_LONG).show()
            } else if ((local == 1) || (region == 1) && stateFilter(binding.state.text.toString()) != "good") {
                Toast.makeText(this, "Please enter the abbreviation of your state.", Toast.LENGTH_LONG).show()
            } else {
                val data: Map<String, Any> = hashMapOf(
                    "local" to local,
                    "region" to region,
                    "nation" to nation,
                    "burger" to burger,
                    "creative" to creative,
                    "healthy" to healthy,
                    "lowCal" to lowCal,
                    "lowCarb" to lowCarb,
                    "vegan" to vegan,
                    "workout" to workout,
                    "seafood" to seafood,
                    "pasta" to pasta,
                    "city" to city,
                    "state" to state,
                    "fullName" to binding.fullName.text.toString(),
                    "userName" to binding.userName.text.toString(),
                    "email" to binding.email.text.toString(),
                    "city" to binding.city.text.toString(),
                    "state" to binding.state.text.toString(),
                    "surpriseMe" to surpriseMe
                )
                val data1: Map<String, Any> = hashMapOf(
                    "fullName" to binding.fullName.text.toString(),
                    "username" to binding.userName.text.toString(),
                    "chefOrUser" to "User",
                    "email" to binding.email.text.toString()
                )
                var profilePic = ""
                val data2: Map<String, Any> = hashMapOf("chefOrUser" to "User", "notificationToken" to "", "notifications" to "", "profilePic" to profilePic)
                val user = auth.currentUser
                val profileUpdates = userProfileChangeRequest {
                    displayName = "User"
                }
                user!!.updateProfile(profileUpdates).addOnCompleteListener { task1 ->
                    if (userImage != Uri.EMPTY) {
                        profilePic = "yes"
                        storage.reference.child("users/${FirebaseAuth.getInstance().currentUser!!.email!!}/profileImage/${FirebaseAuth.getInstance().currentUser!!.uid}.png").putFile(userImage!!)
                    } else {
                        profilePic = "no"
                        val imageUri = Uri.parse("android.resource://com.ruh.taiste/" + R.drawable.default_profile)
                        storage.reference.child("users/${FirebaseAuth.getInstance().currentUser!!.email!!}/profileImage/${FirebaseAuth.getInstance().currentUser!!.uid}.png").putFile(imageUri)

                    }
                    db.collection("User").document(auth.currentUser!!.uid)
                        .collection("PersonalInfo")
                        .document().set(data)
                    db.collection("User").document(auth.currentUser!!.uid)
                        .set(data2)
                    Toast.makeText(
                        this,
                        "Information saved.",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(intent)
                    finish()
                }

            }
        } else {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("where_to", "home")
            if (binding.fullName.text.isEmpty()) {
                Toast.makeText(this, "Please enter your full name", Toast.LENGTH_LONG).show()
            } else if (binding.userName.text.isEmpty() || (binding.userName.text.length < 4)) {
                Toast.makeText(this, "Please enter a username with at least 4 characters.", Toast.LENGTH_LONG).show()
            } else if (usernames.contains(binding.userName.text.toString())) {
                Toast.makeText(this, "This username already exists. Please try again.", Toast.LENGTH_LONG).show()
            } else if (searchForSpecialChara(binding.userName.text.toString())) {
                Toast.makeText(this, "Please remove all special characters from your userName.", Toast.LENGTH_LONG).show()
            } else if (binding.email.text.isEmpty() || !isValidEmail(binding.email.text.toString())) {
                Toast.makeText(this, "Please enter your valid email.", Toast.LENGTH_LONG).show()
            } else if (emails.contains(binding.email.text.toString())) {
                Toast.makeText(this, "This is email is already taken. If this is your email, please log in.", Toast.LENGTH_LONG).show()
            } else if (binding.userName.text.isEmpty() || binding.userName.text.contains(" ")) {
                Toast.makeText(this, "Please enter your userName with no spaces.", Toast.LENGTH_LONG).show()
            } else if (!isValidPassword(binding.password.text.toString()) && (binding.password.text.isEmpty() || binding.confirmPassword.text.isEmpty())) {
                Toast.makeText(this, "Please make sure password has 1 uppercase letter, 1 special character, 1 number, 1 lowercase letter, and matches with the second insert.", Toast.LENGTH_LONG).show()
            } else if (local == 1 && (binding.city.text.isEmpty() || binding.state.text.isEmpty())) {
                Toast.makeText(this, "Please enter a city and state.", Toast.LENGTH_LONG).show()
            } else if (region == 1 && (binding.state.text.isEmpty())) {
                Toast.makeText(this, "Please enter a state", Toast.LENGTH_LONG).show()
            } else if ((local == 1) || (region == 1) && stateFilter(binding.state.text.toString()) != "good") {
                Toast.makeText(this, "Please enter the abbreviation of your state.", Toast.LENGTH_LONG).show()
            } else {
                val data: Map<String, Any> = hashMapOf(
                    "local" to local,
                    "region" to region,
                    "nation" to nation,
                    "burger" to burger,
                    "creative" to creative,
                    "healthy" to healthy,
                    "lowCal" to lowCal,
                    "lowCarb" to lowCarb,
                    "vegan" to vegan,
                    "workout" to workout,
                    "seafood" to seafood,
                    "pasta" to pasta,
                    "city" to city,
                    "state" to state,
                    "fullName" to binding.fullName.text.toString(),
                    "userName" to binding.userName.text.toString(),
                    "email" to binding.email.text.toString(),
                    "city" to binding.city.text.toString(),
                    "state" to binding.state.text.toString(),
                    "surpriseMe" to surpriseMe
                )
                val data1: Map<String, Any> = hashMapOf(
                    "fullName" to binding.fullName.text.toString(),
                    "username" to binding.userName.text.toString(),
                    "chefOrUser" to "User",
                    "email" to binding.email.text.toString()
                )
                var profilePic = ""
                val data2: Map<String, Any> = hashMapOf("chefOrUser" to "User", "notificationToken" to "", "notifications" to "", "profilePic" to profilePic)
                if (newOrEdit != "edit") {
                    auth.createUserWithEmailAndPassword(
                        binding.email.text.toString(),
                        binding.password.text.toString()
                    )
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {

                                val user = auth.currentUser

                                val profileUpdates = userProfileChangeRequest {
                                    displayName = "User"
                                }

                                user!!.updateProfile(profileUpdates)
                                    .addOnCompleteListener { task1 ->


                                        if (userImage != Uri.EMPTY) {
                                            profilePic = "yes"
                                            storage.reference.child("users/${FirebaseAuth.getInstance().currentUser!!.email!!}/profileImage/${FirebaseAuth.getInstance().currentUser!!.uid}.png").putFile(userImage!!)
                                        } else {
                                            val imageUri = Uri.parse("android.resource://com.ruh.taiste/" + R.drawable.default_profile)
                                            profilePic = "no"
                                            storage.reference.child("users/${FirebaseAuth.getInstance().currentUser!!.email!!}/profileImage/${FirebaseAuth.getInstance().currentUser!!.uid}.png").putFile(imageUri)
                                        }
                                        db.collection("User").document(auth.currentUser!!.uid)
                                            .collection("PersonalInfo")
                                            .document().set(data)
                                        db.collection("Usernames").document(auth.currentUser!!.uid)
                                            .set(data1)
                                        db.collection("User").document(auth.currentUser!!.uid)
                                            .set(data2)
                                        Toast.makeText(
                                            this,
                                            "Information saved.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        startActivity(intent)
                                        finish()
                                    }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Something went wrong. Please try again later.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                } else {
                    if (!binding.password.text.contains("*")) {
                        auth.currentUser!!.updatePassword(binding.password.text.toString())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    db.collection("User").document(auth.currentUser!!.uid)
                                        .collection("PersonalInfo")
                                        .document(personalizeDocumentId).update(data)
                                    db.collection("Usernames").document(auth.currentUser!!.uid)
                                        .update(data1)
                                    db.collection("User").document(auth.currentUser!!.uid)
                                        .update(data2)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Your password could not be updated. Please try again later.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    } else {
                        db.collection("User").document(auth.currentUser!!.uid)
                            .collection("PersonalInfo")
                            .document(personalizeDocumentId).update(data)
                        db.collection("Usernames").document(auth.currentUser!!.uid)
                            .update(data1)
                        db.collection("User").document(auth.currentUser!!.uid).update(data2)
                        startActivity(intent)
                        finish()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                val uri: Uri = data?.data!!

                userImage = uri
                imageChanged = "Yes"
                if (newOrEdit == "edit") {
                    val data5 : Map<String, Any> = hashMapOf("profilePic" to "yes")
                    db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).update(data5)
                    storage.reference.child("users/${FirebaseAuth.getInstance().currentUser!!.email!!}/profileImage/${FirebaseAuth.getInstance().currentUser!!.uid}.png").putFile(uri)
                    Toast.makeText(this, "Image Uploaded.", Toast.LENGTH_LONG).show()
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