package com.vteam.testdemo.landing.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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
import com.vteam.testdemo.common.Constants;
import com.vteam.testdemo.top.GroupChatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class GroupsFragment extends Fragment {
    private View mGroupFragmentView;
    private RecyclerView mRecyclerView;
    private GroupAdapter mArrayAdapter;
    private ArrayList<String> mListOfGroups = new ArrayList<>();
    private DatabaseReference mGroupRef;
    private DatabaseReference mUserGroupsRef;
    private FirebaseAuth auth;
    private String currentUserID;


    public GroupsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mGroupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();

        mGroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        mUserGroupsRef = FirebaseDatabase.getInstance().getReference().child(Constants.NODES.USER_NODE).child(currentUserID).child(Constants.CHILD_NODES.GROUPS);

        InitializeFields();

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

                mListOfGroups.clear();
                mListOfGroups.addAll(set);
                mArrayAdapter = new GroupAdapter(mListOfGroups);
                mRecyclerView.setAdapter(mArrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields() {
        mRecyclerView = (RecyclerView) mGroupFragmentView.findViewById(R.id.list_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mArrayAdapter = new GroupAdapter(mListOfGroups);
        mRecyclerView.setAdapter(mArrayAdapter);
    }

}
