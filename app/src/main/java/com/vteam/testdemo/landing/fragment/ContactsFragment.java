package com.vteam.testdemo.landing.fragment;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vteam.testdemo.R;
import com.vteam.testdemo.chat.ChatActivity;
import com.vteam.testdemo.common.Constants;
import com.vteam.testdemo.landing.model.Users;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.vteam.testdemo.R.color.color_000000;


public class ContactsFragment extends Fragment {
    private View mContactsView;
    private RecyclerView mContactsList;

    private DatabaseReference  mUsersRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;
    private String senderUserID;
    private FirebaseRecyclerAdapter<Users, ContactsViewHolder> adapter;
    private Query query;


    public ContactsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);
        mContactsList = (RecyclerView) mContactsView.findViewById(R.id.contact_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mContactsList.setLayoutManager(layoutManager);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(mContactsList.getContext(),
                layoutManager.getOrientation());
        mContactsList.addItemDecoration(mDividerItemDecoration);
        mAuth = FirebaseAuth.getInstance();
        mUsersRef = FirebaseDatabase.getInstance().getReference().child(Constants.NODES.USER_NODE);
        query = mUsersRef.limitToLast(50);
        return mContactsView;
    }


    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(query, Users.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Users, ContactsViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, final int position, @NonNull Users model) {
               final String visitorUserId = getRef(position).getKey();
               final String name = model.getName();
               final String profileImage = model.getImage();
                holder.userName.setText(name);
                holder.userStatus.setText(model.getStatus());
                if(model.getUserStatus()!=null && model.getUserStatus().getStatus()!=null ){
                    if ( model.getUserStatus().getStatus().equals(Constants.USER_STATE.OFFLINE)){
                        holder.onlineIcon.setBackground(getContext().getDrawable(R.drawable.live_icon_grey));
                    }else if (model.getUserStatus().getStatus().equals(Constants.USER_STATE.ONLINE)){
                        holder.onlineIcon.setBackground(getContext().getDrawable(R.drawable.live_icon_green));
                    }
                    holder.lastSeenTime.setText(model.getUserStatus().getTime());
                }


                if (profileImage!=null && !profileImage.isEmpty()) {
                    StorageReference reference = FirebaseStorage.getInstance().getReference();

                    reference.child(profileImage).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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


                if(!TextUtils.isEmpty(profileImage)) {
                    Glide.with(getActivity())
                            .load(profileImage)
                            .into(holder.profileImage);
                }


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SendChatRequest(visitorUserId,name,profileImage);
                    }
                });


            }

            @NonNull
            @Override
            public ContactsViewHolder  onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_item_layout, viewGroup, false);
                return new ContactsViewHolder (view);
            }
        };
        mContactsList.setAdapter(adapter);
        adapter.startListening();
    }




    private void SendChatRequest(final String receiverUserID,final String refName,final String imageUrl) {

        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
        chatIntent.putExtra("visit_user_id", receiverUserID);
        chatIntent.putExtra("visit_user_name", refName);
        chatIntent.putExtra("visit_image", imageUrl);
        startActivity(chatIntent);

    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        final TextView userName;
        final TextView lastSeenTime;
        final CircleImageView profileImage;
        final TextView userStatus;
        final ImageView onlineIcon;


        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            lastSeenTime = itemView.findViewById(R.id.text_last_message_date);
            userStatus = itemView.findViewById(R.id.text_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            onlineIcon = (ImageView) itemView.findViewById(R.id.users_status_icon);
        }
    }
}
