package com.vteam.testdemo.landing.fragment;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vteam.testdemo.R;
import com.vteam.testdemo.chat.ChatActivity;
import com.vteam.testdemo.landing.model.ChatModel;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment {
    private View PrivateChatsView;
    private RecyclerView chatsList;

    private DatabaseReference ChatsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID = "";

    public ChatsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        PrivateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("ChatNode").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatsList = (RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        chatsList.setLayoutManager(layoutManager);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(chatsList.getContext(),
                layoutManager.getOrientation());
        chatsList.addItemDecoration(mDividerItemDecoration);


        return PrivateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<ChatModel> options =
                new FirebaseRecyclerOptions.Builder<ChatModel>()
                        .setQuery(ChatsRef, ChatModel.class)
                        .build();


        FirebaseRecyclerAdapter<ChatModel, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<ChatModel, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull ChatModel model) {
                        final String usersIDs = getRef(position).getKey();
                        final String[] retImage = {"default_image"};
                        final String lastMessage = model.getLastMessage();
                        final String lastSeenTime = model.getTime();
                        Log.d("Vikash", "Last message " + lastMessage);
                        holder.lastSeenMessage.setText(lastMessage);
                        holder.lastSeenTime.setText(lastSeenTime);
                        UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.hasChild("image")) {
                                        retImage[0] = dataSnapshot.child("image").getValue().toString();

                                        StorageReference reference = FirebaseStorage.getInstance().getReference();

                                        if (!retImage[0].isEmpty()) {
                                            reference.child(retImage[0]).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    // Got the download URL for 'users/me/profile.png'
                                                    Log.d("URL", "" + uri);
                                                    Activity activity = getActivity();
                                                    if (activity != null) {
                                                        Glide.with(activity)
                                                                .load(uri)
                                                                .into(holder.profileImage);
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    // Handle any errors
                                                }
                                            });
                                        }
                                    }

                                    final String retName = dataSnapshot.child("name").getValue().toString();
                                    final String retStatus = dataSnapshot.child("status").getValue().toString();
//                                    final String lastMessage = dataSnapshot.child("lastMessage").getValue().toString();
//                                    Log.d("Vikash", "Last message " + lastMessage);
                                    holder.userName.setText(retName);
//                                    holder.lastSeenMessage.setText(lastMessage);

//
//                                    if (dataSnapshot.child("userState").hasChild("state")) {
//                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
//                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
//                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();
//
////                                        if (state.equals("online")) {
////                                            holder.userStatus.setText("online");
////                                        } else if (state.equals("offline")) {
////                                            holder.userStatus.setText("Last Seen: " + date + " " + time);
////                                        }
//                                    } else {
////                                        holder.userStatus.setText("offline");
//                                    }

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", usersIDs);
                                            chatIntent.putExtra("visit_user_name", retName);
                                            chatIntent.putExtra("visit_image", retImage[0]);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        return new ChatsViewHolder(view);
                    }
                };

        chatsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userStatus, userName,lastSeenMessage,lastSeenTime;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            lastSeenMessage = itemView.findViewById(R.id.text_last_message);
            lastSeenTime = itemView.findViewById(R.id.text_last_message_date);
//            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
//            onlineIcon = (ImageView) itemView.findViewById(R.id.user_online_status);
        }
    }
}
