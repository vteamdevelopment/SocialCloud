package com.vteam.testdemo.chat.adapter

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.vteam.testdemo.R
import com.vteam.testdemo.chat.adapter.MessageAdapter.MessageViewHolder
import com.vteam.testdemo.common.ConstantNodes
import com.vteam.testdemo.databinding.CustomMessagesLayoutBinding
import com.vteam.testdemo.landing.LandingActivity
import com.vteam.testdemo.top.ImageViewerActivity
import com.vteam.testdemo.top.Messages

class MessageAdapter(private val userMessagesList: List<Messages>) : RecyclerView.Adapter<MessageViewHolder>() {

    private var auth: FirebaseAuth? = null
    private var usersRef: DatabaseReference? = null



    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MessageViewHolder {
        val binding: CustomMessagesLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(viewGroup.context),
            R.layout.custom_messages_layout,
            viewGroup,
            false
        )

        auth = FirebaseAuth.getInstance()
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(
        messageViewHolder: MessageViewHolder,
        position: Int
    ) {
        val messageSenderId = auth!!.currentUser!!.uid
        val messages = userMessagesList[position]
        val fromUserID = messages.from
        val fromMessageType = messages.type


        usersRef = FirebaseDatabase.getInstance().reference.child(ConstantNodes.NODES.USER_NODE).child(fromUserID)
        usersRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    val receiverImage =
                        dataSnapshot.child("image").value.toString()
                    if (receiverImage.isNotEmpty()) {
                        val reference =
                            FirebaseStorage.getInstance().reference
                        reference.child(receiverImage).downloadUrl
                            .addOnSuccessListener { uri -> // Got the download URL for 'users/me/profile.png'
                                Log.d("URL", "" + uri)
                                Glide.with(messageViewHolder.binding.messageReceiverImageView.context)
                                    .load(uri)
                                    .into(messageViewHolder.binding.messageReceiverImageView)
                            }.addOnFailureListener {
                                // Handle any errors
                            }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        messageViewHolder.binding.receiverMessageText.visibility = View.GONE
        messageViewHolder.binding.messageReceiverImageView.visibility = View.GONE
        messageViewHolder.binding.senderMessageText.visibility = View.GONE
        messageViewHolder.binding.messageSenderImageView.visibility = View.GONE
        messageViewHolder.binding.messageReceiverImageView.visibility = View.GONE
        if (fromMessageType == "text") {
            if (fromUserID == messageSenderId) {
                messageViewHolder.binding.senderMessageText.visibility = View.VISIBLE
                messageViewHolder.binding.senderMessageText.setBackgroundResource(R.drawable.receiver_messages_layout)
                messageViewHolder.binding.senderMessageText.setTextColor(Color.BLACK)
                messageViewHolder.binding.senderMessageText.text = """${messages.message}

${messages.time} - ${messages.date}"""
            } else {
                messageViewHolder.binding.messageReceiverImageView.visibility = View.VISIBLE
                messageViewHolder.binding.receiverMessageText.visibility = View.VISIBLE
                messageViewHolder.binding.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout)
                messageViewHolder.binding.receiverMessageText.setTextColor(Color.BLACK)
                messageViewHolder.binding.receiverMessageText.text = """${messages.message}

${messages.time} - ${messages.date}"""
            }
        } else if (fromMessageType == "image") {
            if (fromUserID == messageSenderId) {
                messageViewHolder.binding.messageSenderImageView.visibility = View.VISIBLE
                if (messages.message.isNotEmpty()) {
                    val reference = FirebaseStorage.getInstance().reference
                    reference.child(messages.message).downloadUrl
                        .addOnSuccessListener { uri -> // Got the download URL for 'users/me/profile.png'
                            Log.d("URL", "" + uri)
                            Glide.with(messageViewHolder.binding.messageSenderImageView.context)
                                .load(uri)
                                .into(messageViewHolder.binding.messageSenderImageView)
                        }.addOnFailureListener {
                            // Handle any errors
                        }
                }
            } else {
                messageViewHolder.binding.messageReceiverImageView.visibility = View.VISIBLE
                messageViewHolder.binding.messageReceiverImageView.visibility = View.VISIBLE
                if (messages.message.isNotEmpty()) {
                    val reference = FirebaseStorage.getInstance().reference
                    reference.child(messages.message).downloadUrl
                        .addOnSuccessListener { uri -> // Got the download URL for 'users/me/profile.png'
                            Log.d("URL", "" + uri)
                            Glide.with(messageViewHolder.binding.messageReceiverImageView.context)
                                .load(uri)
                                .into(messageViewHolder.binding.messageReceiverImageView)
                        }.addOnFailureListener {
                            // Handle any errors
                        }
                }
            }
        } else if (fromMessageType == "pdf" || fromMessageType == "docx") {
            if (fromUserID == messageSenderId) {
                messageViewHolder.binding.messageSenderImageView.visibility = View.VISIBLE
                //                messageViewHolder.messageSenderPicture.setBackground(R.drawable.);
                messageViewHolder.itemView.setOnClickListener {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(userMessagesList[position].message)
                    )
                    messageViewHolder.itemView.context.startActivity(intent)
                }
            } else {
                messageViewHolder.binding.messageReceiverImageView.visibility = View.VISIBLE
                messageViewHolder.binding.messageReceiverImageView.visibility = View.VISIBLE
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
                        when (position) {
                            0 -> {
                                deleteSentMessage(position, messageViewHolder)
                                val intent = Intent(
                                    messageViewHolder.itemView.context,
                                    LandingActivity::class.java
                                )
                                messageViewHolder.itemView.context.startActivity(intent)
                            }
                            1 -> {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(
                                        userMessagesList[position].message
                                    )
                                )
                                messageViewHolder.itemView.context.startActivity(intent)
                            }
                            3 -> {
                                deleteMessageForEveryOne(position, messageViewHolder)
                            }
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
                        when (position) {
                            0 -> {
                                deleteSentMessage(position, messageViewHolder)
                                val intent = Intent(
                                    messageViewHolder.itemView.context,
                                    LandingActivity::class.java
                                )
                                messageViewHolder.itemView.context.startActivity(intent)
                            }
                            1 -> {
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
                            3 -> {
                                deleteMessageForEveryOne(position, messageViewHolder)
                            }
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

    inner class MessageViewHolder(val binding: CustomMessagesLayoutBinding) : RecyclerView.ViewHolder(binding.root)

}