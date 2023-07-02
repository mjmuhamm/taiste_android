package com.ruh.taiste.chef.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.ruh.taiste.R
import com.ruh.taiste.both.ChefContent
import com.ruh.taiste.both.models.VideoModel

private const val TAG = "ChefContentAdapter"
class ChefContentAdapter(private var context: Context, private var content: MutableList<VideoModel>, private var chefImageId: String) : BaseAdapter() {

    override fun getCount() = content.size

    override fun getItem(position: Int) = content[position]

    override fun getItemId(position: Int) = position.toLong()

    fun submitList(content: MutableList<VideoModel>, chefImageId: String) {
        this.content = content
        this.chefImageId = chefImageId
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, p1: View?, p2: ViewGroup?): View {

        val view: View = View.inflate(context, R.layout.chef_content_post, null)
        val videoView : ImageView = view.findViewById(R.id.video_view)
        val videoViews : TextView = view.findViewById(R.id.video_views)



        Glide.with(context).load(content[position].dataUri).centerCrop().into(videoView)


        videoView.setOnClickListener {
            Log.d(TAG, "getView: happening")
            var cont : ArrayList<VideoModel> = ArrayList()
            cont.add(content[position])
            for (i in 0 until content.size) {
                if (content[position].id != content[i].id) {
                    cont.add(content[i])
                }
            }
            val intent = Intent(context, ChefContent::class.java)
            intent.putExtra("chef_or_user", "chef")
            intent.putExtra("chef_image_id", chefImageId)
            intent.putExtra("content", cont)
            context.startActivity(intent)
        }

        videoViews.text = "${content[position].views}"



        return view
    }
}