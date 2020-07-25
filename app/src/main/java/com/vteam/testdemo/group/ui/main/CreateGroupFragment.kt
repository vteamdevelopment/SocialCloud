package com.vteam.testdemo.group.ui.main

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.storage.FirebaseStorage
import com.vteam.testdemo.R
import com.vteam.testdemo.common.Constants
import com.vteam.testdemo.databinding.CreateGroupFragmentBinding
import com.vteam.testdemo.databinding.CreateGroupItemLayoutBinding
import com.vteam.testdemo.landing.model.Users

class CreateGroupFragment : Fragment() {

    companion object {
        fun newInstance() = CreateGroupFragment()
    }

    private lateinit var adapter: FirebaseRecyclerAdapter<Users, ContactsViewHolder>
    private lateinit var mQuery: Query
    private lateinit var mUsersRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    lateinit var binding: CreateGroupFragmentBinding
    private lateinit var viewModel: CreateGroupViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.create_group_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        binding.toolbar.title = getString(R.string.create_group)

        val layoutManager = LinearLayoutManager(context)

        viewModel = ViewModelProvider(this).get(CreateGroupViewModel::class.java)
        binding.groupList.layoutManager = layoutManager
        val mDividerItemDecoration = DividerItemDecoration(
            context,
            layoutManager.orientation
        )
        binding.groupList.addItemDecoration(mDividerItemDecoration)
        mAuth = FirebaseAuth.getInstance()
        mUsersRef = FirebaseDatabase.getInstance().reference
            .child(Constants.NODES.USER_NODE)
        mQuery = mUsersRef.limitToLast(50)


    }


    override fun onStart() {
        super.onStart()

        val options = FirebaseRecyclerOptions.Builder<Users>()
            .setQuery(mQuery, Users::class.java)
            .build()

        adapter =
            object : FirebaseRecyclerAdapter<Users, ContactsViewHolder>(options) {
                override fun onBindViewHolder(
                    holder: ContactsViewHolder,
                    position: Int,
                    model: Users
                ) {
                    val visitorUserId = getRef(position).key
                    val name = model.name
                    val profileImage = model.image
                    holder.binding.userProfileName.text = name
                    holder.binding.textStatus.text = model.status
                    if (model.userStatus != null && model.userStatus!!.status != null) {
                        if (model.userStatus!!.status == Constants.USER_STATE.OFFLINE) {
                            holder.binding.usersStatusIcon.background =
                                context!!.getDrawable(R.drawable.live_icon_grey)
                        } else if (model.userStatus!!.status == Constants.USER_STATE.ONLINE) {
                            holder.binding.usersStatusIcon.background =
                                context!!.getDrawable(R.drawable.live_icon_green)
                        }
                        holder.binding.textLastMessageDate.text = model.userStatus!!.time
                    }
                    if (profileImage != null && !profileImage.isEmpty()) {
                        val reference =
                            FirebaseStorage.getInstance().reference
                        reference.child(profileImage).downloadUrl
                            .addOnSuccessListener { uri -> // Got the download URL for 'users/me/profile.png'
                                Log.d("URL", "" + uri)
                                val activity: Activity? = activity
                                if (activity != null) {
                                    Glide.with(activity)
                                        .load(uri)
                                        .into(holder.binding.usersProfileImage)
                                }
                            }.addOnFailureListener {
                                // Handle any errors
                            }
                    }
                    if (!TextUtils.isEmpty(profileImage)) {
                        Glide.with(activity!!)
                            .load(profileImage)
                            .into(holder.binding.usersProfileImage)
                    }
                    holder.itemView.setOnClickListener {
//                        SendChatRequest(
//                            visitorUserId,
//                            name,
//                            profileImage
//                        )
                    }
                }

                override fun onCreateViewHolder(
                    viewGroup: ViewGroup,
                    i: Int
                ): ContactsViewHolder {
                    val binding: CreateGroupItemLayoutBinding = DataBindingUtil.inflate(
                        LayoutInflater.from(viewGroup.context),
                        R.layout.create_group_item_layout,
                        viewGroup,
                        false
                    )
                    return ContactsViewHolder(binding)
                }
            }
        binding.groupList.adapter = adapter
        adapter.startListening()

    }


    override fun onStop() {
        super.onStop()
    }


    class ContactsViewHolder(val binding: CreateGroupItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }
}