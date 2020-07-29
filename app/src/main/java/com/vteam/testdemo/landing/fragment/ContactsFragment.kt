package com.vteam.testdemo.landing.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.storage.FirebaseStorage
import com.vteam.testdemo.R
import com.vteam.testdemo.chat.ChatActivity
import com.vteam.testdemo.common.ConstantNodes
import com.vteam.testdemo.common.Constants
import com.vteam.testdemo.databinding.ChatItemLayoutBinding
import com.vteam.testdemo.databinding.ContactItemLayoutBinding
import com.vteam.testdemo.databinding.FragmentContactsBinding
import com.vteam.testdemo.landing.model.Users
import de.hdodenhof.circleimageview.CircleImageView

class ContactsFragment : Fragment() {
    private lateinit var binding: FragmentContactsBinding
//    private var contactsView: View? = null
//    private var contactsList: RecyclerView? = null
    private var usersRef: DatabaseReference? = null
    private var auth: FirebaseAuth? = null
    private val mCurrentUserID: String? = null
    private val senderUserID: String? = null
    private var adapter: FirebaseRecyclerAdapter<Users, ContactsViewHolder>? =
        null
    private var query: Query? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_contacts,container,false)

        val layoutManager = LinearLayoutManager(context)
        binding.contactList!!.layoutManager = layoutManager
        val mDividerItemDecoration = DividerItemDecoration(
            binding.contactList!!.context,
            layoutManager.orientation
        )
        binding.contactList!!.addItemDecoration(mDividerItemDecoration)
        auth = FirebaseAuth.getInstance()
        usersRef = FirebaseDatabase.getInstance().reference
            .child(ConstantNodes.NODES.USER_NODE)
        query = usersRef!!.limitToLast(50)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<Users>()
            .setQuery(query!!, Users::class.java)
            .build()
        adapter = object :
            FirebaseRecyclerAdapter<Users, ContactsViewHolder>(
                options
            ) {
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
                    val reference = FirebaseStorage.getInstance().reference
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
                    SendChatRequest(
                        visitorUserId,
                        name,
                        profileImage
                    )
                }
            }

            override fun onCreateViewHolder(
                viewGroup: ViewGroup,
                i: Int
            ): ContactsViewHolder {

                val binding : ContactItemLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context),R.layout.contact_item_layout,viewGroup,false)

                return ContactsViewHolder(binding)
            }
        }
        binding.contactList!!.adapter = adapter
        adapter?.startListening()
    }

    private fun SendChatRequest(
        receiverUserID: String?,
        refName: String?,
        imageUrl: String?
    ) {
        val chatIntent = Intent(context, ChatActivity::class.java)
        chatIntent.putExtra("visit_user_id", receiverUserID)
        chatIntent.putExtra("visit_user_name", refName)
        chatIntent.putExtra("visit_image", imageUrl)
        startActivity(chatIntent)
    }

    class ContactsViewHolder(val binding: ContactItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}