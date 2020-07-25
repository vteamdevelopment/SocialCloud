package com.vteam.testdemo

import android.content.Intent
import android.os.Bundle
import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.vteam.testdemo.common.NavigationUtils
import com.vteam.testdemo.common.NavigationUtils.TransactionType.Companion.REPLACE
import com.vteam.testdemo.splash.fragment.OnBoardingFragment
import com.vteam.testdemo.landing.LandingActivity


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
            finish()
        } else
            if (savedInstanceState == null) {
                NavigationUtils.addFragment(
                    OnBoardingFragment.newInstance(),
                    REPLACE,
                    OnBoardingFragment.javaClass.simpleName,
                    R.id.container,supportFragmentManager = supportFragmentManager
                )
            }
    }





}