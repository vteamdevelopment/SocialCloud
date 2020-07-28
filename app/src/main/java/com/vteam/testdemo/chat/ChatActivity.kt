package com.vteam.testdemo.chat

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.vteam.testdemo.R
import com.vteam.testdemo.chat.adapter.MessageAdapter
import com.vteam.testdemo.common.Constants
import com.vteam.testdemo.common.Constants.NODES.CHAT_NODE
import com.vteam.testdemo.common.Constants.NODES.MESSAGES_NODE
import com.vteam.testdemo.common.Utils.setOneToOneChat
import com.vteam.testdemo.databinding.ActivityChatBinding
import com.vteam.testdemo.landing.model.ChatModel
import com.vteam.testdemo.top.Messages
import kotlinx.android.synthetic.main.create_group_fragment.*
import kotlinx.android.synthetic.main.custom_chat_bar.view.*
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityChatBinding
    private lateinit var mUploadTask: UploadTask
    private val mMessagesList: MutableList<Messages> =
        ArrayList()
    private var mMessageReceiverID: String? = null
    private var mMessageReceiverName: String? = null
    private var mMessageReceiverImage: String? = null
    private var mMessageSenderID: String? = null

    private var mAuth: FirebaseAuth? = null
    private var mRootRef: DatabaseReference? = null

    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var mMessageAdapter: MessageAdapter? = null
//    private var userMessagesList: RecyclerView? = null
    private var mLoadingBar: ProgressDialog? = null
    private var mSaveCurrentTime: String? = null
    private var mSaveCurrentDate: String? = null
    private var mChecker = ""
    private var mMyUrl = ""
    private var mFileUri: Uri? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        mBinding= DataBindingUtil.setContentView(this,R.layout.activity_chat)

        setActionBar()
        mAuth = FirebaseAuth.getInstance()
        mMessageSenderID = mAuth!!.currentUser!!.uid
        mRootRef = FirebaseDatabase.getInstance().reference
        val extras = intent.extras
        mMessageReceiverID = extras!!["visit_user_id"].toString()
        mMessageReceiverName = extras["visit_user_name"].toString()
        if (extras.containsKey("visit_image") && extras["visit_image"] != null) {
            mMessageReceiverImage = extras["visit_image"].toString()
        }
        IntializeControllers()
        mBinding.chatToolbar.user_name.text = mMessageReceiverName
        val reference = FirebaseStorage.getInstance().reference
        if (!TextUtils.isEmpty(mMessageReceiverImage)) {
            reference.child(mMessageReceiverImage!!).downloadUrl
                .addOnSuccessListener { uri -> // Got the download URL for 'users/me/profile.png'
                    Log.d("URL", "" + uri)
                    Glide.with(applicationContext)
                        .load(uri)
                        .into(mBinding.chatToolbar.profile_image!!)
                }.addOnFailureListener {
                    // Handle any errors
                }
        }
        mBinding.sendMessageBtn!!.setOnClickListener { SendMessage() }
        DisplayLastSeen()
        mBinding.sendFilesBtn!!.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Images",
                "Pdf Files",
                "MS Word Files"
            )
            val builder =
                AlertDialog.Builder(this@ChatActivity)
            builder.setTitle("Select the File")
            builder.setItems(options) { dialogInterface, i ->
                if (i == 0) {
                    mChecker = "image"
                    val intent = Intent()
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.type = "image/*"
                    startActivityForResult(
                        Intent.createChooser(intent, "Select Image"),
                        443
                    )
                }
                if (i == 1) {
                    mChecker = "pdf"
                    val intent = Intent()
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.type = "application/pdf"
                    startActivityForResult(Intent.createChooser(intent, "Select PDF"), 443)
                }
                if (i == 2) {
                    mChecker = "docx"
                    val intent = Intent()
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.type = "application/msword"
                    startActivityForResult(
                        Intent.createChooser(
                            intent,
                            "Select MSWORD FILE"
                        ), 443
                    )
                }
            }
            builder.show()
        }
    }

    private fun IntializeControllers() {
        mLoadingBar = ProgressDialog(this)

        mMessageAdapter = MessageAdapter(mMessagesList)

        mLinearLayoutManager = LinearLayoutManager(this)
        mBinding.userMessagesList!!.layoutManager = mLinearLayoutManager
        mBinding.userMessagesList!!.adapter = mMessageAdapter
        val calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("MMM dd, yyyy")
        mSaveCurrentDate = currentDate.format(calendar.time)
        val currentTime = SimpleDateFormat("hh:mm a")
        mSaveCurrentTime = currentTime.format(calendar.time)
    }

    private fun setActionBar() {

        setSupportActionBar(mBinding.chatToolbar as Toolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayShowCustomEnabled(true)
        val layoutInflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val actionBarView =
            layoutInflater.inflate(R.layout.custom_chat_bar, null)
        actionBar.customView = actionBarView
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 443 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            mLoadingBar!!.setTitle("Sending File")
            mLoadingBar!!.setMessage("Please wait, we are sending....")
            mLoadingBar!!.setCanceledOnTouchOutside(false)
            mLoadingBar!!.show()
            mFileUri = data.data
            if (mChecker != "image") {
                val storageReference =
                    FirebaseStorage.getInstance().reference.child("Image Files")
                val messageSenderRef =
                    "Messages/$mMessageSenderID/$mMessageReceiverID"
                val messageReceiverRef =
                    "Messages/$mMessageReceiverID/$mMessageSenderID"
                val userMessageKeyRef = mRootRef!!.child("Messages")
                    .child(mMessageSenderID!!).child(mMessageReceiverID!!).push()
                val messagePushID = userMessageKeyRef.key
                val filePath =
                    storageReference.child("$messagePushID.$mChecker")
                filePath.putFile(mFileUri!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val messageTextBody: MutableMap<String, Any?> =
                            HashMap<String, Any?>()
                        messageTextBody["message"] = mMyUrl
                        messageTextBody["name"] = mFileUri!!.lastPathSegment
                        if (mChecker == "pdf") {
                            messageTextBody["type"] = mChecker
                        } else {
                            messageTextBody["type"] = mChecker
                        }
                        messageTextBody["from"] = mMessageSenderID
                        messageTextBody["to"] = mMessageReceiverID
                        messageTextBody["messageID"] = messagePushID
                        messageTextBody["time"] = mSaveCurrentTime
                        messageTextBody["date"] = mSaveCurrentDate
                        val messageBodyDetails: MutableMap<String, Any?> =
                            HashMap<String, Any?>()
                        messageBodyDetails["$messageSenderRef/$messagePushID"] = messageTextBody
                        messageBodyDetails["$messageReceiverRef/$messagePushID"] = messageTextBody
                        mRootRef!!.updateChildren(messageBodyDetails)
                        mLoadingBar!!.dismiss()
                    }
                }.addOnFailureListener { e ->
                    mLoadingBar!!.dismiss()
                    Toast.makeText(this@ChatActivity, e.message, Toast.LENGTH_SHORT).show()
                }
                    .addOnProgressListener { taskSnapshot ->
                        val p =
                            100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                        mLoadingBar!!.setMessage((p as Int).toString() + "% Uploading...")
                    }
            } else if (mChecker == "image") {
                val storageReference =
                    FirebaseStorage.getInstance().reference.child("Image Files")
                val messageSenderRef =
                    "Messages/$mMessageSenderID/$mMessageReceiverID"
                val messageReceiverRef =
                    "Messages/$mMessageReceiverID/$mMessageSenderID"
                val userMessageKeyRef = mRootRef!!.child("Messages")
                    .child(mMessageSenderID!!).child(mMessageReceiverID!!).push()
                val messagePushID = userMessageKeyRef.key
                val filePath = storageReference.child("$messagePushID.jpg")

              mUploadTask = filePath.putFile(mFileUri!!)
                mUploadTask.continueWithTask(object :
                    Continuation<UploadTask.TaskSnapshot, Task<Uri>>{
                    override fun then(p0: Task<UploadTask.TaskSnapshot>): Task<Uri> {
                        if (!p0.isSuccessful) {
                            throw p0.exception!!
                        }
                        return filePath.downloadUrl
                    }
                })
                    .addOnCompleteListener { task ->
                        val downloadUrl = task.result
                        mMyUrl = downloadUrl.toString()
                        val messageSenderRef =
                            "ChatNode/$mMessageSenderID/$mMessageReceiverID"
                        val messageReceiverRef =
                            "ChatNode/$mMessageReceiverID/$mMessageSenderID"
                        val messageRef =
                            "MessagesNode/" + setOneToOneChat(
                                mMessageSenderID!!,
                                mMessageReceiverID!!
                            )
                        val chatModel = ChatModel()
                        chatModel.fromUid = mMessageSenderID
                        chatModel.fromUid = mMessageReceiverID
                        chatModel.date = mSaveCurrentDate
                        chatModel.time = mSaveCurrentTime
                        chatModel.lastMessage = mMyUrl
                        chatModel.type = mChecker
                        val messageTextBody: MutableMap<String, Any?> =
                            HashMap<String, Any?>()
                        messageTextBody["message"] = mMyUrl
                        messageTextBody["name"] = mFileUri!!.lastPathSegment
                        messageTextBody["type"] = mChecker
                        messageTextBody["from"] = mMessageSenderID
                        messageTextBody["to"] = mMessageReceiverID
                        messageTextBody["messageID"] = messagePushID
                        messageTextBody["time"] = mSaveCurrentTime
                        messageTextBody["date"] = mSaveCurrentDate
                        val messageBodyDetails: MutableMap<String, Any?> =
                            HashMap<String, Any?>()
                        messageBodyDetails["$messageSenderRef/"] = chatModel
                        messageBodyDetails["$messageReceiverRef/$messagePushID"] = chatModel
                        messageBodyDetails["$messageRef/$messagePushID"] = messageTextBody
                        mRootRef!!.updateChildren(messageBodyDetails)
                            .addOnCompleteListener(object : OnCompleteListener<Void> {

                                override fun onComplete(p0: Task<Void>) {
                                    mLoadingBar!!.dismiss()
                                    mBinding.inputMessage!!.setText("")
                                }
                            })
                    }

            } else {
                mLoadingBar!!.dismiss()
//                Toast.makeText(this, "nothing selected,error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun DisplayLastSeen() {
        mRootRef!!.child(Constants.NODES.USER_NODE).child(mMessageReceiverID!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child("userState").hasChild("state")) {
                        val state =
                            dataSnapshot.child("userState").child("state").value.toString()
                        val date =
                            dataSnapshot.child("userState").child("date").value.toString()
                        val time =
                            dataSnapshot.child("userState").child("time").value.toString()
                        if (state == "online") {
                            mBinding.chatToolbar.user_last_seen.text = "online"
                        } else if (state == "offline") {
                            mBinding.chatToolbar.user_last_seen.text = "Last Seen: $date $time"
                        }
                    } else {
                        mBinding.chatToolbar.user_last_seen.text= "offline"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    override fun onStart() {
        super.onStart()
        mRootRef!!.child(MESSAGES_NODE).child(
            setOneToOneChat(
                mMessageSenderID!!,
                mMessageReceiverID!!
            )
        )
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(
                    dataSnapshot: DataSnapshot,
                    s: String?
                ) {
                    val messages =
                        dataSnapshot.getValue(
                            Messages::class.java
                        )!!
                    mMessagesList.add(messages)
                    mMessageAdapter!!.notifyDataSetChanged()
                    mBinding.userMessagesList!!.smoothScrollToPosition(
                        mBinding.userMessagesList!!.adapter!!.itemCount
                    )
                }

                override fun onChildChanged(
                    dataSnapshot: DataSnapshot,
                    s: String?
                ) {
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                override fun onChildMoved(
                    dataSnapshot: DataSnapshot,
                    s: String?
                ) {
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun SendMessage() {
        val messageText = mBinding.inputMessage.text.toString()
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show()
        } else {
            val userMessageKeyRef = mRootRef!!.child(MESSAGES_NODE)
                .child(
                    setOneToOneChat(
                        mMessageSenderID!!,
                        mMessageReceiverID!!
                    )
                ).push()
            val userChatNodeKeyRef = mRootRef!!.child(CHAT_NODE)
            val type = "text"
            val messagePushID = userMessageKeyRef.key
            val messageSenderRef = "$mMessageSenderID/$mMessageReceiverID"
            val messageReceiverRef = "$mMessageReceiverID/$mMessageSenderID"
            //            String messageRef = "MessagesNode/" + Utils.setOneToOneChat(messageSenderID,messageReceiverID);
            val chatModel = ChatModel()
            chatModel.fromUid = mMessageSenderID
            chatModel.toUid = mMessageReceiverID
            chatModel.date = mSaveCurrentDate
            chatModel.time = mSaveCurrentTime
            chatModel.lastMessage = messageText
            chatModel.type = type
            val messageTextBody: MutableMap<String, Any?> =
                HashMap<String, Any?>()
            messageTextBody["message"] = messageText
            messageTextBody["type"] = type
            messageTextBody["from"] = mMessageSenderID
            messageTextBody["to"] = mMessageReceiverID
            messageTextBody["messageID"] = messagePushID
            messageTextBody["time"] = mSaveCurrentTime
            messageTextBody["date"] = mSaveCurrentDate

//            Map messageBodyDetails = new HashMap();
//            messageBodyDetails.put(messageRef + "/" + messagePushID, messageTextBody);
            val ChatBodyDetails: MutableMap<String, Any?> =
                HashMap<String, Any?>()
            ChatBodyDetails["$messageSenderRef/"] = chatModel
            ChatBodyDetails["$messageReceiverRef/"] = chatModel
            userMessageKeyRef.updateChildren(messageTextBody)
                .addOnCompleteListener(object : OnCompleteListener<Void> {
                    override fun onComplete(task: Task<Void>) {
                       mBinding.inputMessage!!.setText("")
                    }
                })
            userChatNodeKeyRef.updateChildren(ChatBodyDetails)
                .addOnCompleteListener(object : OnCompleteListener<Void> {
                    override fun onComplete(task: Task<Void>) {

                    }
                })
        }
    }
}