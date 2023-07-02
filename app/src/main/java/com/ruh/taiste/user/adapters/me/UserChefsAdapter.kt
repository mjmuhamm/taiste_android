package com.ruh.taiste.user.adapters.me

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ruh.taiste.R
import com.ruh.taiste.both.ProfileAsUser
import com.ruh.taiste.databinding.UserChefsPostBinding
import com.ruh.taiste.user.models.UserChefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class UserChefsAdapter(private val context: Context, private var userChefs: MutableList<UserChefs>) : RecyclerView.Adapter<UserChefsAdapter.ViewHolder>()  {

    private val db = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(UserChefsPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userChefs[position])
        val item = userChefs[position]

        holder.chefImage.setOnClickListener {
            val intent = Intent(context, ProfileAsUser::class.java)
            intent.putExtra("chef_or_user", "chef")
            intent.putExtra("user" , item.chefImageId)
            context.startActivity(intent)
        }

//        holder.likeButton.setOnClickListener {
//            db.collection("User").document(user).collection("UserChefs").document(item.chefEmail).delete()
//            userChefs.removeAt(position)
//            notifyItemRemoved(position)
//        }



    }

    override fun getItemCount() = userChefs.size

    fun submitList(userChefs: MutableList<UserChefs>) {
        this.userChefs = userChefs
    }

    class ViewHolder(itemView: UserChefsPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        private val context = itemView.root.context
        val chefImage = itemView.chefImage
        val likeButton = itemView.likeButton
        val likeText = itemView.likeText
        private val orderText = itemView.orderText
        private val ratingText = itemView.ratingText

        fun bind(item: UserChefs) {

            val storage = Firebase.storage
            val chefRef = storage.reference

            chefRef.child("chefs/${item.chefEmail}/profileImage/${item.chefImageId}.png").downloadUrl.addOnSuccessListener { uri ->
                Glide.with(context).load(uri).placeholder(R.drawable.default_image).into(chefImage)
            }


            likeText.text = "${item.chefLiked.size}"
            orderText.text = "${item.chefOrders}"
            var num = 0.0
            for (i in 0 until item.chefRating.size) {
                num += item.chefRating[i].toDouble()
                if (i == item.chefRating.size - 1) {
                    num = (num / item.chefRating.size)
                }
            }
            ratingText.text = "$num"



        }

    }
}