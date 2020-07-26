package com.vteam.testdemo.landing.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vteam.testdemo.R;
import com.vteam.testdemo.chat.adapter.GroupAdapter;
import com.vteam.testdemo.chat.model.GroupDetails;
import com.vteam.testdemo.common.Constants;
import com.vteam.testdemo.group.OnGroupItemClick;
import com.vteam.testdemo.top.GroupChatActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class GroupsFragment extends Fragment {
    private View mGroupFragmentView;
    private RecyclerView mRecyclerView;
    private GroupAdapter mGroupAdapter;
    private ArrayList<String> mGroupList = new ArrayList<>();
    private DatabaseReference mUserGroupsRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;


    public GroupsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mGroupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();

        mUserGroupsRef = FirebaseDatabase.getInstance().getReference().child(Constants.NODES.USER_NODE).child(mCurrentUserID).child(Constants.CHILD_NODES.GROUPS);

        initializeFields();

        retrieveAndDisplayGroups();

//        mRecyclerView.setOnClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//                String currentGroupName = adapterView.getItemAtPosition(position).toString();
//                Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
//                groupChatIntent.putExtra("groupName", currentGroupName);
//                startActivity(groupChatIntent);
//            }
//        });

        return mGroupFragmentView;
    }


    private void retrieveAndDisplayGroups() {
        mUserGroupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    set.add(((DataSnapshot) iterator.next()).getKey());
                }
                mGroupList.clear();
                mGroupList.addAll(set);
                mGroupAdapter.notifyDataSetChanged();
//                mGroupAdapter = new GroupAdapter(mGroupList);
//                mRecyclerView.setAdapter(mGroupAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeFields() {
        mRecyclerView = (RecyclerView) mGroupFragmentView.findViewById(R.id.list_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mGroupAdapter = new GroupAdapter(mGroupList);

        mRecyclerView.setAdapter(mGroupAdapter);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(mDividerItemDecoration);

        mGroupAdapter.setOnItemClickListener(new OnGroupItemClick() {
            @Override
            public void onItemClicked(int position, @NotNull String groupId) {
                Intent intent = new Intent(getContext(), GroupChatActivity.class);
                intent.putExtra(Constants.KEY.GROUP_ID,groupId);
                getActivity().startActivity(intent);
            }
        });

    }

}
