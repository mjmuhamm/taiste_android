package com.ruh.taiste.user.fragments

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ruh.taiste.R
import com.ruh.taiste.both.models.CompleteOrders
import com.ruh.taiste.databinding.FragmentOrdersBinding
import com.ruh.taiste.user.adapters.orders.PendingAdapter
import com.ruh.taiste.user.adapters.orders.ScheduledAdapter
import com.ruh.taiste.both.models.PendingOrders
import com.ruh.taiste.both.models.ScheduledOrders
import com.ruh.taiste.user.adapters.orders.CompleteAdapter
import com.ruh.taiste.user.userImage
import com.ruh.taiste.user.userLocation
import com.ruh.taiste.user.userPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.both.Notifications
import com.ruh.taiste.both.guserName
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


private const val TAG = "Orders"
class Orders : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding : FragmentOrdersBinding? = null

    private val binding get() = _binding!!

    private val db = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser!!.email!!

    private lateinit var pendingAdapter: PendingAdapter
    private lateinit var scheduledAdapter: ScheduledAdapter
    private lateinit var completeAdapter: CompleteAdapter

    private var pendingOrders: MutableList<PendingOrders> = ArrayList()
    private var scheduledOrders: MutableList<ScheduledOrders> = ArrayList()
    private var completeOrders: MutableList<CompleteOrders> = ArrayList()

    private var toggle = "Pendin"
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
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)

        binding.pendingRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.scheduledRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.completeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        pendingAdapter = PendingAdapter(requireContext(), pendingOrders)
        scheduledAdapter = ScheduledAdapter(requireContext(), scheduledOrders)
        completeAdapter = CompleteAdapter(requireContext(), completeOrders)

        binding.pendingRecyclerView.adapter = pendingAdapter
        binding.scheduledRecyclerView.adapter = scheduledAdapter
        binding.completeRecyclerView.adapter = completeAdapter

        binding.disclaimer.text = "*Orders appear hear until the chef accepts, and will transfer to the 'Schedule' tab after. If you cancel here, a full refund will be dispersed immediately.*"
        binding.pending.setOnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isOnline(requireContext())
                } else {
                    isOnline1(requireContext())
                }
            ) {

            binding.disclaimer.text = "*Orders appear hear until the chef accepts, and will transfer to the 'Schedule' tab after. If you cancel here, a full refund will be dispersed immediately.*"
            toggle = "Pending"
            if (FirebaseAuth.getInstance().currentUser != null) {
                loadOrders(toggle)
            } else {
                Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
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

            binding.disclaimer.text = "*Orders appear hear when the chef accepts. If you cancel within 7 days of the event you will be refunded 85% of the total order cost, otherwise, you can expect 95% of the total order cost to be refunded.*"
            toggle = "Scheduled"
            if (FirebaseAuth.getInstance().currentUser != null) {
                loadOrders(toggle)
            } else {
                Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
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
                    TODO("VERSION.SDK_INT < M")
                }
            ) {

            binding.disclaimer.text = "*Orders appear after completion. Please consider reviewing each order to help other users best decide on their selections.*"
            toggle = "Complete"
            if (FirebaseAuth.getInstance().currentUser != null) {
                loadOrders(toggle)
            } else {
                Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
            }
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
            intent.putExtra("chef_or_user", "User")
            startActivity(intent)
        }

        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(requireContext())
            } else {
                isOnline1(requireContext())
            }
        ) {

        if (FirebaseAuth.getInstance().currentUser != null) {
            loadNotifications()
            loadOrders(toggle)
            loadUsername()

        } else {
            Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
        }
        } else {
            Toast.makeText(requireContext(), "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }

        return binding.root
    }
    private fun loadUsername() {
        db.collection("Usernames").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val email = data?.get("email") as String
                    val username = data["username"] as String

                    if (email == FirebaseAuth.getInstance().currentUser!!.email!!) {
                        guserName = username
                    }
                }
            }
        }
    }

    private fun loadNotifications() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).addSnapshotListener { document, _ ->
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

    @SuppressLint("SetTextI18n")
    private fun loadOrders(toggle: String) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            binding.progressBar.isVisible = true
            db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("Orders").addSnapshotListener { documents, error ->
                if (documents != null && error == null) {
                    binding.progressBar.isVisible = false
                    for (doc in documents.documents) {
                        val data = doc.data

                        val cancelled = data?.get("cancelled") as String
                        val chefEmail = data["chefEmail"] as String
                        val chefImageId = data["chefImageId"] as String
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
                        val documentId = doc.id
                        Log.d(TAG, " document idloadOrders: $documentId")
                        Log.d(TAG, "loadOrders: ${FirebaseAuth.getInstance().currentUser!!.email!!}")
                        Log.d(TAG, "loadOrders: ${FirebaseAuth.getInstance().currentUser!!.uid}")
                        val allergies = data["allergies"] as String
                        val additionalMenuItems = data["additionalMenuItems"] as String
                        val userName = data["userName"] as String
                        val userEmail = data["userEmail"] as String
                        val payoutDates = data["payoutDates"] as ArrayList<String>


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
                                userName,
                                 userEmail,
                                payoutDates
                            )

                            if (pendingOrders.isEmpty()) {
                                pendingOrders.add(newOrder)
                                pendingAdapter.submitList(pendingOrders)
                                pendingAdapter.notifyItemInserted(0)
                            } else {
                                val index =
                                    pendingOrders.indexOfFirst { it.documentId == documentId }
                                if (index == -1) {
                                    pendingOrders.add(newOrder)
                                    pendingAdapter.submitList(pendingOrders)
                                    pendingAdapter.notifyItemInserted(pendingOrders.size - 1)
                                }
                            }

                        } else if (orderUpdate == "scheduled") {
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
                                allergies,
                                additionalMenuItems,
                                userName,
                                 userEmail,
                                payoutDates
                            )

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
                        } else if (orderUpdate == "complete") {
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
                                allergies,
                                additionalMenuItems,
                                userName, userEmail
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
                        binding.progressBar.isVisible = false
                        if (toggle == "Pending" && pendingOrders.isEmpty()) {
                            binding.noItemsText.text = "No Pending Items Yet."
                            binding.pendingRecyclerView.isVisible = false
                            binding.noItemsText.isVisible = true
                            binding.progressBar.isVisible = false
                        } else if (toggle == "Scheduled" && scheduledOrders.isEmpty()) {
                            binding.noItemsText.text = "No Scheduled Items Yet."
                            binding.noItemsText.isVisible = true
                            binding.scheduledRecyclerView.isVisible = false
                            binding.progressBar.isVisible = false
                        } else if (toggle == "Complete" && completeOrders.isEmpty()) {
                            binding.noItemsText.text = "No Complete Items Yet."
                            binding.completeRecyclerView.isVisible = false
                            binding.noItemsText.isVisible = true
                            binding.progressBar.isVisible = false
                        }

                    }
                }
            }

        } else {
            Toast.makeText(requireContext(), "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
        }
    }
}