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
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.vteam.testdemo.R
import com.vteam.testdemo.chat.adapter.MessageAdapter
import com.vteam.testdemo.common.Constants
import com.vteam.testdemo.common.Constants.NODES.CHAT_NODE
import com.vteam.testdemo.common.Constants.NODES.MESSAGES_NODE
import com.vteam.testdemo.common.Utils.setOneToOneChat
import com.vteam.testdemo.landing.model.ChatModel
import com.vteam.testdemo.top.Messages
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var uploadTask: UploadTask
    private val messagesList: MutableList<Messages> =
        ArrayList()
    private var messageReceiverID: String? = null
    private var messageReceiverName: String? = null
    private var messageReceiverImage: String? = null
    private var messageSenderID: String? = null
    private var userName: TextView? = null
    private var userLastSeen: TextView? = null
    private var userImage: CircleImageView? = null
    private var ChatToolBar: Toolbar? = null
    private var mAuth: FirebaseAuth? = null
    private var RootRef: DatabaseReference? = null
    private var SendMessageButton: ImageButton? = null
    private var SendFilesButton: ImageButton? = null
    private var MessageInputText: EditText? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var messageAdapter: MessageAdapter? = null
    private var userMessagesList: RecyclerView? = null
    private var loadingBar: ProgressDialog? = null
    private var saveCurrentTime: String? = null
    private var saveCurrentDate: String? = null
    private var checker = ""
    private var myUrl = ""
    private var fileUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setActionBar()
        mAuth = FirebaseAuth.getInstance()
        messageSenderID = mAuth!!.currentUser!!.uid
        RootRef = FirebaseDatabase.getInstance().reference
        val extras = intent.extras
        messageReceiverID = extras!!["visit_user_id"].toString()
        messageReceiverName = extras["visit_user_name"].toString()
        if (extras.containsKey("visit_image") && extras["visit_image"] != null) {
            messageReceiverImage = extras["visit_image"].toString()
        }
        IntializeControllers()
        userName!!.text = messageReceiverName
        val reference = FirebaseStorage.getInstance().reference
        if (!TextUtils.isEmpty(messageReceiverImage)) {
            reference.child(messageReceiverImage!!).downloadUrl
                .addOnSuccessListener { uri -> // Got the download URL for 'users/me/profile.png'
                    Log.d("URL", "" + uri)
                    Glide.with(applicationContext)
                        .load(uri)
                        .into(userImage!!)
                }.addOnFailureListener {
                    // Handle any errors
                }
        }
        SendMessageButton!!.setOnClickListener { SendMessage() }
        DisplayLastSeen()
        SendFilesButton!!.setOnClickListener {
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
                    checker = "image"
                    val intent = Intent()
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.type = "image/*"
                    startActivityForResult(
                        Intent.createChooser(intent, "Select Image"),
                        443
                    )
                }
                if (i == 1) {
                    checker = "pdf"
                    val intent = Intent()
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.type = "application/pdf"
                    startActivityForResult(Intent.createChooser(intent, "Select PDF"), 443)
                }
                if (i == 2) {
                    checker = "docx"
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
        loadingBar = ProgressDialog(this)
        userName = findViewById(R.id.custom_profile_name)
        userImage =
            findViewById(R.id.custom_profile_image)
        userLastSeen = findViewById(R.id.custom_user_last_seen)
        SendMessageButton = findViewById(R.id.send_message_btn)
        SendFilesButton = findViewById(R.id.send_files_btn)
        MessageInputText = findViewById<View>(R.id.input_message) as EditText
        messageAdapter = MessageAdapter(messagesList)
        userMessagesList =
            findViewById<View>(R.id.private_messages_list_of_users) as RecyclerView
        linearLayoutManager = LinearLayoutManager(this)
        userMessagesList!!.layoutManager = linearLayoutManager
        userMessagesList!!.adapter = messageAdapter
        val calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("MMM dd, yyyy")
        saveCurrentDate = currentDate.format(calendar.time)
        val currentTime = SimpleDateFormat("hh:mm a")
        saveCurrentTime = currentTime.format(calendar.time)
    }

    private fun setActionBar() {
        ChatToolBar =
            findViewById<View>(R.id.chat_toolbar) as Toolbar
        setSupportActionBar(ChatToolBar)
        val actionBar = supportActionBar
        //        actionBar.setDefaultDisplayHomeAsUpEnabled(true);
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
            loadingBar!!.setTitle("Sending File")
            loadingBar!!.setMessage("Please wait, we are sending....")
            loadingBar!!.setCanceledOnTouchOutside(false)
            loadingBar!!.show()
            fileUri = data.data
            if (checker != "image") {
                val storageReference =
                    FirebaseStorage.getInstance().reference.child("Image Files")
                val messageSenderRef =
                    "Messages/$messageSenderID/$messageReceiverID"
                val messageReceiverRef =
                    "Messages/$messageReceiverID/$messageSenderID"
                val userMessageKeyRef = RootRef!!.child("Messages")
                    .child(messageSenderID!!).child(messageReceiverID!!).push()
                val messagePushID = userMessageKeyRef.key
                val filePath =
                    storageReference.child("$messagePushID.$checker")
                filePath.putFile(fileUri!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val messageTextBody: MutableMap<String, Any?> =
                            HashMap<String, Any?>()
                        messageTextBody["message"] = myUrl
                        messageTextBody["name"] = fileUri!!.lastPathSegment
                        if (checker == "pdf") {
                            messageTextBody["type"] = checker
                        } else {
                            messageTextBody["type"] = checker
                        }
                        messageTextBody["from"] = messageSenderID
                        messageTextBody["to"] = messageReceiverID
                        messageTextBody["messageID"] = messagePushID
                        messageTextBody["time"] = saveCurrentTime
                        messageTextBody["date"] = saveCurrentDate
                        val messageBodyDetails: MutableMap<String, Any?> =
                            HashMap<String, Any?>()
                        messageBodyDetails["$messageSenderRef/$messagePushID"] = messageTextBody
                        messageBodyDetails["$messageReceiverRef/$messagePushID"] = messageTextBody
                        RootRef!!.updateChildren(messageBodyDetails)
                        loadingBar!!.dismiss()
                    }
                }.addOnFailureListener { e ->
                    loadingBar!!.dismiss()
                    Toast.makeText(this@ChatActivity, e.message, Toast.LENGTH_SHORT).show()
                }
                    .addOnProgressListener { taskSnapshot ->
                        val p =
                            100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                        loadingBar!!.setMessage((p as Int).toString() + "% Uploading...")
                    }
            } else if (checker == "image") {
                val storageReference =
                    FirebaseStorage.getInstance().reference.child("Image Files")
                val messageSenderRef =
                    "Messages/$messageSenderID/$messageReceiverID"
                val messageReceiverRef =
                    "Messages/$messageReceiverID/$messageSenderID"
                val userMessageKeyRef = RootRef!!.child("Messages")
                    .child(messageSenderID!!).child(messageReceiverID!!).push()
                val messagePushID = userMessageKeyRef.key
                val filePath = storageReference.child("$messagePushID.jpg")

              uploadTask = filePath.putFile(fileUri!!)
                uploadTask.continueWithTask(object :
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
                        myUrl = downloadUrl.toString()
                        val messageSenderRef =
                            "ChatNode/$messageSenderID/$messageReceiverID"
                        val messageReceiverRef =
                            "ChatNode/$messageReceiverID/$messageSenderID"
                        val messageRef =
                            "MessagesNode/" + setOneToOneChat(
                                messageSenderID!!,
                                messageReceiverID!!
                            )
                        val chatModel = ChatModel()
                        chatModel.fromUid = messageSenderID
                        chatModel.fromUid = messageReceiverID
                        chatModel.date = saveCurrentDate
                        chatModel.time = saveCurrentTime
                        chatModel.lastMessage = myUrl
                        chatModel.type = checker
                        val messageTextBody: MutableMap<String, Any?> =
                            HashMap<String, Any?>()
                        messageTextBody["message"] = myUrl
                        messageTextBody["name"] = fileUri!!.lastPathSegment
                        messageTextBody["type"] = checker
                        messageTextBody["from"] = messageSenderID
                        messageTextBody["to"] = messageReceiverID
                        messageTextBody["messageID"] = messagePushID
                        messageTextBody["time"] = saveCurrentTime
                        messageTextBody["date"] = saveCurrentDate
                        val messageBodyDetails: MutableMap<String, Any?> =
                            HashMap<String, Any?>()
                        messageBodyDetails["$messageSenderRef/"] = chatModel
                        messageBodyDetails["$messageReceiverRef/$messagePushID"] = chatModel
                        messageBodyDetails["$messageRef/$messagePushID"] = messageTextBody
                        RootRef!!.updateChildren(messageBodyDetails)
                            .addOnCompleteListener(object : OnCompleteListener<Void> {


                                override fun onComplete(p0: Task<Void>) {
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            this@ChatActivity,
                                            "Message Sent Successfully...",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@ChatActivity,
                                            "Error",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    loadingBar!!.dismiss()
                                    MessageInputText!!.setText("")
                                }
                            })
                    }

            } else {
                loadingBar!!.dismiss()
                Toast.makeText(this, "nothing selected,error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun DisplayLastSeen() {
        RootRef!!.child(Constants.NODES.USER_NODE).child(messageReceiverID!!)
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
                            userLastSeen!!.text = "online"
                        } else if (state == "offline") {
                            userLastSeen!!.text = "Last Seen: $date $time"
                        }
                    } else {
                        userLastSeen!!.text = "offline"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    override fun onStart() {
        super.onStart()
        RootRef!!.child(MESSAGES_NODE).child(
            setOneToOneChat(
                messageSenderID!!,
                messageReceiverID!!
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
                    messagesList.add(messages)
                    messageAdapter!!.notifyDataSetChanged()
                    userMessagesList!!.smoothScrollToPosition(
                        userMessagesList!!.adapter!!.itemCount
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
        val messageText = MessageInputText!!.text.toString()
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show()
        } else {
            val userMessageKeyRef = RootRef!!.child(MESSAGES_NODE)
                .child(
                    setOneToOneChat(
                        messageSenderID!!,
                        messageReceiverID!!
                    )
                ).push()
            val userChatNodeKeyRef = RootRef!!.child(CHAT_NODE)
            val type = "text"
            val messagePushID = userMessageKeyRef.key
            val messageSenderRef = "$messageSenderID/$messageReceiverID"
            val messageReceiverRef = "$messageReceiverID/$messageSenderID"
            //            String messageRef = "MessagesNode/" + Utils.setOneToOneChat(messageSenderID,messageReceiverID);
            val chatModel = ChatModel()
            chatModel.fromUid = messageSenderID
            chatModel.toUid = messageReceiverID
            chatModel.date = saveCurrentDate
            chatModel.time = saveCurrentTime
            chatModel.lastMessage = messageText
            chatModel.type = type
            val messageTextBody: MutableMap<String, Any?> =
                HashMap<String, Any?>()
            messageTextBody["message"] = messageText
            messageTextBody["type"] = type
            messageTextBody["from"] = messageSenderID
            messageTextBody["to"] = messageReceiverID
            messageTextBody["messageID"] = messagePushID
            messageTextBody["time"] = saveCurrentTime
            messageTextBody["date"] = saveCurrentDate

//            Map messageBodyDetails = new HashMap();
//            messageBodyDetails.put(messageRef + "/" + messagePushID, messageTextBody);
            val ChatBodyDetails: MutableMap<String, Any?> =
                HashMap<String, Any?>()
            ChatBodyDetails["$messageSenderRef/"] = chatModel
            ChatBodyDetails["$messageReceiverRef/"] = chatModel
            userMessageKeyRef.updateChildren(messageTextBody)
                .addOnCompleteListener(object : OnCompleteListener<Void> {
                    override fun onComplete(task: Task<Void>) {
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@ChatActivity,
                                "Message Sent Successfully...",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(this@ChatActivity, "Error", Toast.LENGTH_SHORT).show()
                        }
                        MessageInputText!!.setText("")
                    }
                })
            userChatNodeKeyRef.updateChildren(ChatBodyDetails)
                .addOnCompleteListener(object : OnCompleteListener<Void> {
                    override fun onComplete(task: Task<Void>) {
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@ChatActivity,
                                "Chat Sent Successfully...",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(this@ChatActivity, "Error", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        }
    }
}