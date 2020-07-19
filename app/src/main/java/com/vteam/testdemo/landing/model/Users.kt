package com.vteam.testdemo.landing.model

data class Users(var uId : String?=null, var name:String? =null, var image: String?=null, var status : String?=null, var userStatus: UserStatus?=null ) {
    constructor(uid : String) : this(uId= uid) {
        this.uId= uid
    }
    constructor() : this(null) {

    }
}