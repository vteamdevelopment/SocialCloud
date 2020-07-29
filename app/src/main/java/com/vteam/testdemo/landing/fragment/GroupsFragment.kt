package com.vteam.testdemo.landing.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.vteam.testdemo.R
import com.vteam.testdemo.chat.adapter.GroupAdapter
import com.vteam.testdemo.common.ConstantNodes
import com.vteam.testdemo.common.Constants
import com.vteam.testdemo.databinding.FragmentGroupsBinding
import com.vteam.testdemo.group.OnGroupItemClick
import com.vteam.testdemo.top.GroupChatActivity
import java.util.*

class GroupsFragment : Fragment() {
    private lateinit var binding: FragmentGroupsBinding
    private var groupAdapter: GroupAdapter? = null
    private val groupList =
        ArrayList<String>()
    private var userGroupsRef: DatabaseReference? = null
    private var auth: FirebaseAuth? = null
    private var currentUserID: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_groups,container,false)
        auth = FirebaseAuth.getInstance()
        currentUserID = auth!!.currentUser!!.uid
        userGroupsRef = FirebaseDatabase.getInstance().reference
            .child(ConstantNodes.NODES.USER_NODE).child(currentUserID!!)
            .child(ConstantNodes.CHILD_NODES.GROUPS)
        initializeFields()
        retrieveAndDisplayGroups()
        return binding.root
    }

    private fun retrieveAndDisplayGroups() {
        userGroupsRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val set: MutableSet<String> =
                    HashSet()
                val iterator: Iterator<*> = dataSnapshot.children.iterator()
                while (iterator.hasNext()) {
                    (iterator.next() as DataSnapshot).key?.let { set.add(it) }
                }
                groupList.clear()
                groupList.addAll(set)
                groupAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun initializeFields() {

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.VERTICAL
        binding.listView.layoutManager = layoutManager
        groupAdapter = GroupAdapter(groupList)
        binding.listView.adapter = groupAdapter
        val mDividerItemDecoration = DividerItemDecoration(
            binding.listView.context,
            layoutManager.orientation
        )
        binding.listView.addItemDecoration(mDividerItemDecoration)
        groupAdapter!!.setOnItemClickListener(object : OnGroupItemClick {
            override fun onItemClicked(position: Int, groupId: String) {
                val intent = Intent(context, GroupChatActivity::class.java)
                intent.putExtra(Constants.KEY.GROUP_ID, groupId)
                activity!!.startActivity(intent)
            }
        })
    }
}