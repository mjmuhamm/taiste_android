package com.ruh.taiste.user

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.ruh.taiste.R
import com.ruh.taiste.databinding.ActivityCheckoutBinding
import com.ruh.taiste.user.adapters.checkout.CheckoutAdapter
import com.ruh.taiste.user.models.CheckoutItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.both.guserName
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import com.ruh.taiste.user.models.Credits
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UNCHECKED_CAST")
class Checkout : AppCompatActivity(), ClickListener {

    private lateinit var binding: ActivityCheckoutBinding

    private val backEndUrl = "https://taiste-payments.onrender.com/create-payment-intent"
    private val httpClient = OkHttpClient()

    private var foodTotal = 0.0
    private var newFoodTotal = 0.0
    private lateinit var paymentSheet: PaymentSheet
    private lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    private lateinit var paymentIntentClientSecret: String
    private lateinit var publishableKey: String

    private var credits : MutableList<Credits> = arrayListOf()
    private var newCredits : MutableList<Credits> = arrayListOf()
    private var creditsIds : MutableList<String> = arrayListOf()

    private var paymentId = ""


    private var creditsApplied = ""

    private val db = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser!!.email!!
    private val storage = Firebase.storage


    private val mHandler: Handler = Handler(Looper.getMainLooper())

    private val checkoutItems : MutableList<CheckoutItems> = ArrayList()
    private lateinit var checkoutAdapter : CheckoutAdapter

    private var listener = ""
    private var cant = ""
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        binding.checkoutRecyclerView.layoutManager = LinearLayoutManager(this)
        checkoutAdapter = CheckoutAdapter(this, checkoutItems, this)
        binding.checkoutRecyclerView.adapter = checkoutAdapter

        binding.backButton.setOnClickListener {
            if (listener == "yes") {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("where_to", "home")
                startActivity(intent)
            } else {
                onBackPressedDispatcher.onBackPressed()
            }
        }


        binding.payButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            if (FirebaseAuth.getInstance().currentUser != null) {
                if (binding.totalPrice.text == "$0.00") {
                    AlertDialog.Builder(this)
                        .setTitle("Order")
                        .setMessage("Your covered for this event. Continue?")
                        .setCancelable(false)
                        .setPositiveButton("Yes") { dialog, _ ->
                            saveInfo()
                            for (i in 0 until creditsIds.size) {
                                val creditId =
                                    credits.indexOfFirst { it.documentId == creditsIds[i] }
                                val newCreditId =
                                    newCredits.indexOfFirst { it.documentId == creditsIds[i] }
                                val creditAmount =
                                    credits[creditId].credits.toDouble() - newCredits[newCreditId].credits.toDouble()

                                val data: Map<String, Any> = hashMapOf(
                                    "creditsApplied" to "Yes",
                                    "creditAmount" to creditAmount
                                )
                                db.collection("User").document(user).collection("Credits")
                                    .document(creditsIds[i]).update(data)
                            }
                            dialog.dismiss()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                } else {
                    presentPaymentSheet()
                }
            }  else {
                Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(this)
            } else {
                isOnline1(this)
            }
        ) {
            profilePicGuard()
        } else {
            Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }


    }

    private fun profilePicGuard() {
        db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener { document ->
            if (document != null) {
                val data = document.data

                val profilePic = data?.get("profilePic") as String
                if (profilePic == "yes") {
                    loadCart()
                } else {
                    Toast.makeText(this, "Please upload a profile pic before continuing.", Toast.LENGTH_LONG).show()
                }

            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun loadCart() {

        if (FirebaseAuth.getInstance().currentUser != null) {
            db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("Cart").get().addOnSuccessListener { documents ->
                if (documents != null) {
                    if (documents.size() == 0) {
                        binding.progressBar.isVisible = false
                    }
                    for (doc in documents.documents) {
                        val data = doc.data

                        val chefEmail = data?.get("chefEmail") as String
                        val chefImageId = data["chefImageId"] as String
                        val chefUsername = data["chefUsername"] as String
                        val menuItemId = data["menuItemId"] as String
                        val itemTitle = data["itemTitle"] as String
                        val itemDescription = data["itemDescription"] as String
                        val datesOfEvent = data["datesOfEvent"] as ArrayList<String>
                        val timesForDatesOfEvent = data["timesForDatesOfEvent"] as ArrayList<String>
                        val travelExpenseOption = data["travelExpenseOption"] as String
                        val totalCostOfEvent = data["totalCostOfEvent"] as Number
                        val priceToChef = data["priceToChef"] as Number
                        val quantityOfEvent = data["quantityOfEvent"] as String
                        val unitPrice = data["unitPrice"] as String
                        val distance = data["distance"] as String
                        val location = data["location"] as String
                        val latitudeOfEvent = data["latitudeOfEvent"] as String
                        val longitudeOfEvent = data["longitudeOfEvent"] as String
                        val notesToChef = data["notesToChef"] as String
                        val typeOfService = data["typeOfService"] as String
                        val typeOfEvent = data["typeOfEvent"] as String
                        val city = data["city"] as String
                        val state = data["state"] as String
                        val user = data["user"] as String
                        val imageCount = data["imageCount"] as Number
                        val liked = data["liked"] as ArrayList<String>
                        val itemOrders = data["itemOrders"] as Number
                        val itemRating = data["itemRating"] as ArrayList<Number>
                        val itemCalories = data["itemCalories"] as String
                        val allergies = data["allergies"] as String
                        val additionalMenuItems = data["additionalMenuItems"] as String
                        val documentId = doc.id
                        val signatureDishId = data["signatureDishId"] as String
                        val userNotification = data["userNotificationToken"] as String
                        val chefNotification = data["chefNotificationToken"] as String

                        if (chefUsername == "chefTest") {
                            cant = "cant"
                        }

                            val newItem = CheckoutItems(
                                chefEmail,
                                chefImageId,
                                chefUsername,
                                Uri.EMPTY,
                                menuItemId,
                                itemTitle,
                                itemDescription,
                                datesOfEvent,
                                timesForDatesOfEvent,
                                travelExpenseOption,
                                totalCostOfEvent,
                                priceToChef,
                                quantityOfEvent,
                                unitPrice,
                                distance,
                                location,
                                latitudeOfEvent,
                                longitudeOfEvent,
                                notesToChef,
                                typeOfService,
                                typeOfEvent,
                                city,
                                state,
                                user,
                                documentId,
                                imageCount,
                                liked,
                                itemOrders,
                                itemRating,
                                itemCalories.toInt(),
                                allergies,
                                additionalMenuItems,
                                signatureDishId,
                                userNotification,
                                chefNotification
                            )

                            if (checkoutItems.size == 0) {
                                foodTotal += totalCostOfEvent.toDouble()
                                val cost = "%.2f".format(foodTotal)
                                val taxes = foodTotal * 0.125
                                val tax = "%.2f".format(taxes)
                                val total = "%.2f".format(foodTotal + taxes)
                                binding.foodTotalText.text = "$$cost"
                                binding.taxesAndFeesText.text = "$$tax"
                                binding.totalPrice.text = "$$total"
                                fetchPaymentIntent(total.toDouble())
                                checkoutItems.add(newItem)
                                checkoutAdapter.submitList(checkoutItems)
                                checkoutAdapter.notifyItemInserted(0)
                            } else {
                                val index = checkoutItems.indexOfFirst { it.documentId == doc.id }
                                if (index == -1) {
                                    foodTotal += totalCostOfEvent.toDouble()
                                    val cost = "%.2f".format(foodTotal)
                                    val taxes = "%.2f".format(foodTotal * 0.125)
                                    val total = "%.2f".format(foodTotal + taxes.toDouble())
                                    val tax = "%.2f".format(foodTotal * 0.125)
                                    binding.foodTotalText.text = "$$cost"
                                    binding.taxesAndFeesText.text = "$${tax}"
                                    binding.totalPrice.text = "$$total"
                                    fetchPaymentIntent(total.toDouble())
                                    checkoutItems.add(newItem)
                                    checkoutAdapter.submitList(checkoutItems)
                                    checkoutAdapter.notifyItemInserted(checkoutItems.size - 1)
                                }
                            }


                    }
                }
            }
        } else {
            Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG)
                .show()
        }

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

    private fun fetchPaymentIntent(costOfEvent: Double) {

        val cost = "%.0f".format(costOfEvent * 100)

//        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = FormBody.Builder()
            .add("amount", cost)
            .build()

        val request = Request.Builder()
            .url(backEndUrl)
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
                        val responseJson =
                            JSONObject(responseData)

                        mHandler.post {

                            binding.payButton.isEnabled = true
                            binding.payButton.setTextColor(ContextCompat.getColor(this@Checkout, R.color.main))
                            publishableKey = responseJson.getString("publishableKey")
                            paymentIntentClientSecret = responseJson.getString("paymentIntent")
                            paymentId = responseJson.getString("paymentId")
                            customerConfig = PaymentSheet.CustomerConfiguration(
                                responseJson.getString("customer"),
                                responseJson.getString("ephemeralKey")
                            )

                            binding.progressBar.isVisible = false

                            // Set up PaymentConfiguration with your Stripe publishable key
                            PaymentConfiguration.init(applicationContext, publishableKey)
                        }
                    }
                }
            })
    }

    private fun presentPaymentSheet() {
        paymentSheet.presentWithPaymentIntent(
            paymentIntentClientSecret,
            PaymentSheet.Configuration(
                merchantDisplayName = "Taïste, Inc",
                customer = customerConfig,
                allowsDelayedPaymentMethods = true
            )
        )
    }


    @SuppressLint("SetTextI18n")
    override fun updateCost(costOfEvent: Double, documentId: String) {
        this.listener = "yes"
        foodTotal -= costOfEvent
        val num = "%.2f".format(foodTotal)
        val taxes = "%.2f".format(foodTotal * 0.125)
        val total = "%.2f".format(foodTotal + (foodTotal * 0.125))
        binding.foodTotalText.text = "$$num"
        binding.taxesAndFeesText.text = "$$taxes"
        binding.totalPrice.text = "$$total"

        db.collection("User").document(user).collection("Cart").document(documentId).delete()


    }

    private fun subscribeToTopic(userNotification: String, chefNotification: String, orderId: String, itemTitle: String) {

        val body = FormBody.Builder()
            .add("notificationToken1", userNotification)
            .add("notificationToken2", chefNotification)
            .add("topic", orderId)
            .build()

        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/subscribe-to-topic")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object : Callback {
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
                        val responseJson =
                            JSONObject(responseData)

                        db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("PersonalInfo").get().addOnSuccessListener { documents ->
                            if (documents != null) {
                                for (doc in documents.documents) {
                                    val data = doc.data

                                    val userName = data?.get("userName") as String
                                    sendMessage("New Order", "$userName has just ordered $itemTitle; now awating response.", orderId)
                                }
                            }
                        }

                    }
                }
            })
    }
    private fun sendMessage(title: String, notification: String, topic: String) {

        val body = FormBody.Builder()
            .add("title", title)
            .add("notification", notification)
            .add("topic", topic)
            .build()

        val request = Request.Builder()
            .url("https://taiste-payments.onrender.com/send-message")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object : Callback {
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
                        val responseJson =
                            JSONObject(responseData)



                    }
                }
            })
    }

    @SuppressLint("SimpleDateFormat")
    private fun saveInfo() {

        val sdf = SimpleDateFormat("MM-dd-yyy")
        val date = sdf.format(Date())
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("where_to", "orders")
        for (i in 0 until checkoutItems.size) {
            val item = checkoutItems[i]
            val orderId = UUID.randomUUID().toString()
        val data: Map<String, Any> = hashMapOf("cancelled" to "", "chefEmail" to item.chefEmail, "chefImageId" to item.chefImageId,
            "chefUsername" to item.chefUsername, "city" to item.city,
            "state" to item.state, "distance" to item.distance, "eventDates" to item.datesOfEvent,
            "eventNotes" to item.notesToChef, "eventTimes" to item.timesForDatesOfEvent, "eventType" to item.typeOfEvent,
            "itemDescription" to item.itemDescription, "itemTitle" to item.itemTitle, "latitudeOfEvent" to item.latitudeOfEvent,
            "longitudeOfEvent" to item.longitudeOfEvent, "menuItemId" to item.menuItemId, "numberOfEvents" to checkoutItems.size,
            "orderDate" to date, "orderId" to orderId, "orderUpdate" to "pending", "priceToChef" to item.totalCostOfEvent.toDouble() * 0.95,
            "taxesAndFees" to item.totalCostOfEvent.toDouble() * 0.125, "totalCostOfEvent" to item.totalCostOfEvent, "travelFee" to "", "travelFeeAccepted" to "",
            "travelFeeRequested" to "", "travelFeeApproved" to "", "typeOfService" to item.typeOfService, "userImageId" to FirebaseAuth.getInstance().currentUser!!.uid,
            "user" to user, "unitPrice" to item.unitPrice, "imageCount" to item.imageCount, "liked" to item.liked, "userName" to guserName,
            "itemOrders" to item.itemOrders, "itemRating" to item.itemRating, "itemCalories" to "${item.itemCalories}",
            "location" to item.location, "eventQuantity" to item.quantityOfEvent, "travelExpenseOption" to item.travelExpenseOption,
            "creditsApplied" to creditsApplied, "creditIds" to creditsIds, "allergies" to item.allergies, "additionalMenuItems" to item.additionalMenuItems, "signatureDishId" to item.signatureDishId, "userNotificationToken" to item.userNotification, "chefNotificationToken" to item.chefNotification, "userEmail" to FirebaseAuth.getInstance().currentUser!!.email!!, "payoutDates" to arrayListOf<String>())

            val data2: Map<String, Any> = hashMapOf("cancelled" to "", "chefEmail" to item.chefEmail, "chefImageId" to item.chefImageId, "chefNotificationToken" to "", "chefUsername" to item.chefUsername, "city" to item.city, "state" to item.state, "distance" to item.distance, "eventDates" to item.datesOfEvent, "eventNotes" to item.notesToChef, "eventTimes" to item.timesForDatesOfEvent, "eventType" to item.typeOfEvent, "itemDescription" to item.itemDescription, "itemTitle" to item.itemTitle, "latitudeOfEvent" to item.latitudeOfEvent, "longitudeOfEvent" to item.longitudeOfEvent, "menuItemId" to item.menuItemId, "numberOfEvents" to checkoutItems.size, "orderDate" to date, "orderId" to orderId, "orderUpdate" to "pending", "priceToChef" to item.totalCostOfEvent.toDouble() * 0.95, "taxesAndFees" to item.totalCostOfEvent.toDouble() * 0.125, "totalCostOfEvent" to item.totalCostOfEvent, "travelFee" to "", "travelFeeAccepted" to "", "travelFeeRequested" to "", "travelFeeApproved" to "", "typeOfService" to item.typeOfService, "userImageId" to FirebaseAuth.getInstance().currentUser!!.uid, "userNotificationToken" to "", "user" to user, "unitPrice" to item.unitPrice, "imageCount" to item.imageCount, "liked" to item.liked, "itemOrders" to item.itemOrders, "itemRating" to item.itemRating, "itemCalories" to "${item.itemCalories}", "location" to item.location, "eventQuantity" to item.quantityOfEvent, "travelExpenseOption" to item.travelExpenseOption, "creditsApplied" to creditsApplied, "creditIds" to creditsIds, "paymentIntent" to paymentId, "allergies" to item.allergies, "additionalMenuItems" to item.additionalMenuItems, "signatureDishId" to item.signatureDishId, "userName" to guserName, "userEmail" to FirebaseAuth.getInstance().currentUser!!.email!!, "payoutDates" to arrayListOf<String>())

            var numOfOrders = 0
            if (item.quantityOfEvent == "1-10") {
                numOfOrders = 10
            } else if (item.quantityOfEvent == "11-25") {
                numOfOrders = 25
            } else if (item.quantityOfEvent == "26-40") {
                numOfOrders = 40
            } else if (item.quantityOfEvent == "41-55") {
                numOfOrders = 55
            } else if (item.quantityOfEvent == "56-70") {
                numOfOrders = 70
            } else if (item.quantityOfEvent == "71-90") {
                numOfOrders = 90
            } else {
                numOfOrders = item.quantityOfEvent.toInt()
            }
            val data3: Map<String, Any> = hashMapOf("itemOrders"  to numOfOrders)

            subscribeToTopic(item.userNotification, item.chefNotification, item.documentId, item.itemTitle)
            db.collection("Chef").document(item.chefImageId).collection("Orders").document(orderId).set(data)
            db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").document(orderId).set(data)
            db.collection("Orders").document(orderId).set(data2)
            db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Cart").document(item.documentId).delete()
            db.collection(item.typeOfService).document(item.menuItemId).update(data3)
            val sdfT = SimpleDateFormat("MM-dd-yyyy hh:mm a")
            val date1 = sdfT.format(Date())
            val data5: Map<String, Any> = hashMapOf("notification" to "$guserName has just placed an order (${item.typeOfService}) for ${item.itemTitle}", "date" to date1)
            val data4: Map<String, Any> = hashMapOf("notifications" to "yes")
            db.collection("Chef").document(item.chefImageId).collection("Notifications").document().set(data5)
            db.collection("Chef").document(item.chefImageId).update(data4)
            if (i == checkoutItems.size - 1) {
                Toast.makeText(this, "Order Complete. Please check your 'Order' tab for updates on your order.", Toast.LENGTH_LONG).show()
                startActivity(intent)
            }
        }
    }


    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when(paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                print("Canceled")
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(this, "There was an error processing your payment. Please check your information and try again.", Toast.LENGTH_LONG).show()
            }
            is PaymentSheetResult.Completed -> {
                if (cant != "") {
                    Toast.makeText(this, "You have orders from test profiles in this order. Please cancel this order.", Toast.LENGTH_LONG).show()
                } else {
                    saveInfo()
                }

            }
        }
    }

}
interface ClickListener {
    fun updateCost(costOfEvent: Double, documentId: String)
}
