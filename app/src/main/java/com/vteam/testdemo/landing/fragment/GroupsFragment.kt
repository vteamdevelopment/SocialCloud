package com.vteam.testdemo.landing.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.vteam.testdemo.R
import com.vteam.testdemo.chat.adapter.GroupAdapter
import com.vteam.testdemo.common.Constants
import com.vteam.testdemo.group.OnGroupItemClick
import com.vteam.testdemo.top.GroupChatActivity
import java.util.*

class GroupsFragment : Fragment() {
    private var groupView: View? = null
    private var recyclerView: RecyclerView? = null
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
        groupView = inflater.inflate(R.layout.fragment_groups, container, false)
        auth = FirebaseAuth.getInstance()
        currentUserID = auth!!.currentUser!!.uid
        userGroupsRef = FirebaseDatabase.getInstance().reference
            .child(Constants.NODES.USER_NODE).child(currentUserID!!)
            .child(Constants.CHILD_NODES.GROUPS)
        initializeFields()
        retrieveAndDisplayGroups()

//        mRecyclerView.setOnClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//                String currentGroupName = adapterView.getItemAtPosition(position).toString();
//                Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
//                groupChatIntent.putExtra("groupName", currentGroupName);
//                startActivity(groupChatIntent);
//            }
//        });
        return groupView
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
                //                mGroupAdapter = new GroupAdapter(mGroupList);
//                mRecyclerView.setAdapter(mGroupAdapter);
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun initializeFields() {
        recyclerView =
            groupView!!.findViewById<View>(R.id.list_view) as RecyclerView
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.VERTICAL
        recyclerView!!.layoutManager = layoutManager
        groupAdapter = GroupAdapter(groupList)
        recyclerView!!.adapter = groupAdapter
        val mDividerItemDecoration = DividerItemDecoration(
            recyclerView!!.context,
            layoutManager.orientation
        )
        recyclerView!!.addItemDecoration(mDividerItemDecoration)
        groupAdapter!!.setOnItemClickListener(object : OnGroupItemClick {
            override fun onItemClicked(position: Int, groupId: String) {
                val intent = Intent(context, GroupChatActivity::class.java)
                intent.putExtra(Constants.KEY.GROUP_ID, groupId)
                activity!!.startActivity(intent)
            }
        })
    }
}