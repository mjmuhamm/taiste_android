@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.ruh.taiste.both.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ruh.taiste.R
import com.ruh.taiste.both.ProfileAsUser
import com.ruh.taiste.both.models.Replies
import com.ruh.taiste.chef.chefImage
import com.ruh.taiste.chef.chefImageId
import com.ruh.taiste.chef.chefUsername
import com.ruh.taiste.databinding.SecondCommentPostBinding
import com.ruh.taiste.user.userImage
import com.ruh.taiste.user.userImageId
import com.ruh.taiste.user.userName
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "RepliesAdapter"
class RepliesAdapter(private val context: Context, private var replies: MutableList<Replies>, private var messageText: EditText, private var sendMessage: ImageView) : RecyclerView.Adapter<RepliesAdapter.ViewHolder>()  {

    private val db = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser!!.email!!
    private val chefOrUserI = FirebaseAuth.getInstance().currentUser!!.displayName

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("MM-dd-yyyy hh:mm:ss.SSS a")

    @SuppressLint("SimpleDateFormat")
    private val sdfMS = SimpleDateFormat("MM-dd-yyyy hh:mm:ss.SSS a")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(SecondCommentPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(replies[position])
        val reply = replies[position]

        holder.dateReplyText.setOnClickListener {
            messageText.hint = "reply to ${reply.user}"
            openSoftKeyboard(context, messageText)
        }

        val docRef = db.collection(reply.itemType).document(reply.menuItemId).collection("UserReviews").document(reply.reviewItemID).collection("Replies")
        sendMessage.setOnClickListener {


            val chefOrUser = if (chefOrUserI == "Chef") {
                "chefs" } else { "users" }
            val username = if (chefOrUser == "Chef") {
                userName } else { chefUsername }

            val imageId = if (chefOrUser == "Chef") {
                chefImageId } else { userImageId }

            val image = if (chefOrUser == "Chef") { chefImage } else { userImage }

            val date = sdf.format(Date())
            val msDate = sdfMS.format(Date())

            val documentId = UUID.randomUUID().toString()
            val data : Map<String, Any> = hashMapOf("chefOrUser" to chefOrUser, "message" to messageText.text.toString(), "date" to date, "milliSecondDate" to msDate, "replyToUser" to "", "user" to user, "userImageId" to imageId, "username" to username)

            docRef.document(documentId).set(data)

            db.collection(reply.itemType).document(reply.menuItemId).collection("UserReviews").document(reply.reviewItemID).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val data1 = document.data

                    if (data1?.get("numberOfReplies") != null) {
                        val numberOfReplies = data1["numberOfReplies"] as Number
                        val data2 : Map<String, Any> = hashMapOf("numberOfReplies" to numberOfReplies.toInt() + 1)
                        docRef.document(reply.reviewItemID).update(data2)
                    } else {
                        val data2 : Map<String, Any> = hashMapOf("numberOfReplies" to  1)
                        docRef.document(reply.reviewItemID).update(data2)
                    }
                }
            }



            val newReply = Replies(chefOrUser, date, messageText.text.toString(), date, reply.user, reply.liked, reply.reviewItemID, user, imageId, image, reply.itemType, reply.menuItemId, reply.documentId)
            replies.add(newReply)
            replies.sortBy { it.date }
            notifyItemInserted(replies.size - 1)
            messageText.setText("")
            Toast.makeText(context, "Reply Sent", Toast.LENGTH_LONG).show()
        }

        holder.userImage.setOnClickListener {
            val intent = Intent(context, ProfileAsUser::class.java)
            intent.putExtra("chef_or_user", reply.chefOrUser)
            intent.putExtra("user" , reply.user)
            context.startActivity(intent)
        }
        holder.likeButton.setOnClickListener {
            holder.likeButton.isSelected = !holder.likeButton.isSelected
            if (holder.likeButton.isSelected) {
                holder.likeImage.setImageResource(R.drawable.heart_filled)
                holder.likeText.text = "${holder.likeText.text.toString().toInt() + 1}"
                val data: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayUnion(user))
                db.collection(reply.itemType).document(reply.menuItemId).collection("UserReviews").document(reply.reviewItemID).collection("Replies").document(reply.documentId).update(data)
            } else {
                holder.likeImage.setImageResource(R.drawable.heart_unfilled)
                holder.likeText.text = "${holder.likeText.text.toString().toInt() - 1}"
                val data: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayRemove(user))
                db.collection(reply.itemType).document(reply.menuItemId).collection("UserReviews").document(reply.reviewItemID).collection("Replies").document(reply.documentId).update(data)
            }
        }

    }


    override fun getItemCount() = replies.size

    fun submitList(replies: MutableList<Replies>) {
        this.replies = replies
    }

    class ViewHolder(itemView: SecondCommentPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        val context = itemView.root.context!!
        val userImage = itemView.userImage
        val userName = itemView.userName
        val message = itemView.comment
        val dateReplyText = itemView.dateReplyText
        val likeButton = itemView.likeButton
        val likeImage = itemView.likeImage
        val likeText = itemView.likeText


        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun bind(reply: Replies) {
            Glide.with(context).load(reply.userImage).placeholder(R.drawable.default_profile).into(userImage)
            userName.text = reply.user

            val sdf = SimpleDateFormat("MM-dd-yyyy hh:mm:ss.SSS a")
            val user = FirebaseAuth.getInstance().currentUser!!.email!!
            Log.d(TAG, "bind:  date ${reply.date}")


            val date = sdf.parse(reply.date).time
            val today = Timestamp(System.currentTimeMillis()).time

            val now = today - date
//
            val seconds = now / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            val weeks = days / 7
            val months = weeks / 30
            val years = months / 12

            when {
                years > 0 -> {
                    dateReplyText.text = "${years}y  Reply"
                }
                months > 0 -> {
                    dateReplyText.text = "${months}mo Reply"
                }
                weeks > 0 -> {
                    dateReplyText.text = "${weeks}w  Reply"
                }
                days > 0 -> {
                    dateReplyText.text = "${days}d  Reply"
                }
                hours > 0 -> {
                    dateReplyText.text = "${hours}h  Reply"
                }
                minutes > 0 -> {
                    dateReplyText.text = "${minutes}m  Reply"
                }
                else -> {
                    dateReplyText.text = "${seconds}s  Reply"
                }
            }



            if (reply.liked.contains(user)) {
                likeButton.isSelected = true
                likeImage.setImageResource(R.drawable.heart_filled)
            } else {
                likeButton.isSelected = false
                likeImage.setImageResource(R.drawable.heart_unfilled)
            }

            message.text = reply.message
            likeText.text = "${reply.liked.size}"

        }

    }


}