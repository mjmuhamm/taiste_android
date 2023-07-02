package com.ruh.taiste.both

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.R
import com.ruh.taiste.both.adapters.MessageAdapter
import com.ruh.taiste.both.adapters.NotificationAdapter
import com.ruh.taiste.both.models.MessageNotifications
import com.ruh.taiste.both.models.Notifications
import com.ruh.taiste.databinding.ActivityNotificationsBinding
import com.ruh.taiste.isOnline
import com.ruh.taiste.isOnline1

var guserName = ""
private const val TAG = "Notifications"
class Notifications : AppCompatActivity() {
    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private lateinit var binding : ActivityNotificationsBinding

    private lateinit var notificationAdapter: NotificationAdapter
    private var toggle = "MessageRequests"
    private var messages : MutableList<MessageNotifications> = arrayListOf()
    private var notifications: MutableList<Notifications> = arrayListOf()

    private var chefOrUser = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isOnline(this)
            } else {
                isOnline1(this)
            }
        ) {

        if (FirebaseAuth.getInstance().currentUser != null) {
        notificationAdapter = NotificationAdapter(this, FirebaseAuth.getInstance().currentUser!!.displayName!!, toggle, messages, notifications)
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.messagesRecyclerView.adapter = notificationAdapter

        binding.notificationsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notificationsRecyclerView.adapter = notificationAdapter

            chefOrUser = intent.getStringExtra("chef_or_user").toString()

            loadUsername()
            loadNotifications()
        } else {
            Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
        }
            binding.backButton.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            binding.messageButton.setOnClickListener {
                if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        isOnline(this)
                    } else {
                        isOnline1(this)
                    }
                ) {

                toggle = "MessageRequests"
                binding.messagesRecyclerView.visibility = View.VISIBLE
                binding.notificationsRecyclerView.visibility = View.GONE
                loadNotifications()
                binding.messageButton.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.messageButton.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.notificationButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.notificationButton.setTextColor(ContextCompat.getColor(this, R.color.main))
                } else {
                    Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
                }
            }

            binding.notificationButton.setOnClickListener {
                if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        isOnline(this)
                    } else {
                        isOnline1(this)
                    }
                ) {

                toggle = "Notifications"
                binding.messagesRecyclerView.visibility = View.GONE
                binding.notificationsRecyclerView.visibility = View.VISIBLE
                loadNotifications()

                binding.notificationButton.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
                binding.notificationButton.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.messageButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.messageButton.setTextColor(ContextCompat.getColor(this, R.color.main))
                } else {
                    Toast.makeText(this, "Seems to be a problem with your internet. Please check your connection.", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
        }
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

    @SuppressLint("NotifyDataSetChanged")
    private fun loadNotifications() {
        val data1: Map<String, Any> = hashMapOf("notifications" to "")
        notificationAdapter.submitList(messages, notifications, toggle)
        notificationAdapter.notifyDataSetChanged()


        db.collection(FirebaseAuth.getInstance().currentUser!!.displayName!!).document(FirebaseAuth.getInstance().currentUser!!.uid).update(data1)
        if (toggle == "MessageRequests") {
            if (messages.count() > 0) {
                notificationAdapter.submitList(
                    messages,
                    notifications,
                    toggle
                )
                notificationAdapter.notifyDataSetChanged()
            }
            db.collection(FirebaseAuth.getInstance().currentUser!!.displayName!!).document(FirebaseAuth.getInstance().currentUser!!.uid).collection("MessageRequests").get().addOnSuccessListener { documents ->

                if (documents != null) {
                    for (doc in documents.documents) {
                        val data = doc.data

                        val chefOrUser = data?.get("chefOrUser") as String
                        val userImageId = data["user"] as String
                        val userEmail = data["userEmail"] as String
                        val userName = data["userName"] as String
                        val date = data["date"] as String
                        val a = if (chefOrUser == "Chef") {
                            "chefs"
                        } else {
                            "users"
                        }
                        storage.reference.child("$a/$userEmail/profileImage/$userImageId.png").downloadUrl.addOnSuccessListener { uri ->

                            val message = MessageNotifications(
                                chefOrUser,
                                userImageId,
                                userEmail,
                                uri,
                                userName,
                                date,
                                doc.id
                            )
                            if (messages.size == 0) {
                                messages.add(message)
                                notificationAdapter.submitList(
                                    messages,
                                    notifications,
                                    toggle
                                )
                                notificationAdapter.notifyItemInserted(0)
                            } else {
                                val index =
                                    messages.indexOfFirst { it.userImageId == userImageId }
                                if (index == -1) {
                                    messages.add(message)
                                    messages.sortBy { it.date }
                                    notificationAdapter.submitList(
                                        messages,
                                        notifications,
                                        toggle
                                    )
                                    notificationAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }

                }

                }
        }  else {
            if (notifications.count() > 0) {
                notificationAdapter.submitList(
                    messages,
                    notifications,
                    toggle
                )
                notificationAdapter.notifyDataSetChanged()
            }
            db.collection(FirebaseAuth.getInstance().currentUser!!.displayName!!)
                .document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Notifications").addSnapshotListener { documents, error ->
                    if (error == null) {
                        if (documents != null) {
                            for (doc in documents.documents) {
                                val data = doc.data

                                val notification = data?.get("notification") as String
                                val date = data["date"] as String



                                val notification1 = Notifications(notification, date, doc.id)
                                if (notifications.size == 0) {
                                    notifications.add(notification1)
                                    notificationAdapter.submitList(messages, notifications, toggle)
                                    notificationAdapter.notifyItemInserted(0)

                                } else {
                                    val index = notifications.indexOfFirst { it.documentId == doc.id }

                                    if (index == -1) {
                                        notifications.add(notification1)
                                        notifications.sortByDescending { it.date }
                                        notificationAdapter.submitList(messages, notifications, toggle)
                                        notificationAdapter.notifyDataSetChanged()
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

}