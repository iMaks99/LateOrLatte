package com.example.lateorlatte

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

fun showFragment(fragmentManager: FragmentManager, fragment: Fragment) {
    fragmentManager
        .beginTransaction()
        .addToBackStack(null)
        .replace(R.id.main_content, fragment, fragment.javaClass.simpleName)
        .commit()
}