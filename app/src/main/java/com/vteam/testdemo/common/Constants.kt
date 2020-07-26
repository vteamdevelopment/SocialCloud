package com.vteam.testdemo.common

class Constants {
    object KEY {

        const val GROUP_ID ="GROUP_ID"
        const val MOBILE_NUMBER = "MOBILE_NUMBER"
        const val COUNTRY_CODE = "COUNTRY_CODE"
        const val SELECTED_USER = "SELECTED_USER"
    }


    object USER_STATE{
        const val OFFLINE = "Offline"
        const val ONLINE = "Online"
    }

    object PATTERN{

    const val PATTERN_MMM_DD_yyyy = "MMM dd, yyyy"
      const val PATTERN_hh_mm_a = "hh:mm a"
    }

    object NODES{
        const val USER_NODE = "Users"
        const val CHAT_NODE = "ChatNode"
        const val MESSAGES_NODE = "MessagesNode"
        const val GROUP_DETAILS = "GroupDetails"
        const val GROUP_MESSAGES = "GroupMessages"

    }

    object CHILD_NODES{
        const val GROUPS = "Groups"

    }


}