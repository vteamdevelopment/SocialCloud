package com.vteam.testdemo.chat.model

data class GroupDetails(
    var id: String,
    var adminId: String?=null,
    var adminName: String?=null,
    var createdAt: String?=null,
    var groupIcon: String?=null,
    var memebers: List<String?>?=null,
    var name: String?=null,
    var recentMessage: GroupRecentMessage?=null
){
    constructor(uid: String) : this(id=uid) {
        this.id =uid
    }
}