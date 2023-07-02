package com.ruh.taiste.both.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ruh.taiste.R
import com.ruh.taiste.both.models.Comments
import com.ruh.taiste.both.models.SecondComments
import com.ruh.taiste.chef.chefImage
import com.ruh.taiste.chef.chefImageId
import com.ruh.taiste.chef.chefUsername
import com.ruh.taiste.databinding.CommentPostBinding
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
import kotlin.collections.ArrayList

private const val TAG = "CommentsAdapter"
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class CommentsAdapter(private val context: Context, private var comments: MutableList<Comments>, private var videoId: String, private var messageText: EditText, private var sendMessage: ImageView) : RecyclerView.Adapter<CommentsAdapter.ViewHolder>()  {


    private val db = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser!!.email!!
    private val chefOrUserI = FirebaseAuth.getInstance().currentUser!!.displayName

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("MM-dd-yyyy hh:mm:ss.SSS a")

    private val secondComments : MutableList<SecondComments> = arrayListOf()
    private lateinit var secondCommentAdapter: SecondCommentAdapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(CommentPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(comments[position])
        val comment = comments[position]


        secondCommentAdapter = SecondCommentAdapter(context, secondComments, videoId, comment.documentId, messageText, sendMessage, holder.viewReplyText)
        holder.secondCommentRecyclerView.layoutManager = LinearLayoutManager(context)
        holder.secondCommentRecyclerView.adapter = secondCommentAdapter

        holder.viewReplyLayout.setOnClickListener {
            Log.d(TAG, "onBindViewHolder: happening")
            holder.viewReplyLayout.isSelected = !holder.viewReplyLayout.isSelected

            if (holder.viewReplyLayout.isSelected) {

                if (secondComments.size == 0) {
                    db.collection("Videos").document(videoId).collection("Comments")
                        .document(comment.documentId).collection("Comments").get()
                        .addOnSuccessListener { documents ->
                            if (documents != null) {
                                for (doc in documents.documents) {
                                    val data = doc.data

                                    val chefOrUser = data?.get("chefOrUser") as String
                                    val commentI = data["comment"] as String
                                    val date = data["date"] as String
                                    val repliedToUser = data["repliedToUser"] as String
                                    val user = data["user"] as String
                                    val userImageId = data["userImageId"] as String
                                    val username = data["username"] as String
                                    var liked = arrayListOf<String>()
                                    val documentId = doc.id
                                    var secondCommentsNum = 0

                                    if (data["secondComments"] != null) {
                                        val secondCommentsI = data["secondComments"] as Number
                                        secondCommentsNum = secondCommentsI.toInt()
                                    }

                                    if (data["liked"] != null) {
                                        liked = data["liked"] as ArrayList<String>
                                    }

                                    val secondComment = SecondComments(
                                        chefOrUser,
                                        commentI,
                                        date,
                                        repliedToUser,
                                        user,
                                        userImageId,
                                        chefImage,
                                        secondCommentsNum,
                                        liked,
                                        username,
                                        documentId
                                    )

                                    if (secondComments.isEmpty()) {
                                        secondComments.add(secondComment)
                                        secondCommentAdapter.submitList(secondComments, videoId)
                                        secondCommentAdapter.notifyItemInserted(0)
                                        holder.secondCommentRecyclerViewLayout.visibility = View.VISIBLE
                                    } else {
                                        val index =
                                            secondComments.indexOfFirst { it.documentId == documentId }
                                        if (index == -1) {
                                            secondComments.add(secondComment)
                                            secondCommentAdapter.submitList(secondComments, videoId)
                                            secondCommentAdapter.notifyItemInserted(secondComments.size - 1)
                                            holder.secondCommentRecyclerViewLayout.visibility =
                                                View.VISIBLE
                                        }
                                    }
//                            storage.reference.child("$chefOrUser/$user/profileImage/$userImageId.png").downloadUrl
//
//                                .addOnSuccessListener { userUri ->
//
//                                }

//                                .addOnFailureListener {
//                                    val secondComment = SecondComments(chefOrUser, comment, date, repliedToUser, user, userImageId, chefImage, liked, username, documentId)
//
//                                    if (secondComments.isEmpty()) {
//                                        secondComments.add(secondComment)
//                                        secondCommentAdapter.submitList(secondComments, videoId)
//                                        secondCommentAdapter.notifyItemInserted(0)
//                                    } else {
//                                        val index = secondComments.indexOfFirst { it.documentId == documentId }
//                                        if (index == -1) {
//                                            secondComments.add(secondComment)
//                                            secondCommentAdapter.submitList(secondComments, videoId)
//                                            secondCommentAdapter.notifyItemInserted(secondComments.size - 1)
//                                        }
//                                    }
//                                }
                                }
                            }
                        }
                } else {
                    holder.secondCommentRecyclerViewLayout.visibility =
                        View.VISIBLE
                }
            } else {
                secondComments.clear()
                holder.secondCommentRecyclerViewLayout.visibility = View.GONE
            }
        }

        holder.dateReplyText.setOnClickListener {
            messageText.hint = "reply to ${comment.username}"
            openSoftKeyboard(context, messageText)
        }

        val docRef = db.collection("Videos").document(videoId).collection("Comments").document(comment.documentId).collection("Comments")
        sendMessage.setOnClickListener {

            if (messageText.text != null) {

                val chefOrUser = if (chefOrUserI == "Chef") {
                    "chefs"
                } else {
                    "users"
                }
                val username = if (chefOrUser == "Chef") {
                    userName
                } else {
                    chefUsername
                }

                val imageId = if (chefOrUser == "Chef") {
                    chefImageId
                } else {
                    userImageId
                }

                val image = if (chefOrUser == "Chef") {
                    chefImage
                } else {
                    userImage
                }

                val date = sdf.format(Date())

                val documentId = UUID.randomUUID().toString()

                val data: Map<String, Any> = hashMapOf(
                    "chefOrUser" to chefOrUser,
                    "comment" to messageText.text.toString(),
                    "date" to date,
                    "likes" to ArrayList<String>(),
                    "userImageId" to FirebaseAuth.getInstance().currentUser!!.uid,
                    "username" to username,
                    "userEmail" to FirebaseAuth.getInstance().currentUser!!.email!!
                )

                docRef.document(documentId).set(data)
                db.collection("Videos").document(videoId).collection("UserComments")
                    .document(comment.documentId).get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val data1 = document.data

                        if (data1?.get("secondComments") != null) {
                            val comments = data1["secondComments"] as Number
                            val data2: Map<String, Any> =
                                hashMapOf("secondComments" to comments.toInt() + 1)
                            docRef.document(documentId).update(data2)
                        } else {
                            val data2: Map<String, Any> = hashMapOf("secondComments" to 1)
                            docRef.document(documentId).update(data2)
                        }
                    }
                }
                comment.secondComments = comment.secondComments.toInt() + 1
                holder.viewReplyText.text = "View Replies (${comment.secondComments}) "
                val newComment = SecondComments(
                    chefOrUser,
                    messageText.text.toString(),
                    date,
                    comment.username,
                    user,
                    imageId,
                    image,
                    0,
                    arrayListOf(),
                    username,
                    documentId
                )
                secondComments.add(newComment)
                secondComments.sortBy { it.date }
                secondCommentAdapter.submitList(secondComments, videoId)
                secondCommentAdapter.notifyItemInserted(secondComments.size - 1)
                messageText.setText("")
                notifyItemInserted(secondComments.size - 1)
                Toast.makeText(context, "Comment Sent", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Please enter a message first.", Toast.LENGTH_LONG).show()
            }
        }

        holder.hideButton.setOnClickListener {
            secondComments.clear()
            holder.viewReplyLayout.isSelected = false
            holder.secondCommentRecyclerViewLayout.visibility = View.GONE
        }

        holder.likeButton.setOnClickListener {
            holder.likeButton.isSelected = !holder.likeButton.isSelected
            if (holder.likeButton.isSelected) {
                holder.likeImage.setImageResource(R.drawable.heart_filled)
                holder.likeText.text = "${holder.likeText.text.toString().toInt() + 1}"
                val data: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayUnion(user))
                db.collection("Videos").document(videoId).collection("Comments").document(comment.documentId).update(data)
            } else {
                holder.likeImage.setImageResource(R.drawable.heart_unfilled)
                holder.likeText.text = "${holder.likeText.text.toString().toInt() - 1}"
                val data: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayRemove(user))
                db.collection("Videos").document(videoId).collection("Comments").document(comment.documentId).update(data)
            }
        }
        
    }


    override fun getItemCount() = comments.size

    fun submitList(comments: MutableList<Comments>, videoId: String) {
        this.comments = comments
        this.videoId = videoId
    }

    class ViewHolder(itemView: CommentPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        val context = itemView.root.context!!
        val userImage = itemView.userImage
        val userName = itemView.userName
        private val commentText = itemView.comment
        val dateReplyText = itemView.dateReplyText
        val viewReplyLayout = itemView.viewRepliesLayout
        val viewReplyText = itemView.viewRepliesText
        val secondCommentRecyclerViewLayout = itemView.secondCommentRecyclerViewLayout
        val secondCommentRecyclerView = itemView.secondCommentRecyclerView
        val hideButton = itemView.hideButton
        val likeButton = itemView.likeButton
        val likeImage = itemView.likeImage
        val likeText = itemView.likeText

        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun bind(comment: Comments) {
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

            if (comment.secondComments.toInt() > 0) {
                viewReplyLayout.visibility = View.VISIBLE
                viewReplyText.text = "View Replies (${comment.secondComments}) "
            } else {
                viewReplyLayout.visibility = View.GONE
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


fun openSoftKeyboard(context: Context, view: View) {
    view.requestFocus()
    // open the soft keyboard
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}