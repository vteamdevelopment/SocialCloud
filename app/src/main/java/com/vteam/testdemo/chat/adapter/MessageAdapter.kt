package com.vteam.testdemo.chat.adapter

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.vteam.testdemo.R
import com.vteam.testdemo.chat.adapter.MessageAdapter.MessageViewHolder
import com.vteam.testdemo.landing.LandingActivity
import com.vteam.testdemo.top.ImageViewerActivity
import com.vteam.testdemo.top.Messages
import de.hdodenhof.circleimageview.CircleImageView

class MessageAdapter(private val userMessagesList: List<Messages>) :
    RecyclerView.Adapter<MessageViewHolder>() {
    private var mAuth: FirebaseAuth? = null
    private var usersRef: DatabaseReference? = null
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MessageViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.custom_messages_layout, viewGroup, false)
        mAuth = FirebaseAuth.getInstance()
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(
        messageViewHolder: MessageViewHolder,
        position: Int
    ) {
        val messageSenderId = mAuth!!.currentUser!!.uid
        val messages = userMessagesList[position]
        val fromUserID = messages.from
        val fromMessageType = messages.type
        usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(fromUserID)
        usersRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    val receiverImage =
                        dataSnapshot.child("image").value.toString()
                    if (!receiverImage.isEmpty()) {
                        val reference =
                            FirebaseStorage.getInstance().reference
                        reference.child(receiverImage).downloadUrl
                            .addOnSuccessListener { uri -> // Got the download URL for 'users/me/profile.png'
                                Log.d("URL", "" + uri)
                                Glide.with(messageViewHolder.receiverProfileImage.context)
                                    .load(uri)
                                    .into(messageViewHolder.receiverProfileImage)
                            }.addOnFailureListener {
                                // Handle any errors
                            }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        messageViewHolder.receiverMessageText.visibility = View.GONE
        messageViewHolder.receiverProfileImage.visibility = View.GONE
        messageViewHolder.senderMessageText.visibility = View.GONE
        messageViewHolder.messageSenderPicture.visibility = View.GONE
        messageViewHolder.messageReceiverPicture.visibility = View.GONE
        if (fromMessageType == "text") {
            if (fromUserID == messageSenderId) {
                messageViewHolder.senderMessageText.visibility = View.VISIBLE
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout)
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK)
                messageViewHolder.senderMessageText.text = """${messages.message}

${messages.time} - ${messages.date}"""
            } else {
                messageViewHolder.receiverProfileImage.visibility = View.VISIBLE
                messageViewHolder.receiverMessageText.visibility = View.VISIBLE
                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout)
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK)
                messageViewHolder.receiverMessageText.text = """${messages.message}

${messages.time} - ${messages.date}"""
            }
        } else if (fromMessageType == "image") {
            if (fromUserID == messageSenderId) {
                messageViewHolder.messageSenderPicture.visibility = View.VISIBLE
                if (!messages.message.isEmpty()) {
                    val reference = FirebaseStorage.getInstance().reference
                    reference.child(messages.message).downloadUrl
                        .addOnSuccessListener { uri -> // Got the download URL for 'users/me/profile.png'
                            Log.d("URL", "" + uri)
                            Glide.with(messageViewHolder.messageSenderPicture.context)
                                .load(uri)
                                .into(messageViewHolder.messageSenderPicture)
                        }.addOnFailureListener {
                            // Handle any errors
                        }
                }
            } else {
                messageViewHolder.receiverProfileImage.visibility = View.VISIBLE
                messageViewHolder.messageReceiverPicture.visibility = View.VISIBLE
                if (!messages.message.isEmpty()) {
                    val reference = FirebaseStorage.getInstance().reference
                    reference.child(messages.message).downloadUrl
                        .addOnSuccessListener { uri -> // Got the download URL for 'users/me/profile.png'
                            Log.d("URL", "" + uri)
                            Glide.with(messageViewHolder.messageReceiverPicture.context)
                                .load(uri)
                                .into(messageViewHolder.messageReceiverPicture)
                        }.addOnFailureListener {
                            // Handle any errors
                        }
                }
            }
        } else if (fromMessageType == "pdf" || fromMessageType == "docx") {
            if (fromUserID == messageSenderId) {
                messageViewHolder.messageSenderPicture.visibility = View.VISIBLE
                //                messageViewHolder.messageSenderPicture.setBackground(R.drawable.);
                messageViewHolder.itemView.setOnClickListener {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(userMessagesList[position].message)
                    )
                    messageViewHolder.itemView.context.startActivity(intent)
                }
            } else {
                messageViewHolder.receiverProfileImage.visibility = View.VISIBLE
                messageViewHolder.messageReceiverPicture.visibility = View.VISIBLE
                //                messageViewHolder.messageReceiverPicture.setBackground(R.drawable.file);
            }
        }
        if (fromUserID == messageSenderId) {
            messageViewHolder.itemView.setOnClickListener {
                if (userMessagesList[position]
                        .type == "pdf" || userMessagesList[position]
                        .type == "docx"
                ) {
                    val options =
                        arrayOf<CharSequence>(
                            "Delete For me",
                            "Download and View this Document",
                            "Cancel",
                            "Delete For EveryOne"
                        )
                    val builder =
                        AlertDialog.Builder(messageViewHolder.itemView.context)
                    builder.setTitle("Delete Message")
                    builder.setItems(options) { dialogInterface, position ->
                        if (position == 0) {
                            deleteSentMessage(position, messageViewHolder)
                            val intent = Intent(
                                messageViewHolder.itemView.context,
                                LandingActivity::class.java
                            )
                            messageViewHolder.itemView.context.startActivity(intent)
                        } else if (position == 1) {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    userMessagesList[position].message
                                )
                            )
                            messageViewHolder.itemView.context.startActivity(intent)
                        } else if (position == 3) {
                            deleteMessageForEveryOne(position, messageViewHolder)
                        }
                    }
                    builder.show()
                } else if (userMessagesList[position].type == "text") {
                    val options =
                        arrayOf<CharSequence>(
                            "Delete For me",
                            "Cancel",
                            "Delete For EveryOne"
                        )
                    val builder =
                        AlertDialog.Builder(messageViewHolder.itemView.context)
                    builder.setTitle("Delete Message")
                    builder.setItems(options) { dialogInterface, position ->
                        if (position == 0) {
                            deleteSentMessage(position, messageViewHolder)
                            val intent = Intent(
                                messageViewHolder.itemView.context,
                                LandingActivity::class.java
                            )
                            messageViewHolder.itemView.context.startActivity(intent)
                        } else if (position == 2) {
                            deleteMessageForEveryOne(position, messageViewHolder)
                        }
                    }
                    builder.show()
                } else if (userMessagesList[position].type == "image") {
                    val options =
                        arrayOf<CharSequence>(
                            "Delete For me",
                            "View This Image",
                            "Cancel",
                            "Delete For EveryOne"
                        )
                    val builder =
                        AlertDialog.Builder(messageViewHolder.itemView.context)
                    builder.setTitle("Delete Message")
                    builder.setItems(options) { dialogInterface, position ->
                        if (position == 0) {
                            deleteSentMessage(position, messageViewHolder)
                            val intent = Intent(
                                messageViewHolder.itemView.context,
                                LandingActivity::class.java
                            )
                            messageViewHolder.itemView.context.startActivity(intent)
                        } else if (position == 1) {
                            val intent = Intent(
                                messageViewHolder.itemView.context,
                                ImageViewerActivity::class.java
                            )
                            intent.putExtra(
                                "url",
                                userMessagesList[position].message
                            )
                            messageViewHolder.itemView.context.startActivity(intent)
                        } else if (position == 3) {
                            deleteMessageForEveryOne(position, messageViewHolder)
                        }
                    }
                    builder.show()
                }
            }
        } else {
            messageViewHolder.itemView.setOnClickListener {
                if (userMessagesList[position]
                        .type == "pdf" || userMessagesList[position]
                        .type == "docx"
                ) {
                    val options =
                        arrayOf<CharSequence>(
                            "Delete For me",
                            "Download and View this Document",
                            "Cancel"
                        )
                    val builder =
                        AlertDialog.Builder(messageViewHolder.itemView.context)
                    builder.setTitle("Delete Message")
                    builder.setItems(options) { dialogInterface, position ->
                        if (position == 0) {
                            deleteReceiverMessage(position, messageViewHolder)
                            val intent = Intent(
                                messageViewHolder.itemView.context,
                                LandingActivity::class.java
                            )
                            messageViewHolder.itemView.context.startActivity(intent)
                        } else if (position == 1) {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    userMessagesList[position].message
                                )
                            )
                            messageViewHolder.itemView.context.startActivity(intent)
                        }
                    }
                    builder.show()
                } else if (userMessagesList[position].type == "text") {
                    val options =
                        arrayOf<CharSequence>(
                            "Delete For me",
                            "Cancel"
                        )
                    val builder =
                        AlertDialog.Builder(messageViewHolder.itemView.context)
                    builder.setTitle("Delete Message")
                    builder.setItems(options) { dialogInterface, position ->
                        if (position == 0) {
                            deleteReceiverMessage(position, messageViewHolder)
                            val intent = Intent(
                                messageViewHolder.itemView.context,
                                LandingActivity::class.java
                            )
                            messageViewHolder.itemView.context.startActivity(intent)
                        }
                    }
                    builder.show()
                } else if (userMessagesList[position].type == "image") {
                    val options =
                        arrayOf<CharSequence>(
                            "Delete For me",
                            "View This Image",
                            "Cancel"
                        )
                    val builder =
                        AlertDialog.Builder(messageViewHolder.itemView.context)
                    builder.setTitle("Delete Message")
                    builder.setItems(options) { dialogInterface, position ->
                        if (position == 0) {
                            deleteReceiverMessage(position, messageViewHolder)
                            val intent = Intent(
                                messageViewHolder.itemView.context,
                                LandingActivity::class.java
                            )
                            messageViewHolder.itemView.context.startActivity(intent)
                        } else if (position == 1) {
                            val intent = Intent(
                                messageViewHolder.itemView.context,
                                ImageViewerActivity::class.java
                            )
                            intent.putExtra(
                                "url",
                                userMessagesList[position].message
                            )
                            messageViewHolder.itemView.context.startActivity(intent)
                        }
                    }
                    builder.show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return userMessagesList.size
    }

    private fun deleteSentMessage(position: Int, holder: MessageViewHolder) {
        val rootRef = FirebaseDatabase.getInstance().reference
        rootRef.child("Messages")
            .child(userMessagesList[position].from)
            .child(userMessagesList[position].to)
            .child(userMessagesList[position].messageID)
            .removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        holder.itemView.context,
                        "Deleted Successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        holder.itemView.context,
                        "Error Occurred.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun deleteReceiverMessage(position: Int, holder: MessageViewHolder) {
        val rootRef = FirebaseDatabase.getInstance().reference
        rootRef.child("Messages")
            .child(userMessagesList[position].to)
            .child(userMessagesList[position].from)
            .child(userMessagesList[position].messageID)
            .removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        holder.itemView.context,
                        "Deleted Successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        holder.itemView.context,
                        "Error Occurred.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun deleteMessageForEveryOne(position: Int, holder: MessageViewHolder) {
        val rootRef = FirebaseDatabase.getInstance().reference
        rootRef.child("Messages")
            .child(userMessagesList[position].from)
            .child(userMessagesList[position].to)
            .child(userMessagesList[position].messageID)
            .removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    rootRef.child("Messages")
                        .child(userMessagesList[position].from)
                        .child(userMessagesList[position].to)
                        .child(userMessagesList[position].messageID)
                        .removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "Error Occurred.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        holder.itemView.context,
                        "Error Occurred.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    inner class MessageViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var senderMessageText: TextView
        var receiverMessageText: TextView
        var receiverProfileImage: CircleImageView
        var messageSenderPicture: ImageView
        var messageReceiverPicture: ImageView

        init {
            senderMessageText =
                itemView.findViewById<View>(R.id.sender_message_text) as TextView
            receiverMessageText =
                itemView.findViewById<View>(R.id.receiver_message_text) as TextView
            receiverProfileImage =
                itemView.findViewById<View>(R.id.message_profile_image) as CircleImageView
            messageReceiverPicture =
                itemView.findViewById(R.id.message_receiver_image_view)
            messageSenderPicture =
                itemView.findViewById(R.id.message_sender_image_view)
        }
    }

}