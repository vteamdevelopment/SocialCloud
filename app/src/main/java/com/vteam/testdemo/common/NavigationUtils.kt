package com.vteam.testdemo.common

import android.os.Bundle
import androidx.annotation.IntDef
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.vteam.testdemo.common.NavigationUtils.TransactionType.Companion.ADD
import com.vteam.testdemo.common.NavigationUtils.TransactionType.Companion.ADD_WITH_BACKSTACK
import com.vteam.testdemo.common.NavigationUtils.TransactionType.Companion.REPLACE
import com.vteam.testdemo.common.NavigationUtils.TransactionType.Companion.REPLACE_WITH_BACKSTACK

object NavigationUtils {
    fun addFragment(
        fragment: Fragment,
        @TransactionType transactionType: Int,
        tag: String,
        frameContainer: Int,
        bundle: Bundle? = null,
        supportFragmentManager : FragmentManager
    ) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        if (bundle != null) fragment.arguments = bundle
        when (transactionType) {
            ADD -> {
                fragmentTransaction.add(frameContainer, fragment)
            }
            ADD_WITH_BACKSTACK -> {
                fragmentTransaction.add(frameContainer, fragment)
                fragmentTransaction.addToBackStack(tag)
                val currentFragment = fragmentManager.findFragmentById(frameContainer)
                currentFragment?.let { fragmentTransaction.hide(it) }
            }
            REPLACE -> {
                fragmentTransaction.replace(frameContainer, fragment)
            }
            REPLACE_WITH_BACKSTACK -> {
                fragmentTransaction.replace(frameContainer, fragment)
                fragmentTransaction.addToBackStack(tag)
                val currentFragment = fragmentManager.findFragmentById(frameContainer)
                currentFragment?.let { fragmentTransaction.hide(it) }
            }
        }
        fragmentTransaction.commitNowAllowingStateLoss()

    }


    @IntDef(ADD, ADD_WITH_BACKSTACK, REPLACE, REPLACE_WITH_BACKSTACK)
    annotation class TransactionType {
        companion object {
            const val ADD = 1
            const val ADD_WITH_BACKSTACK = 2
            const val REPLACE = 3
            const val REPLACE_WITH_BACKSTACK = 4
        }
    }

}