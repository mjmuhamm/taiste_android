package com.ruh.taiste.user.adapters.me

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.R
import com.ruh.taiste.both.ItemDetail
import com.ruh.taiste.both.ProfileAsUser
import com.ruh.taiste.databinding.UserOrdersAndLikesPostBinding
import com.ruh.taiste.user.OrderDetails
import com.ruh.taiste.user.models.FeedMenuItems
import com.ruh.taiste.user.models.UserOrders

private const val TAG = "UserOrdersAdapter"
class UserOrdersAdapter(private val context: Context, private var userOrders: MutableList<UserOrders>) : RecyclerView.Adapter<UserOrdersAdapter.ViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(UserOrdersAndLikesPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userOrders[position])
        val item = userOrders[position]

        Log.d(TAG, "onBindViewHolder: ${item.imageCount}")
        holder.itemImage.setOnClickListener {
            val intent = Intent(context, ItemDetail::class.java)
            intent.putExtra("order_item", item)
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
                    item.menuItemId,
                    item.itemImage,
                    item.itemTitle,
                    item.itemDescription,
                    item.itemPrice,
                    item.liked,
                    item.itemOrders,
                    item.itemRating,
                    item.orderDate,
                    item.imageCount,
                    "${item.itemCalories.toInt()}",
                    item.typeOfService,
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

        }



    }

    override fun getItemCount() = userOrders.size

    fun submitList(userOrders: MutableList<UserOrders>) {
        this.userOrders = userOrders
    }

    class ViewHolder(itemView: UserOrdersAndLikesPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        private val context = itemView.root.context
        val chefImage = itemView.chefImage
        private val itemTitle = itemView.itemTitle
        val itemImage = itemView.itemImage
        private val itemDescription = itemView.itemDescription
        val likeButton = itemView.likeButton
        val likeText = itemView.likeText
        private val orderText = itemView.orderText
        private val ratingText = itemView.ratingText
        private val itemPrice = itemView.itemPrice
        val orderButton = itemView.orderButton

        fun bind(item: UserOrders) {

            val storage = Firebase.storage
            val chefRef = storage.reference
            val itemRef = storage.reference

            chefRef.child("chefs/${item.chefEmail}/${item.typeOfService}/${item.menuItemId}0.png").downloadUrl.addOnSuccessListener { itemUri ->
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



        }

    }
}