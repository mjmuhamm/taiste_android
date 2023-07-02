package com.ruh.taiste.both.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ruh.taiste.R
import com.ruh.taiste.both.ProfileAsUser
import com.ruh.taiste.both.guserName
import com.ruh.taiste.both.models.MessageNotifications
import com.ruh.taiste.both.models.Messages
import com.ruh.taiste.both.models.Notifications
import com.ruh.taiste.databinding.MessagePostBinding
import com.ruh.taiste.databinding.NotificationPostBinding

private const val TAG = "NotificationAdapter"
class NotificationAdapter(private val context: Context, private var chefOrUser: String, private var toggle: String, private var messages: MutableList<MessageNotifications>, private var notifications: MutableList<Notifications>) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>()  {

    private val db = Firebase.firestore
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(NotificationPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: toggle $toggle")
        if (toggle == "Notifications") {
            holder.bind(notifications[position])
        } else {
            holder.bind1(messages[position])
        }

        Log.d(TAG, "onBindViewHolder:toggle $toggle")


        holder.userImage.setOnClickListener {
            val message = messages[position]
            val intent = Intent(context, ProfileAsUser::class.java)
            intent.putExtra("userImageId", message.chefOrUser)
            intent.putExtra("user", message.userImageId)
            context.startActivity(intent)
        }

        holder.click.setOnClickListener {
            val message = messages[position]

            val intent = Intent(context, com.ruh.taiste.both.Messages::class.java)

            val senderImageId = FirebaseAuth.getInstance().currentUser!!.uid
            val receiverImageId = message.userImageId
            val chefOrUser = FirebaseAuth.getInstance().currentUser!!.displayName
            val documentId = message.documentId
            val senderUserName = guserName
            val receiverUserName = holder.userName.text
            val senderEmail = FirebaseAuth.getInstance().currentUser!!.email!!
            val receiverEmail = message.userEmail
            val receiverChefOrUser = message.chefOrUser

            intent.putExtra("message_request_sender_image_id", senderImageId)
            intent.putExtra("message_request_receiver_image_id",  receiverImageId)
            intent.putExtra("message_request_chef_or_user", chefOrUser)
            intent.putExtra("message_request_receiver_chef_or_user", receiverChefOrUser)
            intent.putExtra("message_request_document_id", documentId)
            intent.putExtra("message_request_receiver_name", receiverUserName)
            intent.putExtra("message_request_sender_name", senderUserName)
            intent.putExtra("message_request_receiver_email", receiverEmail)
            intent.putExtra("message_request_sender_email", senderEmail)
            intent.putExtra("travel_fee_or_message", "MessageRequests")

            context.startActivity(intent)
        }



    }

    override fun getItemCount() = if (toggle == "Notifications") { notifications.size } else { messages.size }

    fun submitList(messages: MutableList<MessageNotifications>, notifications: MutableList<Notifications>, toggle: String) {
        this.messages = messages
        this.notifications = notifications
        this.toggle = toggle
    }

    class ViewHolder(itemView: NotificationPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        val context = itemView.root.context!!

        val messageView = itemView.messagesView
        val notificationView = itemView.notificationsView
        val userImage = itemView.userImage
        val userName = itemView.userName
        val click = itemView.click
        val text = itemView.text
        val notification = itemView.notification
        val date = itemView.date

        @SuppressLint("SetTextI18n")
        fun bind(item: Notifications) {
            messageView.visibility = View.GONE
            notificationView.visibility = View.VISIBLE
            notification.text = "@${item.notification}"
            date.text = item.date
        }

        @SuppressLint("SetTextI18n")
        fun bind1(item: MessageNotifications) {
            messageView.visibility = View.VISIBLE
            notificationView.visibility = View.GONE
            Glide.with(context).load(item.userImage).placeholder(R.drawable.default_profile).into(userImage)
            userName.text = item.userName
            text.text = "Click here to view your message request from @${item.userName}."

        }

    }
}