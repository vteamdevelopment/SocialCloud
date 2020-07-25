package com.vteam.testdemo.landing;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vteam.testdemo.R;
import com.vteam.testdemo.SplashActivity;
import com.vteam.testdemo.common.Constants;
import com.vteam.testdemo.group.CreateGroupActivity;
import com.vteam.testdemo.landing.model.UserStatus;
import com.vteam.testdemo.profile.ProfileUpdateActivity;
import com.vteam.testdemo.top.TabsAccessorAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import androidx.viewpager.widget.ViewPager;


public class LandingActivity extends AppCompatActivity {

    private static int PICK_CONTACT= 100;
    private Toolbar mToolbar;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;
    private boolean existingUser;
    private ViewPager myViewPager;
    private TabsAccessorAdapter myTabsAccessorAdapter;
    private TabLayout myTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);


        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();

        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            SendUserToLoginActivity();
        } else {
//            VerifyUserExistence();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistence() {
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists())) {
                    Toast.makeText(LandingActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                } else {
                    SendUserToProfileUpdateActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_create_group_option) {
            requestNewGroupCreation();
        }else if (item.getItemId() == R.id.main_profile_option) {
            SendUserToProfileUpdateActivity();
        }else if (item.getItemId() == R.id.main_logout_option) {
            updateUserStatus("offline");
            mAuth.signOut();
            SendUserToLoginActivity();
        }else if(item.getItemId() == R.id.main_invite) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, PICK_CONTACT);
        }
        return true;
    }

    private void requestNewGroupCreation() {
        Intent intent = new Intent(this, CreateGroupActivity.class);
        startActivity(intent);
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LandingActivity.this, R.style.AlertDialog);
        builder.setTitle("Create Group");
        final EditText groupNameField = new EditText(LandingActivity.this);
        groupNameField.setHint("Eg. ThunderBuddies");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(LandingActivity.this, "Please write Group Name...", Toast.LENGTH_SHORT).show();
                } else {
                    CreateNewGroup(groupName);
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    private void CreateNewGroup(final String groupName) {
        RootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LandingActivity.this, groupName + " group is Created Successfully...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(LandingActivity.this, SplashActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }

    private void SendUserToProfileUpdateActivity() {
        Intent settingsIntent = new Intent(LandingActivity.this, ProfileUpdateActivity.class);
        startActivity(settingsIntent);
    }


    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat(Constants.PATTERN.PATTERN_MMM_DD_yyyy);
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat(Constants.PATTERN.PATTERN_hh_mm_a);
        saveCurrentTime = currentTime.format(calendar.getTime());


        UserStatus userStatus = new UserStatus();
        userStatus.setDate(saveCurrentDate);
        userStatus.setTime(saveCurrentTime);
        userStatus.setStatus(state);

//
//        RootRef.child("Users").child(currentUserID).child("userState")
//                            .updateChildren(onlineStateMap);


    }
}