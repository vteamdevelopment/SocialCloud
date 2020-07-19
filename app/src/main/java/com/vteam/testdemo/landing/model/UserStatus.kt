package com.vteam.testdemo.landing.model

data class UserStatus(var status: String? = null,var date:String?= null, var time : String?= null) {
    constructor() : this(null) {
    }
}