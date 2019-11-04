package com.example.lateorlatte


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

/**
 * A simple [Fragment] subclass.
 */
class MenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newMeeting = view.findViewById<Button>(R.id.menu_new_meeting_btn)
        newMeeting.setOnClickListener { v ->
            showFragment(CreateMeetingFragment())
        }
    }

    private fun showFragment(fragment: Fragment) {
        activity!!.supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_content, fragment, fragment.javaClass.simpleName)
            .commit()
    }
}
