package com.vteam.testdemo.chat.model

data class GroupRecentMessage(
    val chatMessageType: String,
    val chatType: String,
    val contentType: String,
    val file: String,
    val fontType: String,
    val fromId: String,
    val image: String,
    val message: String,
    val status: Int,
    val timestamp: String,
    val toId: String,
    val video: String
) {

}
