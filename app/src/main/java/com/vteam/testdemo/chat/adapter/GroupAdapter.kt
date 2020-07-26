package com.vteam.testdemo.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.vteam.testdemo.R
import com.vteam.testdemo.chat.model.GroupDetails
import com.vteam.testdemo.common.Constants
import com.vteam.testdemo.databinding.GroupItemLayoutBinding
import com.vteam.testdemo.group.OnGroupItemClick

class GroupAdapter(private val userSelectedList: List<String>) :
    RecyclerView.Adapter<GroupAdapter.GroupItemViewHolder>() {

    private var onGroupItemClick : OnGroupItemClick? = null
    private var mCurrentUserID: String?= null

    private lateinit var mAuth: FirebaseAuth
    private var mUserGroupsRef: DatabaseReference? = null




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupItemViewHolder {
        var binding: GroupItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.group_item_layout,
            parent,
            false
        )

        mAuth = FirebaseAuth.getInstance()
        mCurrentUserID =mAuth.currentUser?.uid
        mUserGroupsRef = mCurrentUserID?.let {
            FirebaseDatabase.getInstance().reference
                .child(Constants.NODES.GROUP_DETAILS)
        }

        return GroupItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userSelectedList.size
    }

    override fun onBindViewHolder(holder: GroupItemViewHolder, position: Int) {
        val groupId: String = userSelectedList[position]

        mUserGroupsRef?.child(groupId)?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val groupDetails: GroupDetails? = dataSnapshot.getValue(GroupDetails::class.java)
//                val set: MutableSet<String?> =
//                    HashSet()
//                val iterator: MutableIterator<DataSnapshot> = dataSnapshot.children.iterator()
//                while (iterator.hasNext()) {
//                    set.add((iterator.next()).key)
//                }
                holder.binding.groupName.text = groupDetails?.name
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
        holder.binding.root.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                onGroupItemClick?.onItemClicked(position,groupId)
            }
        })

//
//        if (user.image != null && !user.image!!.isEmpty()) {
//            val reference =
//                FirebaseStorage.getInstance().reference
//            reference.child(user.image!!).downloadUrl
//                .addOnSuccessListener { uri -> // Got the download URL for 'users/me/profile.png'
//                    Log.d("URL", "" + uri)
//                    Glide.with(holder.binding.usersProfileImage.context)
//                        .load(uri)
//                        .placeholder(R.drawable.ic_profile)
//                        .error(R.drawable.ic_profile)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .into(holder.binding.usersProfileImage)
//
//                }.addOnFailureListener {
//                    // Handle any errors
//                }
//        }
    }


    fun  setOnItemClickListener(onGroupItemClick: OnGroupItemClick){
        this.onGroupItemClick = onGroupItemClick

    }

    inner class GroupItemViewHolder(val binding: GroupItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

}