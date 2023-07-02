package com.ruh.taiste.chef

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.R
import com.ruh.taiste.chef.models.BusinessBankingInfo
import com.ruh.taiste.chef.models.Person
import com.ruh.taiste.databinding.ActivityAddPersonBankingBinding
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class AddPersonBanking : AppCompatActivity() {
    private lateinit var binding : ActivityAddPersonBankingBinding
    private lateinit var businessBankingInfo: BusinessBankingInfo

    private val db = Firebase.firestore
    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    private lateinit var person : Person
    private var external = ""
    private var newOrEdit = ""
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPersonBankingBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val isPersonExecutiveAdapter = ArrayAdapter(this@AddPersonBanking, R.layout.dropdown_item, resources.getStringArray(R.array.executive_1))




        external = intent.getStringExtra("external").toString()
        newOrEdit = intent.getStringExtra("new_or_edit").toString()

        when (external) {
            "representative" -> {
                @Suppress("DEPRECATION")
                businessBankingInfo = intent.getParcelableExtra("business_banking_info")!!
                person = businessBankingInfo.representative!!
                binding.itemType.text = "Representative"
            }
            "owner1" -> {
                @Suppress("DEPRECATION")
                businessBankingInfo = intent.getParcelableExtra("business_banking_info")!!
                person = businessBankingInfo.owner1!!
                binding.itemType.text = "Owner"
            }
            "owner2" -> {
                @Suppress("DEPRECATION")
                businessBankingInfo = intent.getParcelableExtra("business_banking_info")!!
                person = businessBankingInfo.owner2!!
                binding.itemType.text = "Owner"
            }
            "owner3" -> {
                @Suppress("DEPRECATION")
                businessBankingInfo = intent.getParcelableExtra("business_banking_info")!!
                person = businessBankingInfo.owner3!!
                binding.itemType.text = "Owner"
            }
            "owner4" -> {
                @Suppress("DEPRECATION")
                businessBankingInfo = intent.getParcelableExtra("business_banking_info")!!
                person = businessBankingInfo.owner4!!
                binding.itemType.text = "Owner"
            }
        }
        if (person.isPersonAnExecutive == "Yes" || person.isPersonAnExecutive == "1") {
            binding.executive.setText("Yes") } else { binding.executive.setText("No") }
        if (person.isPersonAnOwner == "Yes" || person.isPersonAnOwner == "1") { binding.owner.setText("Yes") } else { binding.owner.setText("No") }
        binding.executive.setAdapter(isPersonExecutiveAdapter)
        binding.owner.setAdapter(isPersonExecutiveAdapter)
        binding.firstName.setText(person.firstName)
        binding.lastName.setText(person.lastName)
        binding.socialSecurityNumber.setText(person.ssn)
        binding.dobDay.setText(person.dobDay)
        binding.dobMonth.setText(person.dobMonth)
        binding.dobYear.setText(person.dobYear)
        binding.email.setText(person.email)
        binding.phoneNumber.setText(person.phoneNumber)
        binding.streetAddress.setText(person.streetAddress)
        binding.streetAddress2.setText(person.streetAddress2)
        binding.city.setText(person.city)
        binding.state.setText(person.state)
        binding.zipCode.setText(person.zipCode)

        binding.exitButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.save.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            if (external == "representative" && binding.executive.text.toString() != "Yes") {
                Toast.makeText(
                    this,
                    "The representative must be an executive of the company.",
                    Toast.LENGTH_LONG
                ).show()
            } else if (binding.firstName.text.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter a first name in the allotted field.",
                    Toast.LENGTH_LONG
                ).show()
            } else if (binding.lastName.text.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter a last name in the allotted field.",
                    Toast.LENGTH_LONG
                ).show()
            } else if (binding.socialSecurityNumber.text.isEmpty() || binding.socialSecurityNumber.text.length != 9) {
                Toast.makeText(
                    this,
                    "Please enter a valid social security number.",
                    Toast.LENGTH_LONG
                ).show()
            } else if (binding.dobDay.text.isEmpty() || binding.dobDay.text.length != 2 || binding.dobMonth.text.isEmpty() || binding.dobMonth.text.length != 2 || binding.dobYear.text.isEmpty() || binding.dobYear.text.length != 4) {
                Toast.makeText(
                    this,
                    "Please enter a birth date in the following format: '01-01-1990'",
                    Toast.LENGTH_LONG
                ).show()
            } else if (binding.email.text.isEmpty() || !isValidEmail(binding.email.text.toString())) {
                Toast.makeText(this, "Please enter a valid email.", Toast.LENGTH_LONG).show()
            } else if (binding.phoneNumber.text.isEmpty() || binding.phoneNumber.text.length != 10) {
                Toast.makeText(
                    this,
                    "Please enter a valid phone number in the following format: '5555555555'",
                    Toast.LENGTH_LONG
                ).show()
            } else if (binding.streetAddress.text.isEmpty() || binding.city.text.isEmpty() || binding.state.text.isEmpty() || binding.zipCode.text.isEmpty()) {
                Toast.makeText(this, "Please enter a valid street address.", Toast.LENGTH_LONG)
                    .show()
            } else {
                val per = Person(
                    binding.executive.text.toString(),
                    binding.owner.text.toString(),
                    binding.firstName.text.toString(),
                    binding.lastName.text.toString(),
                    binding.email.text.toString(),
                    binding.phoneNumber.text.toString(),
                    binding.dobDay.text.toString(),
                    binding.dobMonth.text.toString(),
                    binding.dobYear.text.toString(),
                    binding.socialSecurityNumber.text.toString(),
                    binding.streetAddress.text.toString(),
                    binding.streetAddress2.text.toString(),
                    binding.city.text.toString(),
                    binding.state.text.toString(),
                    binding.zipCode.text.toString(),
                    binding.executive.text.toString(),
                    binding.owner.text.toString(),
                    businessBankingInfo.stripeId
                )
                if (external == "representative") {
                    businessBankingInfo.representative = per
                    if (binding.owner.text.toString() == "Yes") {

                        if (binding.owner.text.toString() == "Yes") {
                            if (businessBankingInfo.owner1 == null || businessBankingInfo.owner1?.firstName == "" || "${businessBankingInfo.owner1!!.firstName} ${businessBankingInfo.owner1!!.lastName}" == "${binding.firstName.text.toString()} ${binding.lastName.text.toString()}" || businessBankingInfo.representative != null && "${businessBankingInfo.representative!!.firstName} ${businessBankingInfo.representative!!.lastName}" == "${businessBankingInfo.owner1!!.firstName} ${businessBankingInfo.owner1!!.lastName}") {
                                businessBankingInfo.owner1 = per
                            } else if (businessBankingInfo.owner2 == null || businessBankingInfo.owner2?.firstName == "" || "${businessBankingInfo.owner2!!.firstName} ${businessBankingInfo.owner4!!.lastName}" == "${binding.firstName.text.toString()} ${binding.lastName.text.toString()}" || businessBankingInfo.representative != null && "${businessBankingInfo.representative!!.firstName} ${businessBankingInfo.representative!!.lastName}" == "${businessBankingInfo.owner2!!.firstName} ${businessBankingInfo.owner2!!.lastName}") {
                                businessBankingInfo.owner2 = per
                            } else if (businessBankingInfo.owner3 == null || businessBankingInfo.owner3?.firstName == "" || "${businessBankingInfo.owner3!!.firstName} ${businessBankingInfo.owner4!!.lastName}" == "${binding.firstName.text.toString()} ${binding.lastName.text.toString()}" || businessBankingInfo.representative != null && "${businessBankingInfo.representative!!.firstName} ${businessBankingInfo.representative!!.lastName}" == "${businessBankingInfo.owner3!!.firstName} ${businessBankingInfo.owner3!!.lastName}")
                                businessBankingInfo.owner3 = per
                            } else if (businessBankingInfo.owner4 == null || businessBankingInfo.owner4?.firstName == "" || "${businessBankingInfo.owner4!!.firstName} ${businessBankingInfo.owner4!!.lastName}" == "${binding.firstName.text.toString()} ${binding.lastName.text.toString()}" || businessBankingInfo.representative != null && "${businessBankingInfo.representative!!.firstName} ${businessBankingInfo.representative!!.lastName}" == "${businessBankingInfo.owner4!!.firstName} ${businessBankingInfo.owner4!!.lastName}") {
                            businessBankingInfo.owner4 = per
                            }
                        }
                } else if (external == "owner1" && "${businessBankingInfo.representative!!.firstName} ${businessBankingInfo.representative!!.lastName} ${businessBankingInfo.representative!!.ssn}" != "${per.firstName} ${per.lastName} ${per.ssn}") {
                    businessBankingInfo.owner1 = per
                } else if (external == "owner2"  && "${businessBankingInfo.representative!!.firstName} ${businessBankingInfo.representative!!.lastName} ${businessBankingInfo.representative!!.ssn}" != "${per.firstName} ${per.lastName} ${per.ssn}") {
                    businessBankingInfo.owner2 = per
                } else if (external == "owner3"  && "${businessBankingInfo.representative!!.firstName} ${businessBankingInfo.representative!!.lastName} ${businessBankingInfo.representative!!.ssn}" != "${per.firstName} ${per.lastName} ${per.ssn}") {
                    businessBankingInfo.owner3 = per
                } else if (external == "owner4"  && "${businessBankingInfo.representative!!.firstName} ${businessBankingInfo.representative!!.lastName} ${businessBankingInfo.representative!!.ssn}" != "${per.firstName} ${per.lastName} ${per.ssn}") {
                    businessBankingInfo.owner4 = per
                }

                val pers = if (external == "representative") {
                    "representative"
                } else {
                    "owner"
                }
                if (newOrEdit == "edit") {
                    AlertDialog.Builder(this)
                        .setTitle("Update $pers")
                        .setMessage("Are you sure you want to continue? This will delete the current $pers and create a new person with this information.")
                        // if the dialog is cancelable
                        .setCancelable(false)
                        .setPositiveButton("Yes") { dialog, _ ->
                            deletePerson(businessBankingInfo.stripeId, person.personId)
                            createPerson(businessBankingInfo.stripeId)
                            dialog.dismiss()

                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                } else {
                    if (external == "representative") {
                        val intent = Intent(this, Banking::class.java)
                        intent.putExtra("external", "representative")
                        intent.putExtra("business_banking_info", businessBankingInfo)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, Owners::class.java)
                        intent.putExtra("external", "owner")
                        intent.putExtra("business_banking_info", businessBankingInfo)
                        startActivity(intent)
                    }

                }
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun createPerson(stripeId: String) {
        val intent = Intent(this, Banking::class.java)
        intent.putExtra("external", "external")
        intent.putExtra("business_banking_info", businessBankingInfo)
        val intent1 = Intent(this, Owners::class.java)
        intent1.putExtra("external", "external")
        intent1.putExtra("business_banking_info", businessBankingInfo)

        val representative = if (external == "representative") { "true" } else { "false" }
        val owner = if (external == "representative") { if (person.isPersonAnOwner == "true" || person.isPersonAnOwner == "1") { "true" } else { "false" } } else { "true" }
        val executive = if (person.isPersonAnExecutive != "true" || person.isPersonAnExecutive == "0") { "false" } else { "true" }
        val title = if (external == "representative") { "Executive" } else { "Owner" }


        binding.progressBar.isVisible = true
        val body = FormBody.Builder()
            .add("account_id", stripeId)
            .add("first_name", person.firstName)
            .add("last_name", person.lastName)
            .add("dob_day", person.dobDay)
            .add("dob_month", person.dobMonth)
            .add("dob_year", person.dobYear)
            .add("line_1", person.streetAddress)
            .add("line_2", person.streetAddress2)
            .add("postal_code", person.zipCode)
            .add("city", person.city)
            .add("state", person.state)
            .add("email", person.email)
            .add("phone", person.phoneNumber)
            .add("id_number", person.ssn)
            .add("title", title)
            .add("representative", representative)
            .add("owner", owner)
            .add("executive", executive)
            .build()


        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/create-person")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    displayAlert("Error: $e")
                }

                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        displayAlert(
                            "Error: $response"
                        )
                    } else {
                        val responseData = response.body!!.string()
                        val json =
                            JSONObject(responseData)
                        val id = json.getString("id")

                        mHandler.post {

                            if (external == "representative") {
                                val data : Map<String, Any> = hashMapOf("representativeId" to id)
                                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("BankingInfo").document(businessBankingInfo.documentId).update(data)
                                Toast.makeText(this@AddPersonBanking, "Saved successfully.", Toast.LENGTH_LONG).show()
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@AddPersonBanking, "Saved successfully.", Toast.LENGTH_LONG).show()
                                startActivity(intent1)
                                finish()
                            }
                            Toast.makeText(this@AddPersonBanking, "Person added.", Toast.LENGTH_LONG).show()


                        }
                    }
                }
            })

    }
    private fun deletePerson(stripeId: String, personId: String) {

        binding.progressBar.isVisible = true
        val body = FormBody.Builder()
            .add("stripeAccountId", stripeId)
            .add("personId", personId)
            .build()


        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/delete-person")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    displayAlert("Error: $e")
                }

                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        displayAlert(
                            "Error: $response"
                        )
                    } else {
                        mHandler.post {
                            if (external == "representative") {
                                val data: Map<String, Any> = hashMapOf("representativeId" to "")
                                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("BankingInfo").document(businessBankingInfo.documentId).update(data)
                            }
                            Toast.makeText(this@AddPersonBanking, "Person deleted.", Toast.LENGTH_LONG)
                                .show()
                            binding.progressBar.isVisible = false

                        }
                    }
                }
            })

    }
    private fun displayAlert(
        message: String
    ) {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
                .setTitle("Failed to load page.")
                .setMessage(message)

            builder.setPositiveButton("Ok", null)
            builder
                .create()
                .show()
        }
    }

}