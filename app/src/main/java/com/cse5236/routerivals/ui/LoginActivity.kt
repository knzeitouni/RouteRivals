package com.cse5236.routerivals.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cse5236.routerivals.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Show LoginFragment the first time
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.loginFragmentContainer, LoginFragment())
                .commit()
        }
    }
}
