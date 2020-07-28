package com.vteam.testdemo.chat.adapter

import android.view.LayoutInflater
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
    private var currentUserID: String?= null

    private lateinit var auth: FirebaseAuth
    private var userGroupsRef: DatabaseReference? = null




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupItemViewHolder {
        val binding: GroupItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.group_item_layout,
            parent,
            false
        )

        auth = FirebaseAuth.getInstance()
        currentUserID =auth.currentUser?.uid
        userGroupsRef = currentUserID?.let {
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

        userGroupsRef?.child(groupId)?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val groupDetails: GroupDetails? = dataSnapshot.getValue(GroupDetails::class.java)
                holder.binding.groupName.text = groupDetails?.name
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
        holder.binding.root.setOnClickListener { onGroupItemClick?.onItemClicked(position,groupId) }


    }


    fun  setOnItemClickListener(onGroupItemClick: OnGroupItemClick){
        this.onGroupItemClick = onGroupItemClick

    }

    inner class GroupItemViewHolder(val binding: GroupItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

}