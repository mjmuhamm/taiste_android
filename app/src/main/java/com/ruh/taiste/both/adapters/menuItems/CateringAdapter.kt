package com.ruh.taiste.both.adapters.menuItems

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.R
import com.ruh.taiste.both.ItemDetail
import com.ruh.taiste.chef.MenuItemAdd
import com.ruh.taiste.chef.fragments.chefLatitude
import com.ruh.taiste.chef.fragments.chefLongitude
import com.ruh.taiste.databinding.ChefItemPostBinding
import com.ruh.taiste.user.OrderDetails
import com.ruh.taiste.user.models.FeedMenuItems

private const val TAG = "CateringAdapter"
class CateringAdapter(private val context: Context, private var caterItems: MutableList<FeedMenuItems>, private var chefOrUser: String, private var city: String, private var state: String, private var zipCode: String) : RecyclerView.Adapter<CateringAdapter.ViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ChefItemPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(caterItems[position])
        val item = caterItems[position]

        if (chefOrUser == "chef") { holder.editItemButton.visibility = View.VISIBLE
        } else { holder.editItemButton.visibility = View.GONE }


        Log.d(TAG, "onBindViewHolder: ${item.imageCount}")
        holder.itemImage.setOnClickListener {
            val intent = Intent(context, ItemDetail::class.java)
            intent.putExtra("item", item)
            context.startActivity(intent)
        }


        holder.orderButton.setOnClickListener {
            if (item.chefUsername != "chefTest") {
                if (FirebaseAuth.getInstance().currentUser!!.displayName!! != "Chef") {
                    val intent = Intent(context, OrderDetails::class.java)
                    intent.putExtra("item", item)
                    context.startActivity(intent)
                } else {
                    Toast.makeText(
                        context,
                        "Please create a user account to order.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(context, "This is a test profile.", Toast.LENGTH_LONG).show()
            }
        }

        holder.editItemButton.setOnClickListener {
            val intent = Intent(context, MenuItemAdd::class.java)
            intent.putExtra("latitude" , chefLatitude)
            intent.putExtra("longitude" , chefLongitude)
            intent.putExtra("document_id" , item.menuItemId)
            intent.putExtra("new_or_edit" , "edit")
            intent.putExtra("item_label", "Cater Items")
            intent.putExtra("city", city)
            intent.putExtra("state", state)
            intent.putExtra("zipCode", zipCode)
            context.startActivity(intent)
        }

        holder.likeButton.setOnClickListener {

        }



    }

    override fun getItemCount() = caterItems.size

    fun submitList(caterItems: MutableList<FeedMenuItems>) {
        this.caterItems = caterItems
    }

    class ViewHolder(itemView: ChefItemPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        private val context = itemView.root.context
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
        val editItemButton = itemView.editItemButton

        @SuppressLint("SetTextI18n")
        fun bind(item: FeedMenuItems) {

            val storage = Firebase.storage

            storage.reference.child("chefs/${item.chefEmail}/Cater Items/${item.menuItemId}0.png").downloadUrl.addOnSuccessListener { itemUri ->
                Glide.with(context).load(itemUri).placeholder(R.drawable.default_image)
                    .into(itemImage)
            }

            itemTitle.text = item.itemTitle
            itemDescription.text = item.itemDescription
            itemPrice.text = "$${item.itemPrice}"
            likeText.text = "${item.liked.size}"
            orderText.text = "${item.itemOrders}"
            var num = 0.0
            for (i in 0 until item.itemRating.size) {
                num += item.itemRating[i].toDouble()
                if (i == item.itemRating.size - 1) {
                    num = (num / item.itemRating.size)
                }
            }
            val index = item.liked.indexOfFirst { it == FirebaseAuth.getInstance().currentUser!!.email!! }
            if (index != -1) {
                likeImage.setImageResource(R.drawable.heart_filled)
            }
            ratingText.text = "$num"



        }

    }
}