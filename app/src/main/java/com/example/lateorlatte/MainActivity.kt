package com.example.lateorlatte

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pref = getSharedPreferences("lol", Context.MODE_PRIVATE)

        if (pref.getString("phone", "")!!.isEmpty())
            showFragment(RegistrationFragment())
        else
            showFragment(MenuFragment())
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_content, fragment, fragment.javaClass.simpleName)
            .commit()
    }
}
