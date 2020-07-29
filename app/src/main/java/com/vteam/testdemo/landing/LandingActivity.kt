package com.vteam.testdemo.landing

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.vteam.testdemo.R
import com.vteam.testdemo.SplashActivity
import com.vteam.testdemo.common.ConstantNodes
import com.vteam.testdemo.common.Constants
import com.vteam.testdemo.group.CreateGroupActivity
import com.vteam.testdemo.landing.model.UserStatus
import com.vteam.testdemo.profile.ProfileUpdateActivity
import java.text.SimpleDateFormat
import java.util.*

class LandingActivity : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    private var currentUser: FirebaseUser? = null
    private var auth: FirebaseAuth? = null
    private var rootRef: DatabaseReference? = null
    private var currentUserID: String? = null
    private val existingUser = false
    private var viewPager: ViewPager? = null
    private var tabsAccessorAdapter: TabsAccessorAdapter? = null
    private var tabLayout: TabLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
        toolbar =
            findViewById<View>(R.id.main_page_toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.app_name)
        auth = FirebaseAuth.getInstance()
        currentUser = auth!!.currentUser
        rootRef = FirebaseDatabase.getInstance().reference
        viewPager = findViewById<View>(R.id.main_tabs_pager) as ViewPager
        tabsAccessorAdapter = TabsAccessorAdapter(supportFragmentManager)
        viewPager!!.adapter = tabsAccessorAdapter
        tabLayout = findViewById<View>(R.id.main_tabs) as TabLayout
        tabLayout!!.setupWithViewPager(viewPager)
    }

    override fun onStart() {
        super.onStart()
        if (currentUser == null) {
            SendUserToLoginActivity()
        } else {
//            VerifyUserExistence();
        }
    }

    override fun onStop() {
        super.onStop()
        if (currentUser != null) {
            updateUserStatus("offline")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (currentUser != null) {
            updateUserStatus("offline")
        }
    }

    private fun VerifyUserExistence() {
        currentUserID = auth!!.currentUser!!.uid
        rootRef!!.child(ConstantNodes.NODES.USER_NODE).child(currentUserID!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child("name").exists()) {
                        Toast.makeText(this@LandingActivity, "Welcome", Toast.LENGTH_SHORT).show()
                    } else {
                        SendUserToProfileUpdateActivity()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId == R.id.main_create_group_option) {
            requestNewGroupCreation()
        } else if (item.itemId == R.id.main_profile_option) {
            SendUserToProfileUpdateActivity()
        } else if (item.itemId == R.id.main_logout_option) {
            updateUserStatus("offline")
            auth!!.signOut()
            SendUserToLoginActivity()
        } else if (item.itemId == R.id.main_invite) {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            startActivityForResult(intent, PICK_CONTACT)
        }
        return true
    }

    private fun requestNewGroupCreation() {
        val intent = Intent(this, CreateGroupActivity::class.java)
        startActivity(intent)
    }

    private fun RequestNewGroup() {
        val builder =
            AlertDialog.Builder(this@LandingActivity, R.style.AlertDialog)
        builder.setTitle("Create Group")
        val groupNameField = EditText(this@LandingActivity)
        groupNameField.hint = "Eg. ThunderBuddies"
        builder.setView(groupNameField)
        builder.setPositiveButton("Create") { dialogInterface, i ->
            val groupName = groupNameField.text.toString()
            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(
                    this@LandingActivity,
                    "Please write Group Name...",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                CreateNewGroup(groupName)
            }
        }
        builder.setNegativeButton("Cancel") { dialogInterface, i -> dialogInterface.cancel() }
        builder.show()
    }

    private fun CreateNewGroup(groupName: String) {
        rootRef!!.child("Groups").child(groupName).setValue("")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@LandingActivity,
                        "$groupName group is Created Successfully...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun SendUserToLoginActivity() {
        val loginIntent = Intent(this@LandingActivity, SplashActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(loginIntent)
    }

    private fun SendUserToProfileUpdateActivity() {
        val settingsIntent = Intent(this@LandingActivity, ProfileUpdateActivity::class.java)
        startActivity(settingsIntent)
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
        val userStatus = UserStatus()
        userStatus.date = saveCurrentDate
        userStatus.time = saveCurrentTime
        userStatus.status = state

//
//        RootRef.child(com.vteam.testdemo.common.Constants.NODES.USER_NODE).child(currentUserID).child("userState")
//                            .updateChildren(onlineStateMap);
    }

    companion object {
        private const val PICK_CONTACT = 100
    }
}