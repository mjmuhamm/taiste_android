package com.ruh.taiste.both.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ruh.taiste.R
import com.ruh.taiste.both.ProfileAsUser
import com.ruh.taiste.both.models.Replies
import com.ruh.taiste.both.models.Reviews
import com.ruh.taiste.chef.chefImageId
import com.ruh.taiste.chef.chefUsername
import com.ruh.taiste.databinding.ReviewPostBinding
import com.ruh.taiste.user.userImage
import com.ruh.taiste.user.userImageId
import com.ruh.taiste.user.userName
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "ReviewsAdapter"
class ReviewsAdapter(private val context: Context, private var reviews: MutableList<Reviews>) : RecyclerView.Adapter<ReviewsAdapter.ViewHolder>()  {


    private val db = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser!!.email!!
    private val chefOrUserI = FirebaseAuth.getInstance().currentUser!!.displayName

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("MM-dd-yyyy")

    @SuppressLint("SimpleDateFormat")
    private val sdfMS = SimpleDateFormat("MM-dd-yyyy hh:mm:ss.SSS a")

    private val replies : MutableList<Replies> = arrayListOf()
    private lateinit var repliesAdapter: RepliesAdapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ReviewPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reviews[position])
        val review = reviews[position]


        holder.userImage.setOnClickListener {

            Log.d(TAG, "onBindViewHolder: ${review.user}")
            val intent = Intent(context, ProfileAsUser::class.java)
            intent.putExtra("chef_or_user", "User")
            intent.putExtra("user" , review.userImageId)
            context.startActivity(intent)
        }

            holder.likeButton.setOnClickListener {
                holder.likeButton.isSelected = !holder.likeButton.isSelected
                if (holder.likeButton.isSelected) {
                    holder.likeImage.setImageResource(R.drawable.heart_filled)
                    holder.likeText.text = "${holder.likeText.text.toString().toInt() + 1}"
                    val data: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayUnion(user))
                    db.collection(review.itemType).document(review.menuItemId).collection("UserReviews").document(review.reviewItemID).update(data)
                } else {
                    holder.likeImage.setImageResource(R.drawable.heart_unfilled)
                    holder.likeText.text = "${holder.likeText.text.toString().toInt() - 1}"
                    val data: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayRemove(user))
                    db.collection(review.itemType).document(review.menuItemId).collection("UserReviews").document(review.reviewItemID).update(data)
                }
        }

    }


    override fun getItemCount() = reviews.size

    fun submitList(reviews: MutableList<Reviews>) {
        this.reviews = reviews
    }

    class ViewHolder(itemView: ReviewPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        val context = itemView.root.context!!
        val userImage = itemView.userImage
        private val reviewText = itemView.review
        val dateReplyText = itemView.dateReplyText
        val likeButton = itemView.likeButton
        val likeImage = itemView.likeImage
        val likeText = itemView.likeText
        private val recommendText = itemView.recommend
        private val userExpectationsRating = itemView.expectationsMet
        private val userQualityRating = itemView.qualityRating
        private val chefRating = itemView.chefRating


        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun bind(review: Reviews) {
            Glide.with(context).load(review.userImage).placeholder(R.drawable.default_profile).into(userImage)

            val user = FirebaseAuth.getInstance().currentUser!!.email!!


           dateReplyText.text = "${review.date.take(10)}"

            if (review.liked.contains(user)) {
                likeButton.isSelected = true
                likeImage.setImageResource(R.drawable.heart_filled)
            } else {
                likeButton.isSelected = false
                likeImage.setImageResource(R.drawable.heart_unfilled)
            }

            reviewText.text = review.userReviewTextField
            if (review.userRecommendation == 0) {
                recommendText.text = "No"
            } else {
                recommendText.text = "Yes"
            }
            userExpectationsRating.text = "${review.userExpectationsRating}"
            userQualityRating.text = "${review.userQualityRating}"
            chefRating.text = "${review.userChefRating}"

            likeText.text = "${review.liked.size}"

        }

    }


}