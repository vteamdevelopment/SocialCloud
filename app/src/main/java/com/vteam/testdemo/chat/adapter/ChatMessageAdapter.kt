package com.vteam.testdemo.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.vteam.testdemo.R
import com.vteam.testdemo.common.ConstantNodes
import com.vteam.testdemo.databinding.MessagesReceiverLayoutBinding
import com.vteam.testdemo.databinding.MessagesSenderLayoutBinding
import com.vteam.testdemo.top.Messages

class ChatMessageAdapter(private val userMessagesList: List<Messages>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var rootRef: DatabaseReference
    private var currentUserId: String?

    companion object{
        const val  SENDER_VIEW_HOLDER = 1
        const val  RECEIVER_VIEW_HOLDER = 2
    }

    init {
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        rootRef = FirebaseDatabase.getInstance().reference
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//       TODO("Not yet implemented")
//        auth = FirebaseAuth.getInstance()
        when (viewType) {
            SENDER_VIEW_HOLDER -> {
                val binding: MessagesSenderLayoutBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.messages_sender_layout,
                    parent,
                    false
                )
                return MessageSenderViewHolder(binding)
            }
            RECEIVER_VIEW_HOLDER -> {
                val binding: MessagesReceiverLayoutBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.messages_receiver_layout,
                    parent,
                    false
                )
                return MessageReceiverViewHolder(binding)
            }
        }
        val binding: MessagesSenderLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.messages_sender_layout,
            parent,
            false
        )
        return MessageSenderViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return userMessagesList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        TODO("Not yet implemented")
        val messages = userMessagesList[position]
        val fromUserID = messages.from
        val fromMessageType = messages.type
        val usersRef = rootRef.child(ConstantNodes.NODES.USER_NODE).child(fromUserID)

        when (holder) {
            is MessageSenderViewHolder -> {


                if (fromMessageType == "text") {

//                    holder.binding.messageProfileImage.visibility = View.VISIBLE
                    holder.binding.receiverMessageText.visibility = View.VISIBLE
                    holder.binding.receiverMessageText.text =
                        messages.message
                    holder.binding.receiverMessageTime.text = """${messages.time} - ${messages.date}"""

                }
            }
            is MessageReceiverViewHolder -> {


                if (fromMessageType == "text") {

                    holder.binding.receiverMessageText.visibility = View.VISIBLE
                    holder.binding.receiverMessageText.text =
                        messages.message
                    holder.binding.receiverMessageTime.text = """${messages.time} - ${messages.date}"""

                }
            }
            else -> ""
        }
    }

    override fun getItemViewType(position: Int): Int {

        return if(userMessagesList[position].from==currentUserId){
            SENDER_VIEW_HOLDER
        }else{
            RECEIVER_VIEW_HOLDER
        }
    }


    inner class MessageSenderViewHolder(val binding: MessagesSenderLayoutBinding) : RecyclerView.ViewHolder(binding.root)
    inner class MessageReceiverViewHolder(val binding: MessagesReceiverLayoutBinding) : RecyclerView.ViewHolder(binding.root)


}