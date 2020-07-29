package com.vteam.testdemo.chat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.vteam.testdemo.R
import com.vteam.testdemo.databinding.SelectedUserItemLayoutBinding
import com.vteam.testdemo.landing.model.Users

class SelectedUserAdapter(private val userSelectedList: List<Users>) :
    RecyclerView.Adapter<SelectedUserAdapter.SelectedUserViewHolder>() {


    private lateinit var auth: FirebaseAuth

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedUserViewHolder {
        var binding: SelectedUserItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.selected_user_item_layout,
            parent,
            false
        )

        auth = FirebaseAuth.getInstance()
        return SelectedUserViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userSelectedList.size
    }

    override fun onBindViewHolder(holder: SelectedUserViewHolder, position: Int) {
        val user: Users = userSelectedList[position]


        if (user.image != null && !user.image!!.isEmpty()) {
            val reference =
                FirebaseStorage.getInstance().reference
            reference.child(user.image!!).downloadUrl
                .addOnSuccessListener { uri -> // Got the download URL for 'users/me/profile.png'
                    Log.d("URL", "" + uri)
                    Glide.with(holder.binding.usersProfileImage.context)
                        .load(uri)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.binding.usersProfileImage)

                }.addOnFailureListener {
                    // Handle any errors
                }
        }
        holder.binding.userProfileName.text = user.name
    }

    inner class SelectedUserViewHolder(val binding: SelectedUserItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

}