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
    private var mGroupFragmentView: View? = null
    private var mRecyclerView: RecyclerView? = null
    private var mGroupAdapter: GroupAdapter? = null
    private val mGroupList =
        ArrayList<String>()
    private var mUserGroupsRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var mCurrentUserID: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mGroupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false)
        mAuth = FirebaseAuth.getInstance()
        mCurrentUserID = mAuth!!.currentUser!!.uid
        mUserGroupsRef = FirebaseDatabase.getInstance().reference
            .child(Constants.NODES.USER_NODE).child(mCurrentUserID!!)
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
        return mGroupFragmentView
    }

    private fun retrieveAndDisplayGroups() {
        mUserGroupsRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val set: MutableSet<String> =
                    HashSet()
                val iterator: Iterator<*> = dataSnapshot.children.iterator()
                while (iterator.hasNext()) {
                    (iterator.next() as DataSnapshot).key?.let { set.add(it) }
                }
                mGroupList.clear()
                mGroupList.addAll(set)
                mGroupAdapter!!.notifyDataSetChanged()
                //                mGroupAdapter = new GroupAdapter(mGroupList);
//                mRecyclerView.setAdapter(mGroupAdapter);
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun initializeFields() {
        mRecyclerView =
            mGroupFragmentView!!.findViewById<View>(R.id.list_view) as RecyclerView
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.VERTICAL
        mRecyclerView!!.layoutManager = layoutManager
        mGroupAdapter = GroupAdapter(mGroupList)
        mRecyclerView!!.adapter = mGroupAdapter
        val mDividerItemDecoration = DividerItemDecoration(
            mRecyclerView!!.context,
            layoutManager.orientation
        )
        mRecyclerView!!.addItemDecoration(mDividerItemDecoration)
        mGroupAdapter!!.setOnItemClickListener(object : OnGroupItemClick {
            override fun onItemClicked(position: Int, groupId: String) {
                val intent = Intent(context, GroupChatActivity::class.java)
                intent.putExtra(Constants.KEY.GROUP_ID, groupId)
                activity!!.startActivity(intent)
            }
        })
    }
}