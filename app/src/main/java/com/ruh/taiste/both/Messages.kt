package com.ruh.taiste.both

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.ruh.taiste.R
import com.ruh.taiste.both.adapters.MessageAdapter
import com.ruh.taiste.both.models.Messages
import com.ruh.taiste.chef.MainActivity
import com.ruh.taiste.chef.chefImageId
import com.ruh.taiste.databinding.ActivityMessagesBinding
import com.ruh.taiste.user.userImageId
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.FirebaseError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.both.models.VideoModel
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import com.ruh.taiste.user.userImage
import com.ruh.taiste.user.userName
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "Messages"
class Messages : AppCompatActivity() {

    private lateinit var binding: ActivityMessagesBinding


    private val backEndUrl = "https://taiste-payments.onrender.com/create-payment-intent"
    private val httpClient = OkHttpClient()

    private val mHandler: Handler = Handler(Looper.getMainLooper())


    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private var messages: MutableList<Messages> = ArrayList()
    private lateinit var messageAdapter: MessageAdapter

    private lateinit var paymentSheet: PaymentSheet
    private lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    private lateinit var paymentIntentClientSecret: String
    private lateinit var publishableKey: String
    private var paymentId = ""



    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var travelFeeText : EditText
    private lateinit var travelFeePayButton: MaterialButton
    private lateinit var exitButton: TextView
    private lateinit var progressBar: ProgressBar

    //messageRequest
    private var messageRequestRecipient = ""
    private var messageRequestReceiverUsername = ""
    private var messageRequestSenderUsername = ""
    private var messageRequestSenderImageId = ""
    private var messageRequestReceiverImageId = ""
    private var messageRequestChefOrUser = ""
    private var messageRequestDocumentId = ""
    private var messageRequestSenderEmail = ""
    private var messageRequestReceiverEmail = ""
    private var messageRequestReceiverChefOrUser = ""

    //Orders
    private var travelFeeOrMessages = ""
    private var travelFee = ""
    private var menuItemId = ""
    private var totalCostOfEvent = ""
    private var itemTitle = ""
    private var eventType = ""
    private var eventQuantity = ""
    private var location = ""

    private var orderMessageSenderImageId = ""
    private var orderMessageReceiverImageId = ""
    private var orderMessageChefOrUser = ""
    private var orderMessageDocumentId = ""
    private var orderMessageReceiverName = ""
    private var orderMessageSenderName = ""
    private var orderMessageReceiverEmail = ""
    private var orderMessageSenderEmail = ""
    private var orderMessageReceiverChefOrUser = ""



    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("MM-dd-yyyy hh:mm:ss a")


    @SuppressLint("SimpleDateFormat")
    private var sdfYearMonth = SimpleDateFormat("yyyy, MM")
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        messageAdapter = MessageAdapter(this, messages)
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.messageRecyclerView.adapter = messageAdapter


        //messageRequests
        messageRequestSenderImageId = intent.getStringExtra("message_request_sender_image_id").toString()
        messageRequestReceiverImageId = intent.getStringExtra("message_request_receiver_image_id").toString()
        messageRequestChefOrUser = intent.getStringExtra("message_request_chef_or_user").toString()
        messageRequestDocumentId = intent.getStringExtra("message_request_document_id").toString()
        messageRequestReceiverUsername = intent.getStringExtra("message_request_receiver_name").toString()
        messageRequestSenderUsername = intent.getStringExtra("message_request_sender_name").toString()
        messageRequestReceiverEmail = intent.getStringExtra("message_request_receiver_email").toString()
        messageRequestSenderEmail = intent.getStringExtra("message_request_sender_email").toString()
        messageRequestReceiverChefOrUser = intent.getStringExtra("message_request_receiver_chef_or_user").toString()

        //Orders
        eventType = intent.getStringExtra("event_type").toString()
        location = intent.getStringExtra("location").toString()
        eventQuantity  = intent.getStringExtra("event_quantity").toString()
        travelFee = intent.getStringExtra("travel_fee").toString()
        travelFeeOrMessages = intent.getStringExtra("travel_fee_or_message").toString()
        menuItemId = intent.getStringExtra("menu_item_id").toString()
        totalCostOfEvent = intent.getStringExtra("total_cost_of_event").toString()
        itemTitle = intent.getStringExtra("item_title").toString()

        travelFeeOrMessages = intent.getStringExtra("travel_fee_or_message").toString()

       orderMessageSenderImageId = intent.getStringExtra("order_message_sender_image_id").toString()
       orderMessageReceiverImageId = intent.getStringExtra("order_message_receiver_image_id").toString()
       orderMessageChefOrUser = intent.getStringExtra("order_message_chef_or_user").toString()
       orderMessageDocumentId = intent.getStringExtra("order_message_document_id").toString()
       orderMessageReceiverName = intent.getStringExtra("order_message_receiver_name").toString()
       orderMessageSenderName = intent.getStringExtra("order_message_sender_name").toString()
       orderMessageReceiverEmail = intent.getStringExtra("order_message_receiver_email").toString()
       orderMessageSenderEmail = intent.getStringExtra("order_message_sender_email").toString()
       orderMessageReceiverChefOrUser = intent.getStringExtra("order_message_receiver_chef_or_user").toString()

        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(this)
            } else {
                isOnline1(this)
            }
        ) {
            loadMessages()
        } else {
            Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }


        bottomSheetDialog = BottomSheetDialog(this@Messages, R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(this).inflate(R.layout.travel_fee_bottom_sheet, findViewById<RelativeLayout>(R.id.travel_fee_bottom_sheet)
        )

        travelFeeText = bottomSheetView.findViewById(R.id.travel_fee)
        travelFeePayButton = bottomSheetView.findViewById(R.id.pay_button)
        exitButton = bottomSheetView.findViewById(R.id.exit_button)
        progressBar = bottomSheetView.findViewById(R.id.progress_bar)

        bottomSheetDialog.setContentView(bottomSheetView)

        if (travelFeeOrMessages != "MessageRequests") {
            binding.user.text = orderMessageReceiverName
        } else {
            binding.user.text = messageRequestReceiverUsername
        }

        if (eventType == "na") {
            binding.user.text = "@$orderMessageReceiverName"
            binding.eventTypeAndQuantity.visibility = View.GONE
            binding.location.visibility = View.GONE
        } else {
            binding.user.text = "@$orderMessageReceiverName"
            binding.eventTypeAndQuantity.text =
                "Event Type: $eventType    Event Quantity: $eventQuantity"
            binding.location.text = "Location:  $location"
        }

        if (travelFeeOrMessages == "TravelFeeMessages") {
            if (orderMessageChefOrUser == "Chef") {
                travelFeePayButton.text = "Request"
                binding.payTravelFeeButton.text = "Add Travel Fee"
            } else {
                binding.payTravelFeeButton.text = "Pay Travel Fee"
                travelFeePayButton.text = "Pay"
            }

            if (orderMessageChefOrUser == "User" && travelFee == "") {
                binding.messageLabel.text = "Travel Fee"
            } else {
                binding.messageLabel.visibility = View.GONE
                binding.payTravelFeeButton.visibility = View.VISIBLE
            }

            if (travelFee != "") {
                binding.travelFee.visibility = View.VISIBLE
                if (travelFee == "null") {
                    binding.travelFee.isVisible = false
                } else {
                    binding.travelFee.text = "Fee: $$travelFee"
                }
            }
        } else if (travelFeeOrMessages == "MessageRequests") {
            binding.user.text = messageRequestReceiverUsername
            binding.eventTypeAndQuantity.visibility = View.GONE
            binding.location.visibility = View.GONE

        } else {
            binding.user.text = orderMessageReceiverName
        }



        binding.payTravelFeeButton.setOnClickListener {
            if (orderMessageChefOrUser == "User") {
                travelFeePayButton.isEnabled = false
                travelFeePayButton.setTextColor(ContextCompat.getColor(this, R.color.gray))
                travelFeeText.setText(travelFee)
                progressBar.isVisible = true
                fetchPaymentIntent(travelFee.toDouble())
            }
            bottomSheetDialog.show()
        }

        travelFeePayButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {


            if (orderMessageChefOrUser == "Chef") {

            val data : Map<String, Any> = hashMapOf("chefOrUser" to "system", "userImageId" to FirebaseAuth.getInstance().currentUser!!.uid, "date" to sdf.format(Date()), "message" to "@$guserName has requested $${travelFeeText.text} for travel.", "userEmail" to orderMessageSenderEmail, "travelFee" to travelFeeText.text.toString())
            val data1: Map<String, Any> = hashMapOf("notification_type" to travelFeeOrMessages, "destination" to orderMessageDocumentId, "date" to sdf.format(Date()), "notification" to "A travel fee of $${travelFeeText.text} has been requested.")
            val data2: Map<String, Any> = hashMapOf("travelFee" to travelFeeText.text.toString())
                sendMessage("TravelFeeMessage", "@${FirebaseAuth.getInstance().currentUser!!.displayName!!} has requested $$travelFeeText for travel.", orderMessageDocumentId)


                val documentId = UUID.randomUUID().toString()
            db.collection(FirebaseAuth.getInstance().currentUser!!.displayName!!).document(FirebaseAuth.getInstance().currentUser!!.uid).collection(travelFeeOrMessages).document(orderMessageDocumentId).collection(orderMessageDocumentId).document(documentId).set(data)
            db.collection(orderMessageReceiverChefOrUser).document(orderMessageReceiverImageId).collection(travelFeeOrMessages).document(orderMessageDocumentId).collection(orderMessageDocumentId).document(documentId).set(data)
            db.collection(orderMessageReceiverChefOrUser).document(orderMessageReceiverImageId).collection("Notifications").document(documentId).set(data1)
            db.collection(FirebaseAuth.getInstance().currentUser!!.displayName!!).document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").document(orderMessageDocumentId).update(data2)
            db.collection(orderMessageReceiverChefOrUser).document(orderMessageReceiverImageId).collection("Orders").document(orderMessageDocumentId).update(data2)
            db.collection("Orders").document(orderMessageDocumentId).update(data2)

                Toast.makeText(this, "Travel Fee sent.", Toast.LENGTH_LONG).show()
                bottomSheetDialog.dismiss()
            } else {
                presentPaymentSheet()
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        exitButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }




        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.sendMessageButton.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(this)
                } else {
                    isOnline1(this)
                }
            ) {

            if (binding.messageText.text.isNotEmpty()) {

                val data: Map<String, Any> = hashMapOf(
                    "chefOrUser" to FirebaseAuth.getInstance().currentUser!!.displayName!!,
                    "userImageId" to FirebaseAuth.getInstance().currentUser!!.uid,
                    "date" to sdf.format(Date()),
                    "message" to binding.messageText.text.toString(),
                    "userEmail" to FirebaseAuth.getInstance().currentUser!!.email!!,
                    "travelFee" to ""
                )


                if (travelFeeOrMessages == "MessageRequests") {
                    if (messages.count() == 0) {
                        binding.messageText.isEnabled = false
                    }
                        val b = if (FirebaseAuth.getInstance().currentUser!!.displayName!! == "Chef") { "User" } else { "Chef" }
                        db.collection(FirebaseAuth.getInstance().currentUser!!.displayName!!).document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener { document ->

                            val data10 = document.data

                            val notification1 = data10?.get("notificationToken") as String
                            db.collection(messageRequestReceiverChefOrUser).document(messageRequestReceiverImageId).get().addOnSuccessListener { document1 ->
                               val data11 = document1.data
                                val notification2 = data11?.get("notificationToken") as String
                                subscribeToTopic(notification1, notification2, userImageId)
                            }

                        }


                    val sdfT = SimpleDateFormat("MM-dd-yyyy hh:mm a")
                    val date1 = sdfT.format(Date())
                    val data5 : Map<String, Any> = hashMapOf("chefOrUser" to FirebaseAuth.getInstance().currentUser!!.displayName!!, "userImageId" to FirebaseAuth.getInstance().currentUser!!.uid, "userEmail" to FirebaseAuth.getInstance().currentUser!!.email!!, "date" to date1, "userName" to messageRequestSenderUsername)
                    val documentId = UUID.randomUUID().toString()
                    if (messages.count() == 0) {
                    db.collection(FirebaseAuth.getInstance().currentUser!!.displayName!!).document(FirebaseAuth.getInstance().currentUser!!.uid).collection("MessageRequests").document(messageRequestDocumentId).set(data5)
                    db.collection(messageRequestReceiverChefOrUser).document(messageRequestReceiverImageId).collection("MessageRequests").document(messageRequestDocumentId).set(data5)
                    }
                    db.collection(FirebaseAuth.getInstance().currentUser!!.displayName!!).document(FirebaseAuth.getInstance().currentUser!!.uid).collection("MessageRequests").document(messageRequestDocumentId).collection(messageRequestDocumentId).document(documentId).set(data)
                    db.collection(messageRequestReceiverChefOrUser).document(messageRequestReceiverImageId).collection("MessageRequests").document(messageRequestDocumentId).collection(messageRequestDocumentId).document(documentId).set(data)

                    val data3: Map<String, Any> = hashMapOf("notification" to "@$guserName has just sent you a message request.", "date" to date1)
                    val data4: Map<String, Any> = hashMapOf("notifications" to "yes")
                    db.collection(messageRequestReceiverChefOrUser).document(messageRequestReceiverImageId).collection("Notifications").document().set(data3)
                    db.collection(messageRequestReceiverChefOrUser).document(messageRequestReceiverImageId).update(data4)

                    binding.messageText.setText("")
                    Toast.makeText(this, "Message Sent.", Toast.LENGTH_LONG).show()


                } else {
                    val documentId = UUID.randomUUID().toString()
                    db.collection(FirebaseAuth.getInstance().currentUser!!.displayName!!).document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .collection(travelFeeOrMessages).document(orderMessageDocumentId).collection(orderMessageDocumentId)
                        .document(documentId).set(data)
                    db.collection(orderMessageReceiverChefOrUser).document(orderMessageReceiverImageId).collection(travelFeeOrMessages)
                        .document(orderMessageDocumentId).collection(orderMessageDocumentId).document(documentId).set(data)

                    val sdfT = SimpleDateFormat("MM-dd-yyyy hh:mm a")
                    val date1 = sdfT.format(Date())
                    val data3: Map<String, Any> = hashMapOf("notification" to "@$guserName has just messaged you ($eventType) about $itemTitle.", "date" to date1)
                    val data4: Map<String, Any> = hashMapOf("notifications" to "yes")
                    db.collection(orderMessageReceiverChefOrUser).document(orderMessageReceiverImageId).collection("Notifications").document().set(data3)
                    db.collection(orderMessageReceiverChefOrUser).document(orderMessageReceiverImageId).update(data4)
                    db.collection(FirebaseAuth.getInstance().currentUser!!.displayName!!).document(FirebaseAuth.getInstance().currentUser!!.uid).collection(travelFeeOrMessages).document(orderMessageDocumentId).collection(orderMessageDocumentId).document(documentId).set(data)
                    db.collection(FirebaseAuth.getInstance().currentUser!!.displayName!!).document(FirebaseAuth.getInstance().currentUser!!.uid).collection(travelFeeOrMessages).document(orderMessageDocumentId).set(data)
                    db.collection(orderMessageReceiverChefOrUser).document(orderMessageReceiverImageId).collection(travelFeeOrMessages).document(orderMessageDocumentId).set(data)
                    db.collection(orderMessageReceiverChefOrUser).document(orderMessageReceiverImageId).collection(travelFeeOrMessages).document(orderMessageDocumentId).collection(orderMessageDocumentId).document(documentId).set(data)

                    sendMessage("New Message", "@$guserName has just messaged you ($eventType) about $itemTitle.", orderMessageDocumentId)
                }

                binding.messageText.setText("")
                Toast.makeText(this, "Message Sent", Toast.LENGTH_LONG).show()
            } else {
                if (!binding.messageText.isEnabled) {
                    Toast.makeText(this, "We limit the number of initial messages to one until the user replies.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Please add a message.", Toast.LENGTH_LONG).show()
                }
            }
            } else {
                Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }


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

    private fun loadUsername() {
        db.collection("Usernames").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val userName = data?.get("userName") as String
                    guserName = userName

                }
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun loadMessages() {
        if (travelFeeOrMessages == "MessageRequests") {
            binding.messageText.isEnabled = false
            Log.d(TAG, "loadMessages: $userImageId")
            db.collection(FirebaseAuth.getInstance().currentUser!!.displayName!!).document(FirebaseAuth.getInstance().currentUser!!.uid).collection("MessageRequests").document(messageRequestDocumentId).collection(
                messageRequestDocumentId).addSnapshotListener { documents, _ ->
                if (documents != null) {
                    if (documents.size() == 0) {
                        binding.messageText.isEnabled = true
                    }
                    for (doc in documents.documents) {
                        val data = doc.data

                        val chefOrUserI = data?.get("chefOrUser") as String
                        val user = data["user"] as String
                        val message = data["message"] as String
                        val date = data["date"] as String
                        val userEmail = data["userEmail"] as String

                        if (messageRequestRecipient == "") {
                            messageRequestRecipient = user
                        } else if (messageRequestRecipient != "good") {
                            if (messageRequestRecipient != user) {
                                messageRequestRecipient = "good"
                                binding.messageText.isEnabled = true
                            }
                        }
                        var vari = ""
                        if (chefOrUserI == "Chef") {
                            vari = "chefs"
                        } else {
                            vari = "users"
                        }

                        var homeOrAway = ""
                        if (userEmail == FirebaseAuth.getInstance().currentUser!!.email!!) {
                            homeOrAway = "home"
                        } else  {
                            homeOrAway = "away"
                        }

                        storage.reference.child("$vari/$userEmail/profileImage/$user.png").downloadUrl.addOnSuccessListener { uri ->
                            val message = Messages(
                                chefOrUserI,
                                homeOrAway,
                                user,
                                uri,
                                date,
                                message,
                                userEmail,
                                doc.id,
                                ""
                            )
                            if (messages.size == 0) {
                                messages.add(message)
                                messageAdapter.submitList(messages)
                                messageAdapter.notifyItemInserted(0)
                                if (userEmail != FirebaseAuth.getInstance().currentUser!!.email!!) {
                                    binding.messageText.isEnabled = true
                                }
                            } else {
                                val index =
                                    messages.indexOfFirst { it.documentId == doc.id }
                                if (index == -1) {
                                    messages.add(message)
                                    messages.sortBy { it.date }
                                    messageAdapter.submitList(messages)
                                    messageAdapter.notifyDataSetChanged()
                                    binding.messageText.isEnabled = true
                                }
                            }
                        }

                    }
                }
            }
        } else {
            db.collection(FirebaseAuth.getInstance().currentUser!!.displayName!!).document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection(travelFeeOrMessages).document(orderMessageDocumentId).collection(orderMessageDocumentId)
                .addSnapshotListener { documents, _ ->
                    if (documents != null) {
                        for (doc in documents.documents) {
                            val data = doc.data

                            val chefOrUser = data?.get("chefOrUser") as String
                            val user = data["userImageId"] as String
                            val date = data["date"] as String
                            val message = data["message"] as String
                            val userEmail = data["userEmail"] as String

                            var travelFee = if (data["travelFee"] != null) { data["travelFee"] as String } else { "" }

                            binding.travelFee.text = "$$travelFee"
                            this.travelFee = travelFee
                            binding.travelFee.isVisible = true
                            val chefOrUserStorage = if (chefOrUser == "Chef") {
                                "chefs"
                            } else {
                                "users"
                            }

                            val homeOrAway = when {
                                chefOrUser == FirebaseAuth.getInstance().currentUser!!.displayName!! -> {
                                    "home"
                                }
                                chefOrUser != "system" -> {
                                    "away"
                                }
                                else -> {
                                    "system"
                                }
                            }

                            Log.d(TAG, "loadMessages: $chefOrUserStorage")
                            Log.d(TAG, "loadMessages: $homeOrAway")


                            storage.reference.child("$chefOrUserStorage/$userEmail/profileImage/$user.png").downloadUrl

                                .addOnSuccessListener { userUri ->

                                    val newMessage = Messages(
                                        chefOrUser,
                                        homeOrAway,
                                        user,
                                        userUri,
                                        date,
                                        message,
                                        userEmail,
                                        doc.id,
                                        travelFee
                                    )

                                    if (messages.size == 0) {
                                        messages.add(newMessage)
                                        messages.sortBy { it.date }
                                        messageAdapter.submitList(messages)

                                        messageAdapter.notifyDataSetChanged()
                                    } else {
                                        val index =
                                            messages.indexOfFirst { it.documentId == doc.id }
                                        if (index == -1) {
                                            messages.add(newMessage)
                                            messages.sortBy { it.date }
                                            messageAdapter.submitList(messages)
                                            messageAdapter.notifyDataSetChanged()
                                        }
                                    }
                                }

                                .addOnFailureListener {
                                    val newMessage = Messages(
                                        chefOrUser,
                                        homeOrAway,
                                        user,
                                        Uri.EMPTY,
                                        date,
                                        message,
                                        userEmail,
                                        doc.id,
                                        travelFee
                                    )

                                    if (messages.size == 0) {
                                        messages.add(newMessage)
                                        messages.sortBy { it.date }
                                        messageAdapter.submitList(messages)
                                        messageAdapter.notifyDataSetChanged()
                                    } else {
                                        val index =
                                            messages.indexOfFirst { it.documentId == doc.id }
                                        if (index == -1) {
                                            messages.add(newMessage)
                                            messages.sortBy { it.date }
                                            messageAdapter.submitList(messages)
                                            messageAdapter.notifyDataSetChanged()
                                        }
                                    }
                                }
                        }
                    }
                }
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

    private fun subscribeToTopic(userNotification: String, chefNotification: String, topic: String) {

        val body = FormBody.Builder()
            .add("notificationToken1", userNotification)
            .add("notificationToken2", chefNotification)
            .add("topic", topic)
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

                        sendMessage("Message Request", "@$guserName has just sent you a message request.", userImageId)

                    }
                }
            })
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

                            travelFeePayButton.isEnabled = true
                            travelFeePayButton.setTextColor(ContextCompat.getColor(this@Messages, R.color.main))
                            publishableKey = responseJson.getString("publishableKey")
                            paymentId = responseJson.getString("paymentId")
                            paymentIntentClientSecret = responseJson.getString("paymentIntent")
                            customerConfig = PaymentSheet.CustomerConfiguration(
                                responseJson.getString("customer"),
                                responseJson.getString("ephemeralKey")
                            )

                            progressBar.isVisible = false

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

    private fun saveInfo() {
        val intent = Intent(this, MainActivity::class.java)

        val week = if (Calendar.getInstance()[Calendar.WEEK_OF_MONTH] > 4) {
            4 } else { Calendar.getInstance()[Calendar.WEEK_OF_MONTH] }

        val yearMonth = sdfYearMonth.format(Date())
        val weekOfMonth = "Week $week"
        val day = sdf.format(Date())
        intent.putExtra("where_to", "orders")

        val data : Map<String, Any> = hashMapOf("chefOrUser" to "system", "user" to FirebaseAuth.getInstance().currentUser!!.uid, "date" to sdf.format(Date()), "message" to "paidTravelFee${travelFeeText.text}", "userEmail" to FirebaseAuth.getInstance().currentUser!!.email!!)
        val data1: Map<String, Any> = hashMapOf("notification_type" to travelFeeOrMessages, "destination" to orderMessageDocumentId, "date" to sdf.format(Date()), "notification" to "A travel fee of $${travelFeeText.text} has been accepted.")
        val data2: Map<String, Any> = hashMapOf("travelFee" to binding.travelFee.text.toString(), "orderUpdate" to "scheduled")
        val data3: Map<String, Any> = hashMapOf("paymentId" to paymentId, "userImageId" to FirebaseAuth.getInstance().currentUser!!.uid, "chefEmail" to orderMessageReceiverEmail, "menuItemId" to menuItemId, "date" to sdf.format(Date()), "chefImageId" to orderMessageReceiverImageId)


        db.collection(FirebaseAuth.getInstance().currentUser!!.displayName!!).document(FirebaseAuth.getInstance().currentUser!!.uid).collection("TravelFeeMessages").document(orderMessageDocumentId).collection(orderMessageDocumentId).document("payment").set(data)
        db.collection(orderMessageReceiverChefOrUser).document(orderMessageReceiverImageId).collection(travelFeeOrMessages).document(orderMessageDocumentId).collection("TravelFeeMessages").document(orderMessageDocumentId).collection(orderMessageDocumentId).document("payment").set(data)

        val documentId = UUID.randomUUID().toString()
        db.collection("TravelFeePayments").document(orderMessageDocumentId).set(data3)
        db.collection(FirebaseAuth.getInstance().currentUser!!.displayName!!).document(FirebaseAuth.getInstance().currentUser!!.uid).collection("TravelFeePayments").document(orderMessageDocumentId).collection(orderMessageDocumentId).document("payment").set(data)
        db.collection(orderMessageReceiverChefOrUser).document(orderMessageReceiverImageId).collection(travelFeeOrMessages).document(orderMessageDocumentId).collection("TravelFeePayments").document(orderMessageDocumentId).collection(orderMessageDocumentId).document("payment").set(data)

        db.collection(orderMessageReceiverChefOrUser).document(orderMessageReceiverImageId).collection("Notifications").document(documentId).set(data1)
        db.collection(FirebaseAuth.getInstance().currentUser!!.displayName!!).document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").document(orderMessageDocumentId).update(data2)
        db.collection(orderMessageReceiverChefOrUser).document(orderMessageReceiverImageId).collection("Orders").document(orderMessageDocumentId).update(data2)
        db.collection("Orders").document(orderMessageDocumentId).update(data2)
        sendMessage("Travel Fee Received.", "The travel fee has been paid.", orderMessageDocumentId)
        val data4: Map<String, Any> = hashMapOf("notification" to "Travel Fee has been paid for $itemTitle.", "date" to sdf.format(Date()))
        val data5: Map<String, Any> = hashMapOf("notifications" to "yes")
        if (orderMessageReceiverChefOrUser == "Chef") {
            db.collection("Chef").document(orderMessageReceiverImageId).collection("Notifications").document().set(data4)
            db.collection("Chef").document(orderMessageReceiverImageId).update(data5)
        } else {
            db.collection("Chef").document(orderMessageSenderImageId).collection("Notifications").document().set(data4)
            db.collection("Chef").document(orderMessageReceiverImageId).update(data5)
        }


        val docRef = db.collection("Chef").document(orderMessageReceiverImageId).collection("Dashboard").document(eventType)
        val docRef1 = db.collection("Chef").document(orderMessageReceiverImageId).collection("Dashboard").document(eventType).collection(menuItemId).document("Total")
        val docRef2 = db.collection("Chef").document(orderMessageReceiverImageId).collection("Dashboard").document(eventType).collection(menuItemId).document("Month").collection(yearMonth).document("Total")
        val docRef3 = db.collection("Chef").document(orderMessageReceiverImageId).collection("Dashboard").document(eventType).collection(menuItemId).document("Month").collection(yearMonth).document("Week").collection(weekOfMonth).document("Total")
        val docRef4 = db.collection("Chef").document(orderMessageReceiverImageId).collection("Dashboard").document(eventType).collection(menuItemId).document("Month").collection(yearMonth).document("Week").collection(weekOfMonth).document(orderMessageDocumentId)

        docRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val data4 = document.data

                val totalPay = data4?.get("totalPay") as Number
                val info : Map<String, Any> = hashMapOf("totalPay" to totalPay.toDouble() + totalCostOfEvent.toDouble())
                docRef.update(info)
            } else {
                val info : Map<String, Any> = hashMapOf("totalPay" to totalCostOfEvent.toDouble())
                docRef.set(info)
            }
        }

        docRef1.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val data4 = document.data

                val totalPay = data4?.get("totalPay") as Number
                val info : Map<String, Any> = hashMapOf("totalPay" to totalPay.toDouble() + totalCostOfEvent.toDouble())
                docRef1.update(info)
            } else {
                val info : Map<String, Any> = hashMapOf("totalPay" to totalCostOfEvent.toDouble())
                docRef1.set(info)
            }
        }

        docRef2.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val data4 = document.data

                val totalPay = data4?.get("totalPay") as Number
                val info : Map<String, Any> = hashMapOf("totalPay" to totalPay.toDouble() + totalCostOfEvent.toDouble())
                docRef2.update(info)
            } else {
                val info : Map<String, Any> = hashMapOf("totalPay" to totalCostOfEvent.toDouble())
                docRef2.set(info)
            }
        }

        docRef3.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val data4 = document.data

                val totalPay = data4?.get("totalPay") as Number
                val info : Map<String, Any> = hashMapOf("totalPay" to totalPay.toDouble() + totalCostOfEvent.toDouble())
                docRef3.update(info)
            } else {
                val info : Map<String, Any> = hashMapOf("totalPay" to totalCostOfEvent.toDouble())
                docRef3.set(info)
            }
        }

        docRef4.set(data1)

        Toast.makeText(this, "Travel Fee Paid.", Toast.LENGTH_LONG).show()

        bottomSheetDialog.dismiss()
        startActivity(intent)

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
                saveInfo()

            }
        }
    }
}