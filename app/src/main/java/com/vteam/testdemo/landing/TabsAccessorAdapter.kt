package com.vteam.testdemo.landing

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.vteam.testdemo.landing.fragment.ChatsFragment
import com.vteam.testdemo.landing.fragment.ContactsFragment
import com.vteam.testdemo.landing.fragment.GroupsFragment

class TabsAccessorAdapter(fm: FragmentManager?) :
    FragmentPagerAdapter(fm!!) {
    override fun getItem(i: Int): Fragment {
        return when (i) {
            0 -> {
                ChatsFragment()
            }
            1 -> {
                GroupsFragment()
            }
            2 -> {
                ContactsFragment()
            }
            else -> ChatsFragment()
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Chats"
            1 -> "Groups"
            2 -> "Contacts"
            else -> null
        }
    }
}