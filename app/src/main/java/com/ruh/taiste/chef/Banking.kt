package com.ruh.taiste.chef

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.ruh.taiste.R
import com.ruh.taiste.chef.models.*
import com.ruh.taiste.databinding.ActivityBankingBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import com.ruh.taiste.user.models.FeedMenuItems
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.sql.Timestamp
import java.time.Instant.now
import java.time.LocalDate.now
import java.util.*


private const val TAG = "Banking"

class Banking : AppCompatActivity() {
    private lateinit var binding: ActivityBankingBinding

    private val db = Firebase.firestore

    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())


    //Bank
    private lateinit var bottomSheetDialog: BottomSheetDialog

    private lateinit var bankName: EditText
    private lateinit var accountHolder: EditText
    private lateinit var accountNumber: EditText
    private lateinit var routingNumber: EditText
    private lateinit var exitButton: TextView
    private lateinit var saveButton: MaterialButton
    private lateinit var deleteButton: MaterialButton
    private lateinit var disclaimerText: TextView
    private lateinit var primaryAccountLayout: LinearLayout
    private lateinit var primaryYes: MaterialButton
    private lateinit var primaryNo: MaterialButton


    private var bankAccount: BankAccount = BankAccount("", "", "","","")
    private var bankAccountEdit = ""

    var representative: Person? = null
    var owners: MutableList<Person> = arrayListOf()


    private var newOrEdit = "new"

    private var documentId = ""
    private var ip = ""
    private var stripeId = ""
    private var accountType = ""

    private var update = ""

    lateinit var businessBankingInfo : BusinessBankingInfo

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBankingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ip = Utils.getIPAddress(true)
        Log.d(TAG, "onCreate: ip ${Utils.getIPAddress(true)}")
        newOrEdit = intent.getStringExtra("new_or_edit").toString()
        var ext = BankAccount("","","","","")
        var per = Person("","","","","","","","","","","","","","","","","","")
        businessBankingInfo = BusinessBankingInfo("", "","", "", "", "", "", "", "", "", "", "", "", ext, per, per, per, per, per)
        if (intent.getStringExtra("external") != null) {
            @Suppress("DEPRECATION")
            businessBankingInfo = intent.getParcelableExtra("business_banking_info")!!
            binding.individualLayout.isVisible = false
            binding.businessLayout.isVisible = true
            binding.businessButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.businessButton.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.individualButton.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.individualButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white))

            if (businessBankingInfo.termsOfServiceAccept == "Yes") {
                binding.termsOfServiceAcceptImageB.setImageResource(R.drawable.circle_checked)
                binding.termsOfServiceAcceptTextB.text = "Accepted!"
            } else {
                binding.termsOfServiceAcceptImageB.setImageResource(R.drawable.circle_unchecked)
                binding.termsOfServiceAcceptTextB.text = "Do you accept?"
            }
            binding.mccCodeB.setText(businessBankingInfo.mcc)
            binding.businessUrlB.setText(businessBankingInfo.url)
            binding.companyName.setText(businessBankingInfo.name)
            binding.phoneB.setText(businessBankingInfo.phone)
            binding.streetAddressB.setText(businessBankingInfo.streetAddress)
            binding.streetAddress2B.setText(businessBankingInfo.streetAddress2)
            binding.cityB.setText(businessBankingInfo.city)
            binding.stateB.setText(businessBankingInfo.state)
            binding.zipCodeB.setText(businessBankingInfo.zipCode)
            binding.taxId.setText(businessBankingInfo.companyTaxId)
            if (businessBankingInfo.externalAccount != null) {
                if (businessBankingInfo.externalAccount!!.bankName != "") {
                    binding.externalAccountTextB.text =
                        businessBankingInfo.externalAccount!!.accountNumber
                }
            }
            if (businessBankingInfo.representative != null) {
                binding.representativeTextB.text = "${businessBankingInfo.representative!!.firstName} ${businessBankingInfo.representative!!.lastName}"
            }

        } else {
            if (newOrEdit == "edit") {
                if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        isOnline(this)
                    } else {
                        isOnline1(this)
                    }
                ) {

                if (FirebaseAuth.getInstance().currentUser != null) {
                    loadBankingInfo()
                } else {
                    Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
                }
                } else {
                    Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
                }
                binding.deleteAccountButton.isVisible = true
            }
        }





        //Bank
        bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(this)
            .inflate(R.layout.banking_bottom_sheet, R.id.banking_bottom_sheet as? RelativeLayout)

        bankName = bottomSheetView.findViewById(R.id.bank_name)
        accountHolder = bottomSheetView.findViewById(R.id.account_holder)
        accountNumber = bottomSheetView.findViewById(R.id.account_number)
        routingNumber = bottomSheetView.findViewById(R.id.routing_number)
        exitButton = bottomSheetView.findViewById(R.id.exit_button)
        saveButton = bottomSheetView.findViewById(R.id.add_button)

        bottomSheetDialog.setContentView(bottomSheetView)

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        exitButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        binding.individualButton.setOnClickListener {
            accountType = "Individual"
            if (newOrEdit == "edit") {
                AlertDialog.Builder(this)
                    .setTitle("Account Switch")
                    .setMessage("Are you sure you want to continue? This will delete your stripe bank account.")
                    // if the dialog is cancelable
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, _ ->
                        if (FirebaseAuth.getInstance().currentUser != null) {
                            deleteAccount(stripeId)
                        } else {
                            Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
                        }
                        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("BankingInfo").document(documentId).delete()
                        binding.individualLayout.visibility = View.VISIBLE
                        binding.businessLayout.visibility = View.GONE
                        binding.individualButton.setTextColor(ContextCompat.getColor(this, R.color.white))
                        binding.individualButton.setBackgroundColor(
                            ContextCompat.getColor(
                                this,
                                R.color.secondary
                            )
                        )
                        binding.businessButton.setTextColor(ContextCompat.getColor(this, R.color.main))
                        binding.businessButton.setBackgroundColor(
                            ContextCompat.getColor(
                                this,
                                R.color.white
                            )
                        )
                        Toast.makeText(this, "Stripe Account Deleted.", Toast.LENGTH_LONG).show()
                        dialog.dismiss()
                        startActivity(intent)

                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            } else {
                binding.individualLayout.visibility = View.VISIBLE
                binding.businessLayout.visibility = View.GONE
                binding.individualButton.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.individualButton.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.secondary
                    )
                )
                binding.businessButton.setTextColor(ContextCompat.getColor(this, R.color.main))
                binding.businessButton.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.white
                    )
                )
            }
        }

        binding.businessButton.setOnClickListener {
            accountType = "Business"
            if (newOrEdit == "edit") {
                AlertDialog.Builder(this)
                    .setTitle("Account Switch")
                    .setMessage("Are you sure you want to continue? This will delete your stripe bank account.")
                    // if the dialog is cancelable
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, _ ->
                        deleteAccount(stripeId)
                        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("BankingInfo").document(documentId).delete()
                        binding.individualLayout.visibility = View.GONE
                        binding.businessLayout.visibility = View.VISIBLE
                        binding.individualButton.setTextColor(ContextCompat.getColor(this, R.color.main))
                        binding.individualButton.setBackgroundColor(
                            ContextCompat.getColor(
                                this,
                                R.color.white
                            )
                        )
                        binding.businessButton.setTextColor(ContextCompat.getColor(this, R.color.white))
                        binding.businessButton.setBackgroundColor(
                            ContextCompat.getColor(
                                this,
                                R.color.secondary
                            )
                        )
                        Toast.makeText(this, "Stripe Account Deleted.", Toast.LENGTH_LONG).show()
                        dialog.dismiss()
                        startActivity(intent)

                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            } else {
                binding.individualLayout.visibility = View.GONE
                binding.businessLayout.visibility = View.VISIBLE
                binding.individualButton.setTextColor(ContextCompat.getColor(this, R.color.main))
                binding.individualButton.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.white
                    )
                )
                binding.businessButton.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.businessButton.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.secondary
                    )
                )
                    }
        }
        //Bank
        binding.externalAccountEditButton.setOnClickListener {
            bankAccountEdit = "0"
            if (businessBankingInfo.externalAccount != null ) {
                bankName.setText(businessBankingInfo.externalAccount!!.bankName)
                accountHolder.setText(businessBankingInfo.externalAccount!!.accountHolder)
                if (businessBankingInfo.externalAccount!!.accountNumber != "") {
                    accountNumber.setText("****${businessBankingInfo.externalAccount!!.accountNumber}")
                    routingNumber.setText(businessBankingInfo.externalAccount!!.routingNumber)
                }
            }
//                primaryAccountLayout.visibility = View.VISIBLE

            if (newOrEdit == "edit") {
                saveButton.text = "Update"
            }
            if (newOrEdit == "edit") {
                bankName.isEnabled = false
                accountHolder.isEnabled = false
                accountNumber.isEnabled = false
                routingNumber.isEnabled = false
            }
            bottomSheetDialog.show()
        }


        binding.externalAccountEditButtonB.setOnClickListener {
            accountType = "Business"
            bankAccountEdit = "0"
            if (businessBankingInfo.externalAccount != null ) {
                bankName.setText(businessBankingInfo.externalAccount!!.bankName)
                accountHolder.setText(businessBankingInfo.externalAccount!!.accountHolder)
                if (businessBankingInfo.externalAccount!!.accountNumber != "") {
                    accountNumber.setText("****${businessBankingInfo.externalAccount!!.accountNumber}")
                    routingNumber.setText(businessBankingInfo.externalAccount!!.routingNumber)
                }
            }
//                primaryAccountLayout.visibility = View.VISIBLE

            if (newOrEdit == "edit") {
                saveButton.text = "Update"
            }
//            deleteButton.isVisible = true
            if (newOrEdit == "edit") {
                bankName.isEnabled = false
                accountHolder.isEnabled = false
                accountNumber.isEnabled = false
                routingNumber.isEnabled = false
            }
            bottomSheetDialog.show()

        }

        saveButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            if (bankName.text.isEmpty()) {
                Toast.makeText(this, "Please enter a bank name in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (accountHolder.text.isEmpty()) {
                Toast.makeText(this, "Please enter an account holder in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (accountNumber.text.isEmpty()) {
                Toast.makeText(this, "Please enter a valid account number in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (routingNumber.text.isEmpty() || routingNumber.text.length != 9) {
                Toast.makeText(this, "Please enter a valid routing number in the allotted field.", Toast.LENGTH_LONG).show()
            } else {
                if (newOrEdit != "edit") {
                        bankAccount = BankAccount(bankName.text.toString(), accountHolder.text.toString(), accountNumber.text.toString(), routingNumber.text.toString(), UUID.randomUUID().toString())
                        if (accountType == "Business") {
                            businessBankingInfo.externalAccount = bankAccount
                            binding.externalAccountLayoutB.visibility = View.VISIBLE
                            binding.externalAccountTextB.text = "Account #: ${bankAccount.accountNumber.toString()}"
                        } else {
                            binding.externalAccountLayout.visibility = View.VISIBLE
                            binding.externalAccountText.text = "Account #: ${bankAccount.accountNumber.toString()}"
                        }
                    bottomSheetDialog.dismiss()
                } else {

                    AlertDialog.Builder(this)
                        .setMessage("This will delete your old external account and create a new one with this information. Continue?")
                        // if the dialog is cancelable
                        .setCancelable(false)
                        .setPositiveButton("Yes") { dialog, _ ->
                            deleteExternalAccount(stripeId, bankAccount.externalAccountId)
                            createExternalAccount(stripeId, accountType)
                            dialog.dismiss()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()

                }

            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }

        }
        binding.clickHereToView.setOnClickListener {
            val intent = Intent(this, StripeTermsOfService::class.java)
            startActivity(intent)
            binding.doYouAcceptLayout.visibility = View.VISIBLE
        }

        binding.clickHereToViewB.setOnClickListener {
            val intent = Intent(this, StripeTermsOfService::class.java)
            startActivity(intent)
            binding.doYouAcceptLayoutB.visibility = View.VISIBLE
        }

        binding.doYouAcceptLayout.setOnClickListener {
            binding.doYouAcceptLayout.isSelected = !binding.doYouAcceptLayout.isSelected
            if (binding.doYouAcceptLayout.isSelected) {
                binding.termsOfServiceAcceptText.text = "Accepted!"
                binding.termsOfServiceAcceptImage.setImageResource(R.drawable.circle_checked)
            } else {
                binding.termsOfServiceAcceptText.text = "Do you accept?"
                binding.termsOfServiceAcceptImage.setImageResource(R.drawable.circle_unchecked)
            }
        }

        binding.doYouAcceptLayoutB.setOnClickListener {
            binding.doYouAcceptLayoutB.isSelected = !binding.doYouAcceptLayoutB.isSelected
            if (binding.doYouAcceptLayoutB.isSelected) {
                binding.termsOfServiceAcceptTextB.text = "Accepted!"
                binding.termsOfServiceAcceptImageB.setImageResource(R.drawable.circle_checked)
            } else {
                binding.termsOfServiceAcceptTextB.text = "Do you accept?"
                binding.termsOfServiceAcceptImageB.setImageResource(R.drawable.circle_unchecked)
            }
        }

        binding.ownerButton.setOnClickListener {
           val terms = if (binding.termsOfServiceAcceptTextB.text.toString() == "Accepted!") { "Yes" } else { "No" }
            businessBankingInfo = BusinessBankingInfo(businessBankingInfo.stripeId, terms, binding.mccCodeB.text.toString(), binding.companyName.text.toString(), binding.businessUrlB.text.toString(), binding.streetAddressB.text.toString(), binding.streetAddress2B.text.toString(), binding.cityB.text.toString(), binding.stateB.text.toString(), binding.zipCodeB.text.toString(), binding.phoneB.text.toString(), binding.taxId.text.toString(), businessBankingInfo.documentId, businessBankingInfo.externalAccount, businessBankingInfo.representative, businessBankingInfo.owner1, businessBankingInfo.owner2, businessBankingInfo.owner3, businessBankingInfo.owner4)
            val intent = Intent(this@Banking, Owners::class.java)
            intent.putExtra("external", "external")
            intent.putExtra("new_or_edit", newOrEdit)
            intent.putExtra("business_banking_info", businessBankingInfo)
            startActivity(intent)
        }
        binding.representative.setOnClickListener {
            val terms = if (binding.termsOfServiceAcceptTextB.text.toString() == "Accepted!") { "Yes" } else { "No" }
            businessBankingInfo = BusinessBankingInfo(businessBankingInfo.stripeId, terms, binding.mccCodeB.text.toString(), binding.companyName.text.toString(), binding.businessUrlB.text.toString(), binding.streetAddressB.text.toString(), binding.streetAddress2B.text.toString(), binding.cityB.text.toString(), binding.stateB.text.toString(), binding.zipCodeB.text.toString(), binding.phoneB.text.toString(), binding.taxId.text.toString(), businessBankingInfo.documentId, businessBankingInfo.externalAccount, businessBankingInfo.representative, businessBankingInfo.owner1, businessBankingInfo.owner2, businessBankingInfo.owner3, businessBankingInfo.owner4)
            val intent = Intent(this@Banking, AddPersonBanking::class.java)
            intent.putExtra("external", "representative")
            intent.putExtra("new_or_edit", newOrEdit)
            intent.putExtra("business_banking_info", businessBankingInfo)
            startActivity(intent)
        }
        binding.representativeEditButtonB.setOnClickListener {
            val terms = if (binding.termsOfServiceAcceptTextB.text.toString() == "Accepted!") { "Yes" } else { "No" }
            businessBankingInfo = BusinessBankingInfo(businessBankingInfo.stripeId, terms, binding.mccCodeB.text.toString(), binding.companyName.text.toString(), binding.businessUrlB.text.toString(), binding.streetAddressB.text.toString(), binding.streetAddress2B.text.toString(), binding.cityB.text.toString(), binding.stateB.text.toString(), binding.zipCodeB.text.toString(), binding.phoneB.text.toString(), binding.taxId.text.toString(), businessBankingInfo.documentId, businessBankingInfo.externalAccount, businessBankingInfo.representative, businessBankingInfo.owner1, businessBankingInfo.owner2, businessBankingInfo.owner3, businessBankingInfo.owner4)
            val intent = Intent(this@Banking, AddPersonBanking::class.java)
            intent.putExtra("external", "representative")
            intent.putExtra("new_or_edit", newOrEdit)
            intent.putExtra("business_banking_info", businessBankingInfo)
            startActivity(intent)
        }

        binding.saveButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            if (FirebaseAuth.getInstance().currentUser != null) {
                if (binding.termsOfServiceAcceptText.text != "Accepted!") {
                    Toast.makeText(
                        this,
                        "In order to use our services at this time, you must accept Stripe's terms of service. If for any reason you have a problem with banking while using Bloom, please contact us and we'll help resolve it.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.mccCode.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please enter your mcc code in the allotted field.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.businessUrl.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please enter your business url in the allotted field.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.firstName.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please enter your first name in the allotted field.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.lastName.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please enter your last name in the allotted field.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.email.text.isEmpty() || !isValidEmail(binding.email.text.toString())) {
                    Toast.makeText(
                        this,
                        "Please enter your email address in the allotted field.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.phone.text.isEmpty() || binding.phone.text.length != 10) {
                    Toast.makeText(
                        this,
                        "Please enter your phone in the following format: 5555555555.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if ((binding.dobDay.text.isEmpty() || binding.dobDay.text.length != 2) || (binding.dobMonth.text.isEmpty() || binding.dobMonth.text.length != 2) || (binding.dobYear.text.isEmpty() || binding.dobYear.text.length != 4)) {
                    Toast.makeText(
                        this,
                        "Please enter your date of birth in the following format: 01-01-2001.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.streetAddress.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please enter your street address in the allotted field.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.city.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please enter your city in the allotted field.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.state.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please enter your state in the allotted field.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.zipCode.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please enter your zip code in the allotted field.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.socialSecurityNumber.text.isEmpty() || binding.socialSecurityNumber.text.length != 9) {
                    Toast.makeText(
                        this,
                        "Please enter your ssn in the following format: 1111111111.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (bankAccount.bankName == "") {
                    Toast.makeText(this, "Please enter your bank account info.", Toast.LENGTH_LONG)
                        .show()
                } else {
                    if (newOrEdit == "edit") {
                        updateIndividualAccount()
                    } else {
                        createIndividualAccount()

                    }
                }
            } else {
                Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.saveButtonB.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            if (FirebaseAuth.getInstance().currentUser != null) {
                if (binding.termsOfServiceAcceptTextB.text != "Accepted!") {
                    Toast.makeText(
                        this,
                        "In order to use our services at this time, you must accept Stripe's terms of service. If for any reason you have a problem with banking while using Bloom, please contact us and we'll help resolve it.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.mccCodeB.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please enter your mcc code in the allotted field.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.businessUrlB.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please enter your business url in the allotted field.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.companyName.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please enter your company name in the allotted field.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.phoneB.text.isEmpty() || binding.phoneB.text.length != 10) {
                    Toast.makeText(
                        this,
                        "Please enter your company phone in the following format: 5555555555.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.streetAddressB.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please enter your company street address in the allotted field.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.cityB.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please enter your company city in the allotted field.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.stateB.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please enter your company state in the allotted field.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.zipCodeB.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please enter your company zip code in the allotted field.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (binding.taxId.text.isEmpty() || binding.taxId.text.length != 9) {
                    Toast.makeText(
                        this,
                        "Please enter your company tax id following format: 123456789.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (businessBankingInfo.representative == null || businessBankingInfo.representative!!.firstName == "") {
                    Toast.makeText(
                        this,
                        "Please select a representative to represent this account.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (businessBankingInfo.owner1 == null || businessBankingInfo.owner1!!.firstName == "") {
                    Toast.makeText(
                        this,
                        "Please include at least one owner in the allotted field.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (businessBankingInfo.externalAccount == null || businessBankingInfo.externalAccount!!.routingNumber.length != 9) {
                    Toast.makeText(
                        this,
                        "Please include the external account where you'd like to receive payouts.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    if (newOrEdit == "edit") {
                        updateBusinessAccount()
                    } else {
                        Log.d(TAG, "onCreate: this is happening.")
                        createBusinessAccount()
                    }

                }
            } else {
                Toast.makeText(this, "Something went wrong. Please check your connection", Toast.LENGTH_LONG).show()
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
         }

        binding.deleteAccountButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Before doing this, Please make sure to not have any payouts. Are you sure you want to continue?")
                // if the dialog is cancelable
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, _ ->
                    if (FirebaseAuth.getInstance().currentUser != null) {
                        deleteAccount(stripeId)
                    } else {
                        Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
                    }
                    dialog.dismiss()
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


    private fun loadBankingInfo() {
        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("BankingInfo").get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    for (doc in documents.documents) {
                        val data = doc.data

                            val accountType = data?.get("accountType") as String
                            val stripeId = data["stripeAccountId"] as String
                            val externalAccount = data["externalAccountId"] as String
                            documentId = doc.id
                            this.accountType = accountType

                        this.stripeId = stripeId
                            if (accountType == "Individual") {
                                binding.termsOfServiceAcceptText.text = "Accepted!"
                                binding.businessLayout.visibility = View.GONE
                                binding.individualLayout.visibility = View.VISIBLE
                                loadIndividualAccount(stripeId, externalAccount)
                                binding.businessButton.isSelected = false
                                binding.individualButton.isSelected = true
                                binding.individualButton.setTextColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.white
                                    )
                                )
                                binding.individualButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.secondary
                                    )
                                )
                                binding.businessButton.setTextColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.main
                                    )
                                )
                                binding.businessButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.white
                                    )
                                )
                                binding.businessLayout.visibility = View.GONE
                                binding.individualLayout.visibility = View.VISIBLE
                            } else {
                                binding.businessLayout.visibility = View.VISIBLE
                                val representativeId = data["representativeId"] as String
                                loadBusinessAccount(stripeId, representativeId, externalAccount, doc.id)
                                binding.businessButton.isSelected = true
                                binding.individualButton.isSelected = false
                                binding.businessButton.setTextColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.white
                                    )
                                )
                                binding.businessButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.secondary
                                    )
                                )
                                binding.individualButton.setTextColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.main
                                    )
                                )
                                binding.individualButton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.white
                                    )
                                )
                                binding.individualLayout.visibility = View.GONE
                                binding.businessLayout.visibility = View.VISIBLE


                            }


                    }
                }
            }
    }

    private fun loadIndividualAccount(stripeId: String, externalAccountId: String) {
        binding.progressBar.isVisible = true
        val body = FormBody.Builder()
            .add("stripeAccountId", stripeId)
            .build()

        val request = Request.Builder()
            .url("https://ruh.herokuapp.com/retrieve-individual-account")
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

                        val mcc = json.getString("mcc")
                        val url = json.getString("url")
                        val firstName = json.getString("first_name")
                        val lastName = json.getString("last_name")
                        val phone = json.getString("phone")
                        val email = json.getString("email")
                        val dobDay = json.getString("dob_day")
                        val dobMonth = json.getString("dob_month")
                        val dobYear = json.getString("dob_year")
                        val streetAddress = json.getString("line1")
                        val streetAddress2 = json.getString("line2")
                        val postalCode = json.getString("postal_code")
                        val city = json.getString("city")
                        val state = json.getString("state")


                        mHandler.post {

                            val day = if (dobDay.toString().toInt() < 10) { "0$dobDay"} else { dobDay }
                            val month = if (dobMonth.toString().toInt() < 10) { "0$dobMonth"} else { dobMonth }
                            binding.mccCode.setText(mcc)
                            binding.businessUrl.setText(url)
                            binding.firstName.setText(firstName)
                            binding.lastName.setText(lastName)
                            binding.phone.setText(phone)
                            binding.email.setText(email)
                            binding.dobDay.setText(day)
                            binding.dobMonth.setText(month)
                            binding.dobYear.setText(dobYear)
                            binding.streetAddress.setText(streetAddress)
                            binding.streetAddress2.setText(streetAddress2)
                            binding.city.setText(city)
                            binding.state.setText(state)
                            binding.zipCode.setText(postalCode)
                            binding.socialSecurityNumber.setText("*********")

                            retrieveExternalAccount(stripeId, externalAccountId, "Individual")

                            binding.progressBar.isVisible = false
                        }
                    }
                }
            })

    }

    private fun loadBusinessAccount(stripeId: String, representativeId: String, externalAccountId: String, documentId: String) {

        binding.progressBar.isVisible = true
        val body = FormBody.Builder()
            .add("stripeAccountId", stripeId)
            .build()

        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/retrieve-business-account")
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

                        val mcc = json.getString("mcc")
                        val name = json.getString("name")
                        val url = json.getString("url")
                        val streetAddress = json.getString("company_line1")
                        val streetAddress2 = json.getString("company_line2")
                        val city = json.getString("company_city")
                        val state = json.getString("company_state")
                        val zipCode = json.getString("company_postal_code")
                        val phone = json.getString("phone")

                        val persons = json.getJSONArray("persons")

                        mHandler.post {
                            binding.progressBar.isVisible = false
                            val ext = BankAccount("", "", "", "", "")
                            val per = Person("", "", "","", "", "", "", "", "", "", "", "", "", "", "", "", "", "")
                            businessBankingInfo = BusinessBankingInfo(stripeId, "Yes", mcc, name, url, streetAddress, streetAddress2, city, state, zipCode, phone, "*********", documentId, ext, per, per, per, per, per)

                            binding.mccCodeB.setText(mcc)
                            binding.businessUrlB.setText(url)
                            binding.companyName.setText(name)
                            binding.streetAddressB.setText(streetAddress)
                            binding.streetAddress2B.setText(streetAddress2)
                            binding.cityB.setText(city)
                            binding.stateB.setText(state)
                            binding.zipCodeB.setText(zipCode)
                            binding.phoneB.setText(phone)
                            binding.taxId.setText("*********")
                            binding.progressBar.isVisible = false


                            retrievePerson(stripeId, representativeId, "representative")
                            retrieveExternalAccount(stripeId, externalAccountId, "Business")

                            Log.d(TAG, "onResponse: persons.leng${persons.length()}")
                            for (i in 0 until persons.length()) {
                                var one = i + 1
                                retrievePerson(stripeId, persons.getJSONObject(i)["id"].toString(), "owner$i")
                            }
                        }
                    }
                }
            })

    }

    private fun retrievePerson(stripeId: String, personId: String, representativeOrOwner: String) {

        val body = FormBody.Builder()
            .add("stripeAccountId", stripeId)
            .add("personId", personId)
            .build()

        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/retrieve-person")
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

                        val firstName = json.getString("first_name")
                        val lastName = json.getString("last_name")
                        val email = json.getString("email")
                        val phoneNumber = json.getString("phone_number")
                        val dobDay = json.getString("dob_day")
                        val dobMonth = json.getString("dob_month")
                        val dobYear = json.getString("dob_year")
                        val streetAddress = json.getString("street_address")
                        val streetAddress2 = json.getString("street_address_2")
                        val city = json.getString("city")
                        val state = json.getString("state")
                        val zipCode = json.getString("zip_code")
                        val executive = json.getString("executive")
                        val owner = json.getString("owner")

                        mHandler.post {

                            val executive1 = if (executive == "true" || executive == "1") { "Yes" } else { "No" }
                            val owner1 = if (owner == "true" || owner == "1") { "Yes" } else { "No" }
                            val person = Person(executive1, owner1, firstName, lastName, email, phoneNumber, dobDay, dobMonth, dobYear, "*********", streetAddress, streetAddress2, city, state, zipCode, executive, owner, personId)
                            when (representativeOrOwner) {
                                "representative" -> {
                                    businessBankingInfo.representative = person
                                    binding.representativeTextB.text = "$firstName $lastName"
                                }

                            }
                            if (owner1 == "Yes") {
                                when {
                                    businessBankingInfo.owner1?.firstName == "" -> {
                                        businessBankingInfo.owner1 = person
                                    }
                                    businessBankingInfo.owner2?.firstName == "" -> {
                                        businessBankingInfo.owner2 = person
                                    }
                                    businessBankingInfo.owner3?.firstName == "" -> {
                                        businessBankingInfo.owner3 = person
                                    }
                                    businessBankingInfo.owner4?.firstName == "" -> {
                                        businessBankingInfo.owner4 = person
                                    }
                                }
                            }


                            Log.d(TAG, "onResponse: executive $executive")
                            Log.d(TAG, "onResponse: owner $owner")

                        }
                    }
                }
            })

    }

    private fun retrieveExternalAccount(stripeId: String, externalAccountId: String, accountType: String) {

        val body = FormBody.Builder()
            .add("stripeAccountId", stripeId)
            .add("externalAccountId", externalAccountId)
            .build()

        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/retrieve-external-account")
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

                        val bankName = json.getString("bank_name")
                        val accountHolder = json.getString("account_holder")
                        val accountNumber = json.getString("account_number")
                        val routingNumber = json.getString("routing_number")
                        val defaultForCurrency = json.getString("default_for_currency")


                        mHandler.post {

                            Log.d(TAG, "onResponse: $bankName")
                            Log.d(TAG, "onResponse: $accountHolder")
                            Log.d(TAG, "onResponse: $accountNumber")
                            Log.d(TAG, "onResponse: $routingNumber")
                            Log.d(TAG, "onResponse: $defaultForCurrency")
                            val bankAccount =
                                BankAccount(
                                    bankName,
                                    accountHolder,
                                    accountNumber,
                                    routingNumber,
                                    externalAccountId
                                )
                            if (accountType == "Individual") {
                                binding.externalAccountLayout.visibility = View.VISIBLE
                                binding.externalAccountText.text = "****$accountNumber"
                            } else {
                                businessBankingInfo.externalAccount = bankAccount
                                binding.externalAccountLayoutB.visibility = View.VISIBLE
                                binding.externalAccountTextB.text = "****$accountNumber"
                            }



                        }
                    }
                }
            })

    }

    private fun createIndividualAccount() {
        val intent = Intent(this@Banking, MainActivity::class.java)
        intent.putExtra("where_to", "home")

        val date = Calendar.getInstance().timeInMillis / 1000
            binding.progressBar.isVisible = true
            val body = FormBody.Builder()
                .add("mcc", binding.mccCode.text.toString())
                .add("url", binding.businessUrl.text.toString())
                .add("date", "$date")
                .add("ip", ip)
                .add("first_name", binding.firstName.text.toString())
                .add("last_name", binding.lastName.text.toString())
                .add("dob_day", binding.dobDay.text.toString())
                .add("dob_month", binding.dobMonth.text.toString())
                .add("dob_year", binding.dobYear.text.toString())
                .add("line_1", binding.streetAddress.text.toString())
                .add("line_2", binding.streetAddress2.text.toString())
                .add("postal_code", binding.zipCode.text.toString())
                .add("city", binding.city.text.toString())
                .add("state", binding.state.text.toString())
                .add("email", binding.email.text.toString())
                .add("phone", binding.phone.text.toString())
                .add("ssn", binding.socialSecurityNumber.text.toString())
                .add("account_holder", bankAccount.accountHolder)
                .add("account_number", bankAccount.accountNumber.toString())
                .add("routing_number", bankAccount.routingNumber.toString())
                .build()

            val request = Request.Builder()
                .url("https://taiste-payments.onrender.com/create-individual-account")
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
                            val externalAccountId = json.getString("external_account")


                            mHandler.post {
                                val data: Map<String, Any> = hashMapOf("accountType" to "Individual", "stripeAccountId" to id, "externalAccountId" to externalAccountId)
                                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("BankingInfo").document().set(data)

                                    Toast.makeText(this@Banking, "Saved successfully.", Toast.LENGTH_LONG).show()
                                    startActivity(intent)
                                finish()


                                binding.progressBar.isVisible = false
                            }
                        }
                    }
                })

        }

    private fun createBusinessAccount() {

        val intent = Intent(this@Banking, MainActivity::class.java)
        intent.putExtra("where_to", "home")


        binding.progressBar.isVisible = true
        val representative = businessBankingInfo.representative!!
        var vari = false
        if  (businessBankingInfo.representative!!.isPersonAnOwner == "Yes") { vari = true }

        val date = Calendar.getInstance().timeInMillis / 1000

        Log.d(TAG, "createBusinessAccount: ip $ip")
        Log.d(TAG, "createBusinessAccount: date $date")
        Log.d(TAG, "createBusinessAccount: rep $representative")
        Log.d(TAG, "createBusinessAccount: bus $businessBankingInfo")




        val body = FormBody.Builder()
            .add("mcc", businessBankingInfo.mcc)
            .add("url", businessBankingInfo.url)
            .add("date", "$date")
            .add("ip", ip)
            .add("company_name", businessBankingInfo.name)
            .add("company_line1", businessBankingInfo.streetAddress)
            .add("company_line2", businessBankingInfo.streetAddress2)
            .add("company_postal_code", businessBankingInfo.zipCode)
            .add("company_city", businessBankingInfo.city)
            .add("company_state", businessBankingInfo.state)
            .add("company_phone", businessBankingInfo.phone)
            .add("company_tax_id", businessBankingInfo.companyTaxId)
            .add("account_holder", businessBankingInfo.externalAccount!!.accountHolder)
            .add("account_number", businessBankingInfo.externalAccount!!.accountNumber)
            .add("routing_number", businessBankingInfo.externalAccount!!.routingNumber)
            .add("representative_first_name", representative.firstName)
            .add("representative_last_name", representative.lastName)
            .add("representative_dob_day", representative.dobDay)
            .add("representative_dob_month", representative.dobMonth)
            .add("representative_dob_year", representative.dobYear)
            .add("representative_line_1", representative.streetAddress)
            .add("representative_line_2", representative.streetAddress2)
            .add("representative_city", representative.city)
            .add("representative_state", representative.state)
            .add("representative_postal_code", representative.zipCode)
            .add("representative_email", representative.email)
            .add("representative_phone", representative.phoneNumber)
            .add("representative_id_number", representative.ssn)
            .add("representative_title", "Executive")
            .add("representative", "true")
            .add("representative_owner", "$vari")
            .add("representative_executive", "true")
            .build()

        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/create-business-account")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()


        httpClient.newCall(request)
            .enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    displayAlert("Error: $e")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        displayAlert(
                            "Error: $response"
                        )
                    } else {
                        val responseData = response.body!!.string()
                        val json =
                            JSONObject(responseData)

                        val id = json.getString("stripeId")
                        val externalAccountId = json.getString("bankAccountId")
                        val representativeId = json.getString("representativeId")

                        mHandler.post {
                            val data: Map<String, Any> = hashMapOf("stripeAccountId" to id, "externalAccountId" to externalAccountId, "representativeId" to representativeId, "accountType" to "Business")
                            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("BankingInfo").document().set(data)

                            Toast.makeText(this@Banking, "Saved successfully.", Toast.LENGTH_LONG).show()

                            val index = owners.indexOfFirst { "${it.firstName} ${it.lastName} ${it.email}" == "${representative.firstName} ${representative.lastName} ${representative.email}" }

                            var owner : Person?
                            var rep = businessBankingInfo.representative
                            var end = ""
                            for (i in 0 until 4) {
                                if (i == 0) {
                                    owner = businessBankingInfo.owner1
                                    if (owner!!.firstName != "") {
                                        if ("${owner.firstName} ${owner.lastName} ${owner.ssn}" != "${rep!!.firstName} ${rep.lastName} ${rep.ssn}") {
                                            if (businessBankingInfo.owner2!!.firstName == "") {
                                                end = "end"
                                            }
                                            createPerson(id, "owner", owner, end)
                                        }
                                    }
                                } else if (i == 1 && end != "end") {
                                    owner = businessBankingInfo.owner2
                                    if (owner!!.firstName != "") {
                                        if ("${owner.firstName} ${owner.lastName} ${owner.ssn}" != "${rep!!.firstName} ${rep.lastName} ${rep.ssn}") {
                                            if (businessBankingInfo.owner3!!.firstName == "") {
                                                end = "end"
                                            }
                                            createPerson(id, "owner", owner, end)
                                        }
                                    }
                                } else if (i == 2 && end != "end") {
                                    owner = businessBankingInfo.owner3
                                    if (owner!!.firstName != "") {
                                        if ("${owner.firstName} ${owner.lastName} ${owner.ssn}" != "${rep!!.firstName} ${rep.lastName} ${rep.ssn}") {
                                            if (businessBankingInfo.owner4!!.firstName == "") {
                                                end = "end"
                                            }
                                            createPerson(id, "owner", owner, end)
                                        }
                                    }
                                } else if (i == 3) {
                                    owner = businessBankingInfo.owner4
                                    if (owner!!.firstName != "") {
                                        if ("${owner.firstName} ${owner.lastName} ${owner.ssn}" != "${rep!!.firstName} ${rep.lastName} ${rep.ssn}") {

                                            createPerson(id, "owner", owner, "end")
                                        }
                                    }
                                }
                            }

                            startActivity(intent)
                            finish()

                            binding.progressBar.isVisible = false
                        }
                    }
                }
            })
    }



    private fun updateIndividualAccount() {

            val intent = Intent(this@Banking, MainActivity::class.java)
            intent.putExtra("where_to", "home")
            binding.progressBar.isVisible = true
            val body = FormBody.Builder()
                .add("mcc", binding.mccCode.text.toString())
                .add("url", binding.businessUrl.text.toString())
                .add("first_name", binding.firstName.text.toString())
                .add("last_name", binding.lastName.text.toString())
                .add("dob_day", binding.dobDay.text.toString())
                .add("dob_month", binding.dobMonth.text.toString())
                .add("dob_year", binding.dobYear.text.toString())
                .add("line1", binding.streetAddress.text.toString())
                .add("line2", binding.streetAddress2.text.toString())
                .add("postal_code", binding.zipCode.text.toString())
                .add("city", binding.city.text.toString())
                .add("state", binding.state.text.toString())
                .add("email", binding.email.text.toString())
                .add("phone", binding.phone.text.toString())
                .build()

            val request = Request.Builder()
                .url("https://taiste-payments.onrender.com/update-individual-account")
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

                                    Toast.makeText(this@Banking, "Update successful.", Toast.LENGTH_LONG).show()
                                    startActivity(intent)
                                    finish()


                                binding.progressBar.isVisible = false
                            }
                        }
                    }
                })

        }

    private fun updateBusinessAccount() {
        val intent = Intent(this@Banking, MainActivity::class.java)

        binding.progressBar.isVisible = true
        val body = FormBody.Builder()
            .add("mcc", binding.mccCodeB.text.toString())
            .add("url", binding.businessUrlB.text.toString())
            .add("company_name", binding.companyName.text.toString())
            .add("company_line1", binding.streetAddressB.text.toString())
            .add("company_line2", binding.streetAddress2B.text.toString())
            .add("company_postal_code", binding.zipCodeB.text.toString())
            .add("company_city", binding.cityB.text.toString())
            .add("company_state", binding.stateB.text.toString())
            .add("company_phone", binding.phoneB.text.toString())
            .add("company_tax_id", binding.taxId.text.toString())
            .build()

        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/update-business-account")
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
                            Toast.makeText(this@Banking, "Update successful.", Toast.LENGTH_LONG).show()
                            startActivity(intent)
                            finish()
                            binding.progressBar.isVisible = false
                        }
                    }
                }
            })

    }

    private fun createExternalAccount(stripeId: String, accountType: String) {
        val intent = Intent(this@Banking, MainActivity::class.java)
        intent.putExtra("where_to", "home")
        binding.progressBar.isVisible = true
        val body = FormBody.Builder()
            .add("stripeAccountId", stripeId)
            .add("account_holder", bankAccount.accountHolder)
            .add("account_holder_type", accountType)
            .add("routing_number", bankAccount.routingNumber.toString())
            .add("account_number", bankAccount.accountNumber.toString())
            .build()

        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/create-bank-account")
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

                        val externalAccount = json.getString("externalAccount")


                        mHandler.post {
                            if (newOrEdit == "new") {
                                if (accountType == "Individual") {
                                    Toast.makeText(this@Banking, "Saved successfully! Please check your banking status for more information on your banking status.", Toast.LENGTH_LONG).show()
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                val data : Map<String, Any> = hashMapOf("externalAccountId" to externalAccount)
                                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("BankingInfo").document(documentId).update(data)
                                Toast.makeText(this@Banking, "Saved successfully!", Toast.LENGTH_LONG).show()
                                bottomSheetDialog.dismiss()
                            }
                        }
                    }
                }
            })

    }

    private fun createPerson(stripeId: String, representativeOrOwner: String, person: Person, end: String) {

        val representative = if (representativeOrOwner == "representative") { "true" } else { "false" }
        val owner = if (person.isPersonAnOwner == "Yes") { "true" } else { "false" }
        val executive = if (person.isPersonAnExecutive == "Yes") { "false" } else { "true" }


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
            .add("title", "Owner")
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

                            if (end == "true") {
                                Toast.makeText(this@Banking, "Saved successfully.", Toast.LENGTH_LONG).show()
                                startActivity(intent)
                                finish()
                            }
                            if (representativeOrOwner == "representative") {
                                val data : Map<String, Any> = hashMapOf("representativeId" to id)
                                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("BankingInfo").document(documentId).update(data)
                            }
                            Toast.makeText(this@Banking, "Person added.", Toast.LENGTH_LONG).show()


                        }
                    }
                }
            })

    }

//    private fun updatePerson(stripeId: String, representativeOrOwner: String, personId: String) {
//
//        val title = if (representativeOrOwner == "representative") {
//            titleInCompany.text.toString()
//        } else {
//            "Owner"
//        }
//        val representative = if (representativeOrOwner == "representative") {
//            "true"
//        } else {
//            "false"
//        }
//        val owner = if (representative == "representative") {
//            if (position.text.toString() == "Owner") {
//                "true"
//            } else {
//                "false"
//            }
//        } else {
//            "true"
//        }
//        val executive = if (representative == "representative") {
//            if (position.text.toString() == "Owner") {
//                "false"
//            } else {
//                "true"
//            }
//        } else {
//            "false"
//        }
//        binding.progressBar.isVisible = true
//        val body = FormBody.Builder()
//            .add("stripeAccountId", stripeId)
//            .add("personId", personId)
//            .add("first_name", firstName.text.toString())
//            .add("last_name", lastName.text.toString())
//            .add("dob_day", dobDay.text.toString())
//            .add("dob_month", dobMonth.text.toString())
//            .add("dob_year", dobYear.text.toString())
//            .add("line_1", streetAddress.text.toString())
//            .add("line_2", streetAddress2.text.toString())
//            .add("postal_code", zipCode.text.toString())
//            .add("city", city.text.toString())
//            .add("state", state.text.toString())
//            .add("email", email.text.toString())
//            .add("phone", phoneNumber.text.toString())
//            .add("title", title)
//            .add("representative", representative)
//            .add("owner", owner)
//            .add("executive", executive)
//            .build()
//
//
//        val request = Request.Builder()
//            .url("http://10.34.87.96:4242/update-person")
//            .addHeader("Content-Type", "application/json; charset=utf-8")
//            .post(body)
//            .build()
//
//        httpClient.newCall(request)
//            .enqueue(object : Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    displayAlert("Error: $e")
//                }
//
//                @SuppressLint("SetTextI18n")
//                override fun onResponse(call: Call, response: Response) {
//                    if (!response.isSuccessful) {
//                        displayAlert(
//                            "Error: $response"
//                        )
//                    } else {
//
//                        mHandler.post {
//                            Toast.makeText(this@Banking, "Person updated.", Toast.LENGTH_LONG)
//                                .show()
//                            if (bottomSheetDialog1.isShowing) {
//                                bottomSheetDialog1.dismiss()
//                            }
//                        }
//                    }
//                }
//            })
//
//    }



    private fun deleteAccount(stripeId: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("where_to", "home")
            binding.progressBar.isVisible = true
            val body = FormBody.Builder()
                .add("stripeAccountId", stripeId)
                .build()


            val request = Request.Builder()
                .url("https://taiste-payments.onrender.com/delete-account")
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(body)
                .build()

            httpClient.newCall(request)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        displayAlert( "Error: $e")
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onResponse(call: Call, response: Response) {
                        if (!response.isSuccessful) {
                            displayAlert(
                                "Error: $response"
                            )
                        } else {
                            mHandler.post {
                                Toast.makeText(this@Banking, "Account Deleted. Please make sure to replace this account before continuing.", Toast.LENGTH_LONG).show()
                                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("BankingInfo").document(documentId).delete()
                                binding.progressBar.isVisible = false
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                })
    }

    private fun deleteExternalAccount(stripeId: String, externalAccountId: String) {
        binding.progressBar.isVisible = true
        val body = FormBody.Builder()
            .add("stripeAccountId", stripeId)
            .add("externalAccountId", externalAccountId)
            .build()

        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/delete-bank-account")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    displayAlert( "Error: $e")
                }

                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        displayAlert(
                            "Error: $response"
                        )
                    } else {
                        mHandler.post {
                            bottomSheetDialog.dismiss()
                                binding.externalAccountLayoutB.visibility = View.GONE
                            val data : Map<String, Any> = hashMapOf("externalAccountId" to "")
                            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("BankingInfo").document(documentId).update(data)
                            Toast.makeText(
                                this@Banking,
                                "External account deleted.",
                                Toast.LENGTH_LONG
                            ).show()
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

fun isValidEmail(target: CharSequence?): Boolean {
    return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target!!).matches()
}