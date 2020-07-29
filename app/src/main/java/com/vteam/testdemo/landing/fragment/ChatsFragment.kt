package com.vteam.testdemo.landing.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.vteam.testdemo.R
import com.vteam.testdemo.chat.ChatActivity
import com.vteam.testdemo.common.Constants
import com.vteam.testdemo.common.Constants.NODES.CHAT_NODE
import com.vteam.testdemo.databinding.ChatItemLayoutBinding
import com.vteam.testdemo.databinding.FragmentChatsBinding
import com.vteam.testdemo.landing.model.ChatModel
import de.hdodenhof.circleimageview.CircleImageView

class ChatsFragment : Fragment() {
    private  lateinit var binding: FragmentChatsBinding
//    private var chatsView: View? = null
//    private var chatsList: RecyclerView? = null
    private var chatsRef: DatabaseReference? = null
    private var usersRef: DatabaseReference? = null
    private var auth: FirebaseAuth? = null
    private var currentUserID = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_chats,container,false)
//        chatsView = inflater.inflate(R.layout.fragment_chats, container, false)
        auth = FirebaseAuth.getInstance()
        currentUserID = auth!!.currentUser!!.uid
        chatsRef =
            FirebaseDatabase.getInstance().reference.child(CHAT_NODE).child(currentUserID)
        usersRef = FirebaseDatabase.getInstance().reference
            .child(Constants.NODES.USER_NODE)
//        chatsList = chatsView?.findViewById<View>(R.id.chats_list) as RecyclerView
        val layoutManager = LinearLayoutManager(context)
        binding.chatsList.layoutManager = layoutManager
        val mDividerItemDecoration = DividerItemDecoration(
            binding.chatsList.context,
            layoutManager.orientation
        )
        binding.chatsList.addItemDecoration(mDividerItemDecoration)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val options =
            FirebaseRecyclerOptions.Builder<ChatModel>()
                .setQuery(chatsRef!!, ChatModel::class.java)
                .build()
        val adapter: FirebaseRecyclerAdapter<ChatModel, ChatsViewHolder> =
            object : FirebaseRecyclerAdapter<ChatModel, ChatsViewHolder>(options) {
                override fun onBindViewHolder(
                    holder: ChatsViewHolder,
                    position: Int,
                    model: ChatModel
                ) {
                    val usersIDs = getRef(position).key
                    val retImage =
                        arrayOf("default_image")
                    val lastMessage = model.lastMessage
                    val lastSeenTime = model.time
                    Log.d("Vikash", "Last message $lastMessage")
                    holder.binding.textLastMessageDate.text = lastMessage
                    holder.binding.textLastMessageDate.text = lastSeenTime
                    usersRef!!.child(usersIDs!!).addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                if (dataSnapshot.hasChild("image")) {
                                    retImage[0] =
                                        dataSnapshot.child("image").value.toString()
                                    val reference =
                                        FirebaseStorage.getInstance().reference
                                    if (retImage[0].isNotEmpty()) {
                                        reference.child(retImage[0]).downloadUrl
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
                                }
                                val retName =
                                    dataSnapshot.child("name").value.toString()
                                val retStatus =
                                    dataSnapshot.child("status").value.toString()
                                var userStatus = ""


                                if (dataSnapshot
                                        .child("userStatus")
                                        .child("status").exists()) {
                                    userStatus =
                                        dataSnapshot.child("userStatus").child("status").value
                                            .toString()
                                }
                                //                                    Log.d("Vikash", "Last message " + lastMessage);
                                holder.binding.userProfileName.text = retName
                                if (userStatus != null) {
                                    if (userStatus == Constants.USER_STATE.OFFLINE) {
                                        holder.binding.usersStatusIcon.background =
                                            context!!.getDrawable(R.drawable.live_icon_grey)
                                    } else if (userStatus == Constants.USER_STATE.ONLINE) {
                                        holder.binding.usersStatusIcon.background =
                                            context!!.getDrawable(R.drawable.live_icon_green)
                                    }
                                }
                                holder.itemView.setOnClickListener {
                                    val chatIntent =
                                        Intent(context, ChatActivity::class.java)
                                    chatIntent.putExtra("visit_user_id", usersIDs)
                                    chatIntent.putExtra("visit_user_name", retName)
                                    chatIntent.putExtra("visit_image", retImage[0])
                                    startActivity(chatIntent)
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                }

                override fun onCreateViewHolder(
                    viewGroup: ViewGroup,
                    i: Int
                ): ChatsViewHolder {

                    val binding : ChatItemLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context),R.layout.chat_item_layout,viewGroup,false)
                    return ChatsViewHolder(binding)
                }
            }
        binding.chatsList.adapter = adapter
        adapter.startListening()
    }

    class ChatsViewHolder(val binding: ChatItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}