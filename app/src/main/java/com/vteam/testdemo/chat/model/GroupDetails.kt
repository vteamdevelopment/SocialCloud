package com.vteam.testdemo.chat.model

data class GroupDetails(
    val id: String,
    val adminId: String,
    val adminName: String,
    val createdAt: String,
    val groupIcon: String,
    val memebers: List<String>,
    val name: String,
    val recentMessage: GroupRecentMessage
)