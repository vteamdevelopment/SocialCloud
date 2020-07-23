package com.vteam.testdemo.common

object Utils {
    @JvmStatic
    fun setOneToOneChat(uid1: String, uid2: String): String {
        return if (uid1.compareTo(uid2) < 0) {
            uid1 + uid2
        } else {
            uid2 + uid1
        }
    }
}