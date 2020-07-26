package com.vteam.testdemo.group

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vteam.testdemo.R

class CreateGroupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_group_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SelectUserForGroupFragment.newInstance())
                .commitNow()
        }
    }
}