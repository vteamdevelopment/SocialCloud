package com.vteam.testdemo.group

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.vteam.testdemo.R
import com.vteam.testdemo.chat.adapter.SelectedUserAdapter
import com.vteam.testdemo.chat.model.GroupDetails
import com.vteam.testdemo.common.Constants
import com.vteam.testdemo.common.Constants.NODES.GROUP_DETAILS
import com.vteam.testdemo.databinding.CreateGroupFragmentBinding
import com.vteam.testdemo.landing.model.Users
import com.vteam.testdemo.profile.CreateProfileActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CreateGroupFragment : Fragment() {

    companion object {
        fun newInstance() = CreateGroupFragment()
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: CreateGroupFragmentBinding
    private lateinit var viewModel: CreateGroupViewModel2
    private lateinit var groupProfileImagesRef: StorageReference
    private lateinit var userProfileImagesRef: StorageReference
    private lateinit var rootRef: DatabaseReference
    private lateinit var currentUserID: String

    var selectedUserList: MutableList<Users> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.containsKey(Constants.KEY.SELECTED_USER)) {
                selectedUserList = it.getParcelableArrayList<Users>(Constants.KEY.SELECTED_USER)!!

            }
        }
        auth = FirebaseAuth.getInstance()
        currentUserID = auth.currentUser!!.uid
        rootRef = FirebaseDatabase.getInstance().reference
        userProfileImagesRef = FirebaseStorage.getInstance().reference.child("Profile Images")
        groupProfileImagesRef = FirebaseStorage.getInstance().reference.child("Profile Images")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.create_group_fragment, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val gridLayoutManager = GridLayoutManager(context, 4)
        gridLayoutManager.orientation = GridLayoutManager.VERTICAL
        binding.groupSelectedList.layoutManager = gridLayoutManager
        binding.groupSelectedList.adapter = SelectedUserAdapter(selectedUserList)

        binding.next.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                val groupDetails: GroupDetails = GroupDetails("adsdfsd");
                groupDetails.adminId = currentUserID
                groupDetails.adminName = "Name of admin"
                val calForDate = Calendar.getInstance()
                val currentDateFormat =
                    SimpleDateFormat(Constants.PATTERN.PATTERN_MMM_DD_yyyy)
                var currentDate = currentDateFormat.format(calForDate.time)


                groupDetails.createdAt = currentDate
                groupDetails.memebers = selectedUserList.map { it.uId }
                groupDetails.name = "User Name"

                val groupDetailsRef = rootRef.child(GROUP_DETAILS)
                val groupDetailsKeyRef: DatabaseReference = groupDetailsRef.push()


                val messagePushID = groupDetailsKeyRef.key

                var map = HashMap<String,Any?>();

                messagePushID?.let { map.put(it,groupDetails) }


                groupDetailsRef.updateChildren(map)
                    .addOnCompleteListener(object : OnCompleteListener<Void> {
                        override fun onComplete(p0: Task<Void>) {
                            if (p0.isSuccessful()) {
                                Toast.makeText(
                                    context,
                                    "Group created Successfully...",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(context, "Error", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    })

            }
        })

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CreateGroupViewModel2::class.java)
        // TODO: Use the ViewModel
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CreateProfileActivity.GALLERY_PICK) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val ImageUri = data.data
                activity?.let {
                    CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(it)
                }
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                var loadingBar = ProgressDialog(context)
                loadingBar.setTitle("Set Profile Image")
                loadingBar.setMessage("Please wait, your profile image is updating...")
                loadingBar.setCanceledOnTouchOutside(false)
                loadingBar.show()
                val resultUri = result.uri
                val filePath: StorageReference = userProfileImagesRef.child(currentUserID + ".jpg")
                filePath.putFile(resultUri).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            context,
                            "Profile Image uploaded Successfully...",
                            Toast.LENGTH_SHORT
                        ).show()
                        val downloaedUrl =
                            task.result.metadata!!.path
                        rootRef.child(com.vteam.testdemo.common.Constants.NODES.USER_NODE)
                            .child(currentUserID).child("image")
                            .setValue(downloaedUrl)
                            .addOnCompleteListener(OnCompleteListener<Void?> { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Image save in Database, Successfully...",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    loadingBar.dismiss()
                                } else {
                                    val message =
                                        task.exception.toString()
                                    Toast.makeText(
                                        context,
                                        "Error: $message",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    loadingBar.dismiss()
                                }
                            })
                    } else {
                        val message = task.exception.toString()
                        Toast.makeText(
                            context,
                            "Error: $message",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadingBar.dismiss()
                    }
                }
            }
        }

    }

}