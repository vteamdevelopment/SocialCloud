package com.vteam.testdemo.profile

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.vteam.testdemo.R
import com.vteam.testdemo.common.Constants
import com.vteam.testdemo.landing.LandingActivity
import com.vteam.testdemo.landing.model.UserStatus
import com.vteam.testdemo.landing.model.Users
import com.vteam.testdemo.profile.CreateProfileActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class CreateProfileActivity : AppCompatActivity() {
    private var RootRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var currentUserID: String? = null
    private var UpdateAccountSettings: Button? = null
    private var userName: EditText? = null
    private var userStatus: EditText? = null
    private var userProfileImage: CircleImageView? = null
    private var UserProfileImagesRef: StorageReference? = null
    private var loadingBar: ProgressDialog? = null
    private var SettingsToolBar: Toolbar? = null
    private var userProfileImageEdit: CircleImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setActionBar()
        InitializeFields()
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth!!.currentUser!!.uid
        RootRef = FirebaseDatabase.getInstance().reference
        UserProfileImagesRef = FirebaseStorage.getInstance().reference.child("Profile Images")

//        userName.setEnabled(false);
        UpdateAccountSettings!!.setOnClickListener { UpdateSettings() }
        RetrieveUserInfo()
        userProfileImageEdit!!.setOnClickListener { //                Intent galleryIntent = new Intent();
//                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//                galleryIntent.setType("image/*");
//                startActivityForResult(galleryIntent, GALLERY_PICK);
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this@CreateProfileActivity)
        }
    }

    private fun updateUserStatus(state: String) {
        val saveCurrentTime: String
        val saveCurrentDate: String
        val calendar = Calendar.getInstance()
        val currentDate =
            SimpleDateFormat(Constants.PATTERN.PATTERN_MMM_DD_yyyy)
        saveCurrentDate = currentDate.format(calendar.time)
        val currentTime =
            SimpleDateFormat(Constants.PATTERN.PATTERN_hh_mm_a)
        saveCurrentTime = currentTime.format(calendar.time)
        val onlineStateMap =
            HashMap<String, Any>()
        onlineStateMap["time"] = saveCurrentTime
        onlineStateMap["date"] = saveCurrentDate
        onlineStateMap["state"] = state

//
//        RootRef.child("Users").child(currentUserID).child("userState")
//                            .updateChildren(onlineStateMap);
    }

    private fun UpdateSettings() {
        val userName = userName!!.text.toString()
        val status = userStatus!!.text.toString()
        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(this, "Please write your user name first....", Toast.LENGTH_SHORT).show()
        }
        if (TextUtils.isEmpty(status)) {
            Toast.makeText(this, "Please write your status....", Toast.LENGTH_SHORT).show()
        } else {
            val users = Users(currentUserID)
            users.name = userName
            users.status = status
            //            HashMap<String, Object> profileMap = new HashMap<>();
//            profileMap.put("uid", currentUserID);
//            profileMap.put("name", userName);
//            profileMap.put("status", status);
            val saveCurrentTime: String
            val saveCurrentDate: String
            val calendar = Calendar.getInstance()
            val currentDate =
                SimpleDateFormat(Constants.PATTERN.PATTERN_MMM_DD_yyyy)
            saveCurrentDate = currentDate.format(calendar.time)
            val currentTime =
                SimpleDateFormat(Constants.PATTERN.PATTERN_hh_mm_a)
            saveCurrentTime = currentTime.format(calendar.time)
            val userStatus = UserStatus()
            userStatus.date = saveCurrentDate
            userStatus.time = saveCurrentTime
            userStatus.status = Constants.USER_STATE.ONLINE
            users.userStatus = userStatus
            val userMap: MutableMap<String?, Any> =
                HashMap()
            userMap[currentUserID] = users
            RootRef!!.child(Constants.NODES.USER_NODE)
                .updateChildren(userMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        SendUserToMainActivity()
                        Toast.makeText(
                            this@CreateProfileActivity,
                            "Profile Updated Successfully...",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val message = task.exception.toString()
                        Toast.makeText(
                            this@CreateProfileActivity,
                            "Error: $message",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun RetrieveUserInfo() {
        RootRef!!.child(Constants.NODES.USER_NODE).child(currentUserID!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild(
                            "image"
                        )
                    ) {
                        val retrieveUserName =
                            dataSnapshot.child("name").value.toString()
                        val retrieveStatus =
                            dataSnapshot.child("status").value.toString()
                        val retrieveProfilePhoto =
                            dataSnapshot.child("image").value.toString()
                        userName!!.setText(retrieveUserName)
                        userStatus!!.setText(retrieveStatus)
                        val reference =
                            FirebaseStorage.getInstance().reference
                        reference.child(retrieveProfilePhoto).downloadUrl
                            .addOnSuccessListener { uri -> // Got the download URL for 'users/me/profile.png'
                                Log.d("URL", "" + uri)
                                Glide.with(applicationContext)
                                    .load(uri)
                                    .into(userProfileImage!!)
                            }.addOnFailureListener {
                                // Handle any errors
                            }
                    } else if (dataSnapshot.exists() && dataSnapshot.hasChild("name")) {
                        val retrieveUserName =
                            dataSnapshot.child("name").value.toString()
                        val retrievesStatus =
                            dataSnapshot.child("status").value.toString()
                        userName!!.setText(retrieveUserName)
                        userStatus!!.setText(retrievesStatus)
                    } else {
                        userName!!.visibility = View.VISIBLE
                        Toast.makeText(
                            this@CreateProfileActivity,
                            "Please set & update your profile information...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun SendUserToMainActivity() {
        val mainIntent = Intent(this@CreateProfileActivity, LandingActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }

    private fun InitializeFields() {
        UpdateAccountSettings =
            findViewById<View>(R.id.update_settings_button) as Button
        userName = findViewById<View>(R.id.set_user_name) as EditText
        userStatus = findViewById<View>(R.id.set_profile_status) as EditText
        userProfileImage =
            findViewById<View>(R.id.set_profile_image) as CircleImageView
        userProfileImageEdit =
            findViewById(R.id.profile_image_edit)
        loadingBar = ProgressDialog(this)
    }

    private fun setActionBar() {
        SettingsToolBar =
            findViewById<View>(R.id.settings_toolbar) as Toolbar
        setSupportActionBar(SettingsToolBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowCustomEnabled(true)
        supportActionBar!!.title = "Profile Update"
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_PICK) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val ImageUri = data.data
                CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this)
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                loadingBar!!.setTitle("Set Profile Image")
                loadingBar!!.setMessage("Please wait, your profile image is updating...")
                loadingBar!!.setCanceledOnTouchOutside(false)
                loadingBar!!.show()
                val resultUri = result.uri
                val filePath = UserProfileImagesRef!!.child("$currentUserID.jpg")
                filePath.putFile(resultUri).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@CreateProfileActivity,
                            "Profile Image uploaded Successfully...",
                            Toast.LENGTH_SHORT
                        ).show()
                        val downloaedUrl =
                            task.result.metadata!!.path
                        RootRef!!.child(Constants.NODES.USER_NODE)
                            .child(currentUserID!!).child("image")
                            .setValue(downloaedUrl)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this@CreateProfileActivity,
                                        "Image save in Database, Successfully...",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    loadingBar!!.dismiss()
                                } else {
                                    val message =
                                        task.exception.toString()
                                    Toast.makeText(
                                        this@CreateProfileActivity,
                                        "Error: $message",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    loadingBar!!.dismiss()
                                }
                            }
                    } else {
                        val message = task.exception.toString()
                        Toast.makeText(
                            this@CreateProfileActivity,
                            "Error: $message",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadingBar!!.dismiss()
                    }
                }
            }
        }
    }

    companion object {
        const val GALLERY_PICK = 1
    }
}