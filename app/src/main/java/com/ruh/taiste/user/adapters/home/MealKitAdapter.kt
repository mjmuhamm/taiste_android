package com.ruh.taiste.user.adapters.home

import android.annotation.SuppressLint
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
import com.ruh.taiste.databinding.MenuFeedPostBinding
import com.ruh.taiste.user.OrderDetails
import com.ruh.taiste.user.models.FeedMenuItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ruh.taiste.both.guserName
import java.text.SimpleDateFormat
import java.util.*

class MealKitAdapter(private val context: Context, private var mealKitItems: MutableList<FeedMenuItems>) : RecyclerView.Adapter<MealKitAdapter.ViewHolder>()  {

    private val db = Firebase.firestore
    private val chefOrUser = FirebaseAuth.getInstance().currentUser!!.displayName!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(MenuFeedPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mealKitItems[position])


        val item = mealKitItems[position]

        holder.itemImage.setOnClickListener {
            val intent = Intent(context, ItemDetail::class.java)
            intent.putExtra("item", item)
            context.startActivity(intent)
        }

        holder.orderButton.setOnClickListener {
            if (item.chefUsername == "chefTest") {
                Toast.makeText(context, "This is a test profile.", Toast.LENGTH_LONG).show()
            } else {
                    val intent = Intent(context, OrderDetails::class.java)
                    intent.putExtra("item", item)
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
            holder.likeButton.isSelected = !holder.likeButton.isSelected
            if (holder.likeButton.isSelected) {
                item.liked.add(FirebaseAuth.getInstance().currentUser!!.email!!)
                holder.likeImage.setImageResource(R.drawable.heart_filled)
                holder.likeText.text = "${holder.likeText.text.toString().toInt() + 1}"
                val data1: Map<String, Any> = hashMapOf("chefEmail" to item.chefEmail, "chefImageId" to item.chefImageId, "imageCount" to item.imageCount, "itemDescription" to item.itemDescription, "liked" to item.liked, "itemOrders" to item.itemOrders, "itemRating" to item.itemRating, "itemPrice" to item.itemPrice, "itemTitle" to item.itemTitle, "itemType" to item.itemType, "itemCalories" to item.itemCalories, "expectations" to 0, "chefRating" to 0, "quality" to 0, "city" to item.city, "state" to item.state, "chefPassion" to item.chefPassion)
                db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("UserLikes").document(item.menuItemId).set(data1)
                val data: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayUnion(FirebaseAuth.getInstance().currentUser!!.email!!))
                db.collection(item.itemType).document(item.menuItemId).update(data)
                val sdfT = SimpleDateFormat("MM-dd-yyyy hh:mm a")
                val date1 = sdfT.format(Date())
                val data3: Map<String, Any> = hashMapOf("notification" to "$guserName has just liked your item (${item.itemType})  ${item.itemTitle}", "date" to date1)
                val data4: Map<String, Any> = hashMapOf("notifications" to "yes")
                db.collection("Chef").document(item.chefImageId).collection("Notifications").document().set(data3)
                db.collection("Chef").document(item.chefImageId).update(data4)
            } else {
                val index = item.liked.indexOfFirst { it == FirebaseAuth.getInstance().currentUser!!.email!! }
                if (index != -1) {
                    item.liked.removeAt(index)
                }
                holder.likeImage.setImageResource(R.drawable.heart_unfilled)
                holder.likeText.text = "${holder.likeText.text.toString().toInt() - 1}"
                db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.email!!).collection("UserLikes").document(item.menuItemId).delete()
                val data: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayRemove(FirebaseAuth.getInstance().currentUser!!.email!!))
                db.collection(item.itemType).document(item.menuItemId).update(data)
            }
            val docRef = db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.email!!).collection("UserChefs").document(item.chefEmail)
            docRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data
                    val timesLiked = data?.get("timesLiked") as Number
                    if (holder.likeButton.isSelected) {
                        val data2: Map<String, Any> =
                            hashMapOf("timesLiked" to timesLiked.toInt() + 1)
                        docRef.update(data2)
                    } else {
                        if (timesLiked.toInt() == 1) {
                            docRef.delete()
                        } else {
                            val data2: Map<String, Any> = hashMapOf("timesLiked" to timesLiked.toInt() - 1)
                            docRef.update(data2)
                        }
                    }
                } else {
                    val data2: Map<String, Any> = hashMapOf("timesLiked" to 1, "chefEmail" to item.chefEmail, "chefImageId" to item.chefImageId, "chefPassion" to item.chefPassion)
                    docRef.set(data2)
                }
            }
        }

    }

    override fun getItemCount() = mealKitItems.size

    fun submitList(mealKitItems: MutableList<FeedMenuItems>) {
        this.mealKitItems = mealKitItems
    }

    class ViewHolder(itemView: MenuFeedPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        private val context = itemView.root.context
        val chefImage = itemView.chefImage
        private val itemTitle = itemView.itemTitle
        val itemImage = itemView.itemImage
        private val itemDescription = itemView.itemDescription
        val likeButton = itemView.likeButton
        val likeImage = itemView.likeImage
        val likeText = itemView.likeText
        private val orderText = itemView.orderText
        private val ratingText = itemView.ratingText
        private val itemPrice = itemView.itemPrice
        val orderButton = itemView.orderButton

        fun bind(item: FeedMenuItems) {
            val storage = Firebase.storage
            val chefRef = storage.reference
            val itemRef = storage.reference
            chefRef.child("chefs/${item.chefEmail}/MealKit Items/${item.menuItemId}0.png").downloadUrl.addOnSuccessListener { itemUri ->
                Glide.with(context).load(itemUri).placeholder(R.drawable.default_image).into(itemImage)
            }
            itemRef.child("chefs/${item.chefEmail}/profileImage/${item.chefImageId}.png").downloadUrl.addOnSuccessListener { chefUri ->
                Glide.with(context).load(chefUri).placeholder(R.drawable.default_profile).into(chefImage)
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