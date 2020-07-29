package com.vteam.testdemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.vteam.testdemo.common.NavigationUtils
import com.vteam.testdemo.common.NavigationUtils.TransactionType.Companion.REPLACE
import com.vteam.testdemo.splash.fragment.OnBoardingFragment
import com.vteam.testdemo.landing.LandingActivity


class SplashActivity : AppCompatActivity() {

    private var currentUserID: FirebaseUser?
    private var auth: FirebaseAuth

    init {

        auth = FirebaseAuth.getInstance()
        currentUserID = auth.getCurrentUser()
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