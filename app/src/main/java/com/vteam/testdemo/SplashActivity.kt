package com.vteam.testdemo

import android.content.Intent
import android.os.Bundle
import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.vteam.testdemo.SplashActivity.TransactionType.Companion.ADD
import com.vteam.testdemo.SplashActivity.TransactionType.Companion.ADD_WITH_BACKSTACK
import com.vteam.testdemo.SplashActivity.TransactionType.Companion.REPLACE
import com.vteam.testdemo.SplashActivity.TransactionType.Companion.REPLACE_WITH_BACKSTACK
import com.vteam.testdemo.splash.fragment.OnBoardingFragment
import com.vteam.testdemo.top.LandingActivity
import kotlinx.android.synthetic.main.item_list_content.*


class SplashActivity : AppCompatActivity() {

    private var currentUserID: FirebaseUser?
    private var mAuth: FirebaseAuth

    init {

        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.getCurrentUser()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)


        if (currentUserID != null) {

            val intent = Intent(this, LandingActivity::class.java)
            startActivity(intent)
        } else
            if (savedInstanceState == null) {
                addFragment(
                    OnBoardingFragment.newInstance(),
                    REPLACE,
                    OnBoardingFragment.javaClass.simpleName,
                    R.id.container
                )
            }
    }



    fun addFragment( fragment: Fragment,@TransactionType transactionType :  Int, tag: String, frameContainer:Int, bundle: Bundle? = null) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        if(bundle != null )fragment.arguments = bundle
        when(transactionType){
            ADD ->{
                fragmentTransaction.add(frameContainer, fragment)
            }
            ADD_WITH_BACKSTACK ->{
                fragmentTransaction.add(frameContainer, fragment)
                fragmentTransaction.addToBackStack(tag)
                val currentFragment  =  fragmentManager.findFragmentById(frameContainer)
                currentFragment?.let { fragmentTransaction.hide(it) }
            }
            REPLACE ->{
                fragmentTransaction.replace(frameContainer, fragment)
            }
            REPLACE_WITH_BACKSTACK ->{
                fragmentTransaction.replace(frameContainer, fragment)
                fragmentTransaction.addToBackStack(tag)
                val currentFragment  =  fragmentManager.findFragmentById(frameContainer)
                currentFragment?.let { fragmentTransaction.hide(it) }
            }
        }
        fragmentTransaction.commitNowAllowingStateLoss()

    }


    @IntDef(ADD, ADD_WITH_BACKSTACK, REPLACE, REPLACE_WITH_BACKSTACK)
    annotation class TransactionType {
        companion object {
           const val ADD= 1
           const val ADD_WITH_BACKSTACK= 2
           const val REPLACE = 3
           const val REPLACE_WITH_BACKSTACK=4
        }
    }
}