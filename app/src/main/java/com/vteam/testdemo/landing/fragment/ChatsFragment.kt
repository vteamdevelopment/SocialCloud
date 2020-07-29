package com.vteam.testdemo.landing.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.vteam.testdemo.landing.model.ChatModel
import de.hdodenhof.circleimageview.CircleImageView

class ChatsFragment : Fragment() {
    private var chatsView: View? = null
    private var chatsList: RecyclerView? = null
    private var ChatsRef: DatabaseReference? = null
    private var usersRef: DatabaseReference? = null
    private var auth: FirebaseAuth? = null
    private var currentUserID = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        chatsView = inflater.inflate(R.layout.fragment_chats, container, false)
        auth = FirebaseAuth.getInstance()
        currentUserID = auth!!.currentUser!!.uid
        ChatsRef =
            FirebaseDatabase.getInstance().reference.child(CHAT_NODE).child(currentUserID)
        usersRef = FirebaseDatabase.getInstance().reference
            .child(Constants.NODES.USER_NODE)
        chatsList = chatsView?.findViewById<View>(R.id.chats_list) as RecyclerView
        val layoutManager = LinearLayoutManager(context)
        chatsList!!.layoutManager = layoutManager
        val mDividerItemDecoration = DividerItemDecoration(
            chatsList!!.context,
            layoutManager.orientation
        )
        chatsList!!.addItemDecoration(mDividerItemDecoration)
        return chatsView
    }

    override fun onStart() {
        super.onStart()
        val options =
            FirebaseRecyclerOptions.Builder<ChatModel>()
                .setQuery(ChatsRef!!, ChatModel::class.java)
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
                    holder.lastSeenMessage.text = lastMessage
                    holder.lastSeenTime.text = lastSeenTime
                    usersRef!!.child(usersIDs!!).addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                if (dataSnapshot.hasChild("image")) {
                                    retImage[0] =
                                        dataSnapshot.child("image").value.toString()
                                    val reference =
                                        FirebaseStorage.getInstance().reference
                                    if (!retImage[0].isEmpty()) {
                                        reference.child(retImage[0]).downloadUrl
                                            .addOnSuccessListener { uri -> // Got the download URL for 'users/me/profile.png'
                                                Log.d("URL", "" + uri)
                                                val activity: Activity? = activity
                                                if (activity != null) {
                                                    Glide.with(activity)
                                                        .load(uri)
                                                        .into(holder.profileImage)
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
                                holder.userName.text = retName
                                if (userStatus != null) {
                                    if (userStatus == Constants.USER_STATE.OFFLINE) {
                                        holder.onlineIcon.background =
                                            context!!.getDrawable(R.drawable.live_icon_grey)
                                    } else if (userStatus == Constants.USER_STATE.ONLINE) {
                                        holder.onlineIcon.background =
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
                    val view = LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.chat_item_layout, viewGroup, false)
                    return ChatsViewHolder(view)
                }
            }
        chatsList!!.adapter = adapter
        adapter.startListening()
    }

    class ChatsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val onlineIcon: CircleImageView
        var profileImage: CircleImageView
        var userStatus: TextView? = null
        var userName: TextView
        var lastSeenMessage: TextView
        var lastSeenTime: TextView

        init {
            userName = itemView.findViewById(R.id.user_profile_name)
            lastSeenMessage = itemView.findViewById(R.id.text_status)
            lastSeenTime = itemView.findViewById(R.id.text_last_message_date)
            //            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image)
            onlineIcon =
                itemView.findViewById<View>(R.id.users_status_icon) as CircleImageView
        }
    }
}