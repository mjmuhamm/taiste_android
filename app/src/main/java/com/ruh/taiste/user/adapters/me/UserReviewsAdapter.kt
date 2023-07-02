package com.ruh.taiste.user.adapters.me

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.R
import com.ruh.taiste.both.ProfileAsUser
import com.ruh.taiste.databinding.UserReviewsPostBinding
import com.ruh.taiste.user.models.UserReviews

private const val TAG = "UserReviewsAdapter"
class UserReviewsAdapter(private val context: Context, private var userReviews: MutableList<UserReviews>) : RecyclerView.Adapter<UserReviewsAdapter.ViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(UserReviewsPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userReviews[position])
        val item = userReviews[position]

        holder.chefImage.setOnClickListener {
            val intent = Intent(context, ProfileAsUser::class.java)
            intent.putExtra("chef_or_user", "chef")
            intent.putExtra("user" , item.chefImageId)
            context.startActivity(intent)
        }

        holder.likeButton.setOnClickListener {

        }



    }

    override fun getItemCount() = userReviews.size

    fun submitList(userReviews: MutableList<UserReviews>) {
        this.userReviews = userReviews
    }

    class ViewHolder(itemView: UserReviewsPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        private val context = itemView.root.context
        val chefImage = itemView.chefImage
        private val itemTitle = itemView.itemTitle
        val review = itemView.review
        private val recommend = itemView.recommend
        private val expectationsMet = itemView.expectationsMet
        private val qualityRating = itemView.qualityRating
        private val chefRating = itemView.chefRating
        val likeText = itemView.likeText
        val likeButton = itemView.likeButton
        private val reviewDate = itemView.reviewDate


        @SuppressLint("SetTextI18n")
        fun bind(item: UserReviews) {

            val storage = Firebase.storage
            val chefRef = storage.reference

            chefRef.child("chefs/${item.chefEmail}/profileImage/${item.chefImageId}.png").downloadUrl.addOnSuccessListener { uri ->
                Glide.with(context).load(uri).placeholder(R.drawable.default_image).into(chefImage)
            }

            itemTitle.text = item.itemTitle
            review.text = "Review: ${item.userReviewTextField}"
            Log.d(TAG, "bind: recommedn ${item.userRecommendation}")
            if (item.userRecommendation.toInt() == 1) { recommend.text = "Do you recommend to others?  Yes" } else { recommend.text = "Do you recommend to others?  No" }

            reviewDate.text = "${item.date.take(10)}"
            expectationsMet.text = "${item.userExpectationsRating}"
            qualityRating.text = "${item.userQualityRating}"
            chefRating.text = "${item.userChefRating}"
            likeText.text = "${item.liked.size}"





        }

    }
}