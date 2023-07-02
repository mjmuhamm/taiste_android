package com.ruh.taiste.both.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ruh.taiste.R
import com.ruh.taiste.both.models.SecondComments
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

private const val TAG = "SecondCommentAdapter"
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SecondCommentAdapter(private val context: Context, private var comments: MutableList<SecondComments>, private var videoId: String, private val firstCommentId: String, private var messageText: EditText, private var sendMessage: ImageView, private var viewRepliesText: TextView) : RecyclerView.Adapter<SecondCommentAdapter.ViewHolder>()  {

    private val db = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser!!.email!!
    private val chefOrUserI = FirebaseAuth.getInstance().currentUser!!.displayName

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("MM-dd-yyyy hh:mm:ss.SSS a")


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(SecondCommentPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(comments[position])
        val comment = comments[position]


        holder.dateReplyText.setOnClickListener {
            messageText.hint = "reply to ${comment.username}"
            openSoftKeyboard(context, messageText)
        }
        val docRef = db.collection("Videos").document(videoId).collection("Comments").document(comment.documentId).collection("Comments")
        sendMessage.setOnClickListener {


            val chefOrUser = if (chefOrUserI == "Chef") {
                "chefs" } else { "users" }
            val username = if (chefOrUser == "Chef") {
                userName } else { chefUsername }

            val imageId = if (chefOrUser == "Chef") {
                chefImageId } else { userImageId }

            val image = if (chefOrUser == "Chef") { chefImage } else { userImage }

            val date = sdf.format(Date())
            val documentId = UUID.randomUUID().toString()

            val data : Map<String, Any> = hashMapOf("chefOrUser" to chefOrUser, "comment" to messageText.text.toString(), "date" to date, "repliedToUser" to "", "secondComments" to 0, "user" to user, "userImageId" to imageId, "username" to username)

            docRef.document(documentId).set(data)


            db.collection("Videos").document(videoId).collection("Comments").document(comment.documentId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val data1 = document.data

                    if (data1?.get("secondComments") != null) {
                        val comments = data1["secondComments"] as Number
                        val data2 : Map<String, Any> = hashMapOf("secondComments" to comments.toInt() + 1)
                        docRef.document(documentId).update(data2)
                    } else {
                        val data2 : Map<String, Any> = hashMapOf("secondComments" to  1)
                        docRef.document(documentId).update(data2)
                    }
                }
            }
            comment.secondComments = comment.secondComments.toInt() + 1
            viewRepliesText.text = "View Replies (${comment.secondComments}) "
            val newComment = SecondComments(chefOrUser, messageText.text.toString(), date, comment.user, user, imageId, image, 0, arrayListOf(), username, documentId)
            comments.add(newComment)
            comments.sortBy { it.date }
            messageText.setText("")
            notifyItemInserted(comments.size - 1)
            Toast.makeText(context, "Comment Sent", Toast.LENGTH_LONG).show()
        }

        holder.likeButton.setOnClickListener {
            holder.likeButton.isSelected = !holder.likeButton.isSelected
            if (holder.likeButton.isSelected) {
                holder.likeImage.setImageResource(R.drawable.heart_filled)
                holder.likeText.text = "${holder.likeText.text.toString().toInt() + 1}"
                val data: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayUnion(user))
                db.collection("Videos").document(videoId).collection("Comments").document(firstCommentId).collection("Comments").document(comment.documentId).update(data)
            } else {
                holder.likeImage.setImageResource(R.drawable.heart_unfilled)
                holder.likeText.text = "${holder.likeText.text.toString().toInt() - 1}"
                val data: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayRemove(user))
                db.collection("Videos").document(videoId).collection("Comments").document(firstCommentId).collection("Comments").document(comment.documentId).update(data)
            }
        }
        
    }

    override fun getItemCount() = comments.size

    fun submitList(comments: MutableList<SecondComments>, videoId: String) {
        this.comments = comments
        this.videoId = videoId
    }

    class ViewHolder(itemView: SecondCommentPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        val context = itemView.root.context!!
        val userImage = itemView.userImage
        val userName = itemView.userName
        private val commentText = itemView.comment
        val dateReplyText = itemView.dateReplyText
        val likeButton = itemView.likeButton
        val likeImage = itemView.likeImage
        val likeText = itemView.likeText

        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun bind(comment: SecondComments) {
            Glide.with(context).load(comment.userImage).placeholder(R.drawable.default_profile).into(userImage)
            userName.text = comment.username

            val sdf = SimpleDateFormat("MM-dd-yyyy hh:mm:ss.SSS a")
            val user = FirebaseAuth.getInstance().currentUser!!.email!!
            Log.d(TAG, "bind:  date ${comment.date}")

            val date = sdf.parse(comment.date).time
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

            if (comment.liked.contains(user)) {
                likeButton.isSelected = true
                likeImage.setImageResource(R.drawable.heart_filled)
            } else {
                likeButton.isSelected = false
                likeImage.setImageResource(R.drawable.heart_unfilled)
            }

            commentText.text = comment.comment
            likeText.text = "${comment.liked.size}"

        }

    }


}