package com.ruh.taiste.both.adapters

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
import com.ruh.taiste.both.models.Search
import com.ruh.taiste.databinding.SearchItemBinding

private const val TAG = "SearchAdapter"
class SearchAdapter(private val context: Context, private var searchItems: MutableList<Search>) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(SearchItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(searchItems[position])
        val searchItem = searchItems[position]

        holder.userImage.setOnClickListener {
            Log.d(TAG, "onBindViewHolder: search ${searchItem.userImageId}")
            val intent = Intent(context, ProfileAsUser::class.java)
            intent.putExtra("chef_or_user", searchItem.chefOrUser)
            intent.putExtra("user" , searchItem.userImageId)
            context.startActivity(intent)
        }

    }

    override fun getItemCount() = searchItems.size

    fun submitList(searchItems: MutableList<Search>) {
        this.searchItems = searchItems
    }

    class ViewHolder(itemView: SearchItemBinding) : RecyclerView.ViewHolder(itemView.root) {

        val context = itemView.root.context!!
        val userImage = itemView.userImage
        val userName = itemView.userName
        val userFullName = itemView.userFullName


        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun bind(searchItem: Search) {
            val storage = Firebase.storage
            val x = if (searchItem.chefOrUser == "Chef") { "chefs" } else { "users" }
            storage.reference.child("$x/${searchItem.userEmail}/profileImage/${searchItem.userImageId}.png").downloadUrl.addOnSuccessListener { imageUri ->
                Glide.with(context).load(imageUri).placeholder(R.drawable.default_profile).into(userImage)
            }

            userName.text = "@${searchItem.userName}"
            userFullName.text = searchItem.userFullName


        }

    }


}

