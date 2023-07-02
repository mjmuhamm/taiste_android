package com.ruh.taiste.chef.fragments

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ruh.taiste.R
import com.ruh.taiste.both.models.CompleteOrders
import com.ruh.taiste.chef.*
import com.ruh.taiste.databinding.FragmentOrdersChefBinding
import com.ruh.taiste.chef.adapters.orders.PendingAdapter
import com.ruh.taiste.chef.adapters.orders.ScheduledAdapter
import com.ruh.taiste.both.models.PendingOrders
import com.ruh.taiste.both.models.ScheduledOrders
import com.ruh.taiste.chef.adapters.orders.CompleteAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.both.Notifications
import com.ruh.taiste.both.guserName
import com.ruh.taiste.chef.models.Cancelled
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Orders.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "Orders"
class Orders : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding : FragmentOrdersChefBinding? = null
    private val binding get() = _binding!!

    private val backEndUrl = "https://taiste-payments.onrender.com/create-payment-intent"
    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())


    var owe = 0.0
    var cancelled : MutableList<Cancelled> = arrayListOf()


    private lateinit var pendingAdapter: PendingAdapter
    private lateinit var scheduledAdapter: ScheduledAdapter
    private lateinit var completeAdapter: CompleteAdapter

    private var pendingOrders: MutableList<PendingOrders> = ArrayList()
    private var scheduledOrders: MutableList<ScheduledOrders> = ArrayList()
    private var completeOrders: MutableList<CompleteOrders> = ArrayList()

    private val db = Firebase.firestore

    private var toggle = "Pend"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersChefBinding.inflate(inflater, container, false)

        binding.pendingRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.scheduledRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.completeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        pendingAdapter = PendingAdapter(requireContext(), pendingOrders)
        scheduledAdapter = ScheduledAdapter(requireContext(), scheduledOrders)
        completeAdapter = CompleteAdapter(requireContext(), completeOrders)


        binding.pendingRecyclerView.adapter = pendingAdapter
        binding.scheduledRecyclerView.adapter = scheduledAdapter
        binding.completeRecyclerView.adapter = completeAdapter
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(requireContext())
            } else {
                isOnline1(requireContext())
            }
        ) {
            payout()
            loadOrders("Pend")
        } else {
            Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }

        binding.disclaimer.text = "*Orders appear hear until you have accepted the event, and will transfer to the 'Schedule' tab after. If you cancel here, a full refund will be dispersed to the user immediately with no charge to you.*"
        binding.pending.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

            binding.disclaimer.text = "*Orders appear hear until you have accepted the event, and will transfer to the 'Schedule' tab after. If you cancel here, a full refund will be dispersed to the user immediately with no charge to you.*"
            toggle = "Pending"
            loadOrders("Pending")
            binding.pending.isSelected = true
            binding.scheduled.isSelected = false
            binding.complete.isSelected = false
            binding.noItemsText.isVisible = false

            binding.pendingRecyclerView.isVisible = true
            binding.scheduledRecyclerView.isVisible = false
            binding.completeRecyclerView.isVisible = false

            binding.pendingRecyclerView.scrollToPosition(0)

            binding.pending.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.pending.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.scheduled.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.scheduled.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.complete.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.complete.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.scheduled.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

            binding.disclaimer.text = "*Orders appear hear after you have accepted. If you cancel within 7 days of the event, you will be charged 15% of the total order cost on your next payout. Otherwise, you can expect 5% charge of the total order cost to be charged on your next payout.*"
            toggle = "Scheduled"
            loadOrders("Scheduled")
            binding.pending.isSelected = false
            binding.scheduled.isSelected = true
            binding.complete.isSelected = false
            binding.noItemsText.isVisible = false

            binding.pendingRecyclerView.isVisible = false
            binding.scheduledRecyclerView.isVisible = true
            binding.completeRecyclerView.isVisible = false

            binding.scheduledRecyclerView.scrollToPosition(0)
            binding.pending.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.pending.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.scheduled.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.scheduled.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.complete.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.complete.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.complete.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

            binding.disclaimer.text = "*Orders appear hear after completion. Please consider viewing the user's profile to optimize your menu options for your consumer base.*"
            toggle = "Complete"
            loadOrders("Complete")

            binding.pending.isSelected = false
            binding.scheduled.isSelected = false
            binding.complete.isSelected = true
            binding.noItemsText.isVisible = false

            binding.pendingRecyclerView.isVisible = false
            binding.scheduledRecyclerView.isVisible = false
            binding.completeRecyclerView.isVisible = true

            binding.completeRecyclerView.scrollToPosition(0)

            binding.pending.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.pending.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.scheduled.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.scheduled.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.complete.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.complete.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        }

        binding.notificationsButton.setOnClickListener {
            binding.notificationsImage.visibility = View.GONE
            val intent = Intent(requireContext(), Notifications::class.java)
            intent.putExtra("chef_or_user", "Chef")
            startActivity(intent)
        }

        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(requireContext())
            } else {
                isOnline1(requireContext())
            }
        ) {
            loadUsername()
            loadNotifications()
        } else {
            binding.progressBar.isVisible = false
            Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }

        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Orders().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun loadUsername() {
        if (guserName == "") {
            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("PersonalInfo").get().addOnSuccessListener { documents ->
                if (documents != null) {
                    for (doc in documents.documents) {
                        val data = doc.data

                        val chefName = data?.get("chefName") as String
                        guserName = chefName
                    }
                }
            }
        }
    }
    private fun loadNotifications() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).addSnapshotListener { document, _ ->
                if (document != null) {
                    val data = document.data
                    val notifications = data?.get("notifications") as String
                    if (notifications == "yes") {
                        binding.notificationsImage.visibility = View.VISIBLE
                    } else {
                        binding.notificationsImage.visibility = View.GONE
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "Something went wrong. Please check connection.", Toast.LENGTH_LONG).show()
        }
    }

    private fun payout() {
        @SuppressLint("SimpleDateFormat")
        var sdfT = SimpleDateFormat("MM-dd-yyyy hh:mm a")

        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").addSnapshotListener { documents, error ->
            if (error == null) {
                if (documents != null) {
                    for (doc in documents.documents) {

                        val data = doc.data

                        val eventDates = data?.get("eventDates") as ArrayList<String>
                        val eventTimes = data["eventTimes"] as ArrayList<String>
                        val chefImageId = data["chefImageId"] as String
                        val userImageId = data["userImageId"] as String
                        val totalCostOfEvent = data["totalCostOfEvent"] as Number
                        val menuItemId = data["menuItemId"] as String
                        var payoutDates = data["payoutDates"] as ArrayList<String>
                        val typeOfService = data["typeOfService"] as String


                        var date = ""
                        var index = 0


                            for (i in 0 until eventDates.size) {
                                if (i == 0) {
                                    date = "${eventDates[i]} ${eventTimes[i]}"
                                    index = 0
                                } else {
                                    val time = sdfT.parse(date).time
                                    val day = "${eventDates[i]} ${eventTimes[i]}"
                                    val dayT = sdfT.parse(day).time
                                    if (dayT < time) {
                                        date = day
                                        index = i
                                    }
                                }
                                if (i == eventDates.size - 1) {
                                    val time = (sdfT.parse(date).time / 1000 / 60 / 60)
                                    val today = (System.currentTimeMillis() / 1000 / 60 / 60) + 1


                                    if (today >= time) {
                                        var dates = eventDates
                                        var times = eventTimes

                                        if (!payoutDates.contains(date)) {
                                            if (payoutDates.size + 1 == eventDates.size) {
                                                val data6: Map<String, Any> = hashMapOf("orderUpdate" to "orderComplete")
                                                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").document(menuItemId).update(data6)
                                                db.collection("Orders").document(menuItemId).update(data6)
                                            } else {
                                                val data6: Map<String, Any> = hashMapOf("payoutDates" to FieldValue.arrayUnion(date))
                                                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").document(menuItemId).update(data6)
                                                db.collection("Orders").document(menuItemId).update(data6)
                                                payoutDates.add("123")
                                            }
                                            val amountToBePayed = (totalCostOfEvent.toDouble() * 0.95) / (eventDates.size + 1)
                                            transfer(amountToBePayed, doc.id, userImageId, chefImageId, typeOfService)
                                        }

                                    }
                                }
                            }
                        }

                    }
                }
            }

    }

    private fun transfer(transferAmount: Double, orderId: String, userImageId: String, chefImageId: String, type: String) {
        @SuppressLint("SimpleDateFormat")
        var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener { document ->
        if (document != null) {
            val data = document.data

            val chargeForPayout = data?.get("chargeForPayout") as Double


            db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("BankingInfo").get().addOnSuccessListener { documents ->
                    if (documents != null) {
                        for (doc in documents.documents) {
                            val data = doc.data

                            val stripeId = data?.get("stripeAccountId") as String

                            if (transferAmount - chargeForPayout > 0) {
                            val final = "%.0f".format((
                                    transferAmount + chargeForPayout) * 100)

                                Log.d(TAG, "transfer: final $final")
                                val body = FormBody.Builder()
                                    .add("stripeAccountId", stripeId)
                                    .add("amount", final)
                                    .build()

                                val request = Request.Builder()
                                    .url("https://taiste-payments.onrender.com/transfer")
                                    .addHeader("Content-Type", "application/json; charset=utf-8")
                                    .post(body)
                                    .build()

                                httpClient.newCall(request)
                                    .enqueue(object : Callback {
                                        override fun onFailure(call: Call, e: IOException) {
                                            mHandler.post {

                                                val data1: Map<String, Any> = hashMapOf(
                                                    "transferId" to "",
                                                    "orderId" to orderId,
                                                    "date" to sdf.format(Date()),
                                                    "transferAmount" to 0.0,
                                                    "userImageId" to userImageId,
                                                    "chefImageId" to chefImageId
                                                )
                                                db.collection("TransferErrors").document(orderId)
                                                    .set(data1)
                                            }
                                        }

                                        override fun onResponse(call: Call, response: Response) {
                                            if (!response.isSuccessful) {
                                                mHandler.post {
                                                    displayAlert(
                                                        "Error: $response"
                                                    )

                                                    val data1: Map<String, Any> = hashMapOf(
                                                        "transferId" to "",
                                                        "orderId" to orderId,
                                                        "date" to sdf.format(Date()),
                                                        "transferAmount" to 0.0,
                                                        "userImageId" to userImageId,
                                                        "chefImageId" to chefImageId
                                                    )
                                                    db.collection("TransferErrors")
                                                        .document(orderId)
                                                        .set(data1)
                                                }
                                            } else {
                                                val responseData = response.body!!.string()
                                                val json =
                                                    JSONObject(responseData)

                                                val transferId = json.getString("transferId")

                                                mHandler.post {
                                                    val data1: Map<String, Any> = hashMapOf(
                                                        "transferId" to transferId,
                                                        "transferAmount" to final,
                                                        "orderId" to orderId,
                                                        "userImageId" to userImageId,
                                                        "date" to Date().toString(),
                                                        "chefImageId" to chefImageId
                                                    )

                                                    Toast.makeText(
                                                        context,
                                                        "Payout on its way",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    db.collection("Transfers").document(orderId)
                                                        .set(data1)

                                                    val data7: Map<String, Any> = hashMapOf("notifications" to "yes")
                                                    val data8: Map<String, Any> = hashMapOf("notification" to "A payout of $$transferAmount is on its way.", "date" to sdf.format(Date()))
                                                    db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).update(data7)
                                                    db.collection("Chef")
                                                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                                        .collection("Notifications").document(orderId)
                                                        .update(data8)

                                                }
                                            }
                                        }
                                    })

                            } else {
                                val amountOwed = chargeForPayout - transferAmount
                                val data5: Map<String, Any> = hashMapOf("notification" to "You still owed $$chargeForPayout. Your new amount owed is $$amountOwed.", "date" to sdf.format(Date()))
                                val data6: Map<String, Any> = hashMapOf("notifications" to "yes")
                                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Notifications").document().set(data5)
                                db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).update(data6)
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
       requireActivity().runOnUiThread {
            val builder = AlertDialog.Builder(requireContext())
                .setTitle("Failed to load page.")
                .setMessage(message)

            builder.setPositiveButton("Ok", null)
            builder
                .create()
                .show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadOrders(toggle: String) {
        binding.progressBar.isVisible = true
        db.collection("Chef").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Orders").addSnapshotListener { documents, error ->

                if (documents != null && error == null) {
                    for (doc in documents.documents) {
                        val data = doc.data


                        val cancelled = data?.get("cancelled") as String
                        val chefEmail = data["chefEmail"] as String
                        val chefImageId = data["chefImageId"] as String
                        val chefNotificationToken = data["chefNotificationToken"] as? String
                        val chefUsername = data["chefUsername"] as String
                        val city = data["city"] as String
                        val state = data["state"] as String
                        val distance = data["distance"] as String
                        val eventDates = data["eventDates"] as ArrayList<String>
                        val eventTimes = data["eventTimes"] as ArrayList<String>
                        val eventNotes = data["eventNotes"] as String
                        val eventQuantity = data["eventQuantity"] as String
                        val eventType = data["eventType"] as String
                        val itemDescription = data["itemDescription"] as String
                        val itemTitle = data["itemTitle"] as String
                        val location = data["location"] as String
                        val menuItemId = data["menuItemId"] as String
                        val numberOfEvents = data["numberOfEvents"] as Number
                        val orderDate = data["orderDate"] as String
                        val orderId = data["orderId"] as String
                        val orderUpdate = data["orderUpdate"] as String
                        val priceToChef = data["priceToChef"] as Number
                        val taxesAndFees = data["taxesAndFees"] as Number
                        val totalCostOfEvent = data["totalCostOfEvent"] as Number
                        val travelFeeExpenseOption = data["travelExpenseOption"] as String
                        val travelFee = data["travelFee"] as String
                        val travelFeeAccepted = data["travelFeeAccepted"] as String
                        val travelFeeRequested = data["travelFeeRequested"] as String
                        val typeOfService = data["typeOfService"] as String
                        val unitPrice = data["unitPrice"] as String
                        val user = data["user"] as String
                        val userImageId = data["userImageId"] as String
                        val creditsApplied = data["creditsApplied"] as String
                        val creditIds = data["creditIds"] as ArrayList<String>
                        val userNotificationToken = data["userNotificationToken"] as String
                        val allergies = data["allergies"] as String
                        val additionalMenuItems = data["additionalMenuItems"] as String
                        val documentId = doc.id
                        val payoutDates = data["payoutDates"] as ArrayList<String>
//                        val userName = data["userName"] as String
//                        val userEmail = data["userEmail"] as String

                        if (toggle == "Pending" || toggle == "Pend") {
                            if (orderUpdate == "pending") {
                                val newOrder = PendingOrders(
                                    cancelled,
                                    chefEmail,
                                    chefImageId,
                                    "chefNotificationToken",
                                    chefUsername,
                                    city,
                                    distance,
                                    eventDates,
                                    eventTimes,
                                    eventNotes,
                                    eventType,
                                    eventQuantity,
                                    itemDescription,
                                    itemTitle,
                                    location,
                                    menuItemId,
                                    numberOfEvents,
                                    orderDate,
                                    orderId,
                                    orderUpdate,
                                    priceToChef,
                                    state,
                                    taxesAndFees,
                                    totalCostOfEvent,
                                    travelFeeExpenseOption,
                                    travelFee,
                                    travelFeeAccepted,
                                    travelFeeRequested,
                                    typeOfService,
                                    unitPrice,
                                    user,
                                    userImageId,
                                    userNotificationToken,
                                    documentId,
                                    creditsApplied,
                                    creditIds,
                                    allergies,
                                    additionalMenuItems,
                                    "userName",
                                     "userEmail",
                                    payoutDates
                                )

                                if (pendingOrders.isEmpty()) {
                                    pendingOrders.add(newOrder)
                                    pendingAdapter.submitList(pendingOrders)
                                    pendingAdapter.notifyItemInserted(0)
                                    Log.d(TAG, "loadOrders: happening")
                                } else {
                                    val index =
                                        pendingOrders.indexOfFirst { it.documentId == documentId }
                                    if (index == -1) {
                                        pendingOrders.add(newOrder)
                                        pendingAdapter.submitList(pendingOrders)
                                        pendingAdapter.notifyItemInserted(pendingOrders.size - 1)
                                    }
                                }
                            }}

                            if (toggle == "Scheduled") {
                            if (orderUpdate == "scheduled") {
                                val newOrder = ScheduledOrders(
                                    cancelled,
                                    chefEmail,
                                    chefImageId,
                                    "chefNotificationToken",
                                    chefUsername,
                                    city,
                                    distance,
                                    eventDates,
                                    eventTimes,
                                    eventNotes,
                                    eventType,
                                    eventQuantity,
                                    itemDescription,
                                    itemTitle,
                                    location,
                                    menuItemId,
                                    numberOfEvents,
                                    orderDate,
                                    orderId,
                                    orderUpdate,
                                    priceToChef,
                                    state,
                                    taxesAndFees,
                                    totalCostOfEvent,
                                    travelFeeExpenseOption,
                                    travelFee,
                                    travelFeeAccepted,
                                    travelFeeRequested,
                                    typeOfService,
                                    unitPrice,
                                    user,
                                    userImageId,
                                    userNotificationToken,
                                    documentId,
                                    creditsApplied,
                                    creditIds,
                                    allergies, additionalMenuItems, "userName", "userEmail", payoutDates
                                )
                                pendingAdapter.notifyDataSetChanged()
                                if (scheduledOrders.isEmpty()) {
                                    scheduledOrders.add(newOrder)
                                    scheduledAdapter.submitList(scheduledOrders)
                                    scheduledAdapter.notifyItemInserted(0)

                                } else {
                                    val index =
                                        scheduledOrders.indexOfFirst { it.documentId == documentId }
                                    if (index == -1) {
                                        scheduledOrders.add(newOrder)
                                        scheduledAdapter.submitList(scheduledOrders)
                                        scheduledAdapter.notifyItemInserted(scheduledOrders.size - 1)
                                    }
                                }
                            }}
                            if (toggle == "Complete") {
                            if (orderUpdate == "complete") {
                                val newOrder = CompleteOrders(
                                    cancelled,
                                    chefEmail,
                                    chefImageId,
                                    "chefNotificationToken",
                                    chefUsername,
                                    city,
                                    distance,
                                    eventDates,
                                    eventTimes,
                                    eventNotes,
                                    eventType,
                                    eventQuantity,
                                    itemDescription,
                                    itemTitle,
                                    location,
                                    menuItemId,
                                    numberOfEvents,
                                    orderDate,
                                    orderId,
                                    orderUpdate,
                                    priceToChef,
                                    state,
                                    taxesAndFees,
                                    totalCostOfEvent,
                                    travelFeeExpenseOption,
                                    travelFee,
                                    travelFeeAccepted,
                                    travelFeeRequested,
                                    typeOfService,
                                    unitPrice,
                                    user,
                                    userImageId,
                                    userNotificationToken,
                                    documentId,
                                    creditsApplied,
                                    creditIds,
                                    allergies, additionalMenuItems, "userName", "userEmail"
                                )

                                if (completeOrders.isEmpty()) {
                                    completeOrders.add(newOrder)
                                    completeAdapter.submitList(completeOrders)
                                    completeAdapter.notifyItemInserted(0)
                                } else {
                                    val index =
                                        completeOrders.indexOfFirst { it.documentId == documentId }
                                    if (index == -1) {
                                        completeOrders.add(newOrder)
                                        completeAdapter.submitList(completeOrders)
                                        completeAdapter.notifyItemInserted(completeOrders.size - 1)
                                    }
                                }
                            }
                        }
                        binding.progressBar.isVisible = false
                        if ((toggle == "Pending" && pendingOrders.size == 0)) {
                            binding.noItemsText.text = "No Pending Items Yet."
                            binding.pendingRecyclerView.isVisible = false
                            binding.noItemsText.isVisible = true
                            binding.progressBar.isVisible = false
                        } else if (toggle == "Scheduled" && scheduledOrders.size == 0) {
                            binding.noItemsText.text = "No Scheduled Items Yet."
                            binding.scheduledRecyclerView.isVisible = false
                            binding.progressBar.isVisible = false
                            binding.noItemsText.isVisible = true
                        } else if (toggle == "Complete" && completeOrders.size == 0) {
                            binding.noItemsText.text = "No Complete Items Yet."
                            binding.completeRecyclerView.isVisible = false
                            binding.noItemsText.isVisible = true
                            binding.progressBar.isVisible = false
                        }
                    }
                }

        }

        binding.progressBar.isVisible = false
    }


}