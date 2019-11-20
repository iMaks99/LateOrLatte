package com.example.lateorlatte

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pref = getSharedPreferences("lol", Context.MODE_PRIVATE)

        if (pref.getString("phone", "")!!.isEmpty())
            showFragment(supportFragmentManager, RegistrationFragment())
        else
            showFragment(supportFragmentManager, MenuFragment())
    }


}
