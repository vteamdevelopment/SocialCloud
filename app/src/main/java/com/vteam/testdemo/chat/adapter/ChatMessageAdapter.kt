package com.vteam.testdemo.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.vteam.testdemo.R
import com.vteam.testdemo.databinding.CustomMessagesLayoutBinding
import com.vteam.testdemo.databinding.MessagesReceiverLayoutBinding
import com.vteam.testdemo.databinding.MessagesSenderLayoutBinding
import com.vteam.testdemo.top.Messages

class ChatMessageAdapter(private val userMessagesList: List<Messages>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var currentUserId: String?

    companion object{
        const val  SENDER_VIEW_HOLDER = 1
        const val  RECEIVER_VIEW_HOLDER = 2
    }

    init {
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid
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
    }

    override fun getItemViewType(position: Int): Int {

        if(userMessagesList[position].from==currentUserId){
            return SENDER_VIEW_HOLDER
        }else{
            return RECEIVER_VIEW_HOLDER
        }
    }


    inner class MessageSenderViewHolder(val binding: MessagesSenderLayoutBinding) : RecyclerView.ViewHolder(binding.root)
    inner class MessageReceiverViewHolder(val binding: MessagesReceiverLayoutBinding) : RecyclerView.ViewHolder(binding.root)


}