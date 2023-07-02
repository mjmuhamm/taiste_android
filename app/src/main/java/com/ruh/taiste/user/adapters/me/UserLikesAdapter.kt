package com.ruh.taiste.user.adapters.me

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ruh.taiste.R
import com.ruh.taiste.both.ItemDetail
import com.ruh.taiste.both.ProfileAsUser
import com.ruh.taiste.databinding.UserOrdersAndLikesPostBinding
import com.ruh.taiste.user.OrderDetails
import com.ruh.taiste.user.models.UserLikes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.user.models.FeedMenuItems

class UserLikesAdapter(private val context: Context, private var userLikes: MutableList<UserLikes>) : RecyclerView.Adapter<UserLikesAdapter.ViewHolder>()  {

    private val db = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(UserOrdersAndLikesPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userLikes[position])
        val item = userLikes[position]


        holder.itemImage.setOnClickListener {
            val intent = Intent(context, ItemDetail::class.java)
            intent.putExtra("like_item", item)
            context.startActivity(intent)
        }

        holder.orderButton.setOnClickListener {
            if (item.chefUsername == "chefTest") {
                Toast.makeText(context, "This is a test account.", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(context, OrderDetails::class.java)
                val x = FeedMenuItems(
                    item.chefEmail,
                    "",
                    "",
                    item.chefImageId,
                    item.chefImage,
                    item.documentId,
                    item.itemImage,
                    item.itemTitle,
                    item.itemDescription,
                    item.itemPrice,
                    item.liked,
                    item.itemOrders,
                    item.itemRating,
                    "",
                    item.imageCount,
                    "${item.itemCalories.toInt()}",
                    item.itemType,
                    item.city,
                    item.state,
                    FirebaseAuth.getInstance().currentUser!!.email!!,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
                )
                intent.putExtra("item", x)
                context.startActivity(intent)
            }
        }

        holder.chefImage.setOnClickListener {
            val intent = Intent(context, ProfileAsUser::class.java)
            intent.putExtra("chef_or_user", "chef")
            intent.putExtra("user" , item.chefImageId)
            context.startActivity(intent)
        }

        holder.likeButton.setOnClickListener {
            if (userLikes[position].user == FirebaseAuth.getInstance().currentUser!!.uid) {
            val data: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayRemove(user))
            db.collection(item.itemType).document(item.documentId).update(data)
            db.collection("User").document(user).collection("UserLikes").document(item.documentId).delete()
            userLikes.removeAt(position)
            notifyItemRemoved(position)

            }
        }



    }

    override fun getItemCount() = userLikes.size

    fun submitList(userLikes: MutableList<UserLikes>) {
        this.userLikes = userLikes
    }

    class ViewHolder(itemView: UserOrdersAndLikesPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        private val context = itemView.root.context
        val chefImage = itemView.chefImage
        private val itemTitle = itemView.itemTitle
        val itemImage = itemView.itemImage
        private val itemDescription = itemView.itemDescription
        val likeButton = itemView.likeButton
        val likeText = itemView.likeText
        val likeImage = itemView.likeImage
        private val orderText = itemView.orderText
        private val ratingText = itemView.ratingText
        private val itemPrice = itemView.itemPrice
        val orderButton = itemView.orderButton

        fun bind(item: UserLikes) {

            val storage = Firebase.storage
            val chefRef = storage.reference
            val itemRef = storage.reference

            chefRef.child("chefs/${item.chefEmail}/${item.itemType}/${item.documentId}0.png").downloadUrl.addOnSuccessListener { itemUri ->
                Glide.with(context).load(itemUri).placeholder(R.drawable.default_image).into(itemImage)
            }
            itemRef.child("chefs/${item.chefEmail}/profileImage/${item.chefImageId}.png").downloadUrl.addOnSuccessListener { uri ->
                Glide.with(context).load(uri).placeholder(R.drawable.default_profile).into(chefImage)
            }


            itemTitle.text = item.itemTitle
            itemDescription.text = item.itemDescription
            itemPrice.text = item.itemPrice
            likeText.text = "${item.liked.size}"
            orderText.text = "${item.itemOrders}"
            var num = 0.0
            for (i in 0 until item.itemRating.size) {
                num += item.itemRating[i].toDouble()
                if (i == item.itemRating.size - 1) {
                    num = (num / item.itemRating.size)
                }
            }
            ratingText.text = "$num"
            likeImage.setImageResource(R.drawable.heart_filled)



        }

    }
}