package com.vteam.testdemo.group

import com.vteam.testdemo.chat.model.GroupDetails

interface OnGroupItemClick {
    fun onItemClicked(position: Int, groupId: String)
}