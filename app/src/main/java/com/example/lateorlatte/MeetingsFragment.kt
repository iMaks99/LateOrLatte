package com.example.lateorlatte


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.lateorlatte.dto.Meeting
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A simple [Fragment] subclass.
 */
class MeetingsFragment : Fragment() {

    private lateinit var pref: SharedPreferences
    private lateinit var db: FirebaseFirestore
    private var meetingsTemp: ArrayList<Meeting> = ArrayList()
    private var meetings: List<Meeting>? = null
    private lateinit var adapter: MeetingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_meetings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.title = "Мои встречи"

        pref = activity!!.getSharedPreferences("lol", Context.MODE_PRIVATE)

        db = FirebaseFirestore.getInstance()
        db.collection(Meeting::class.java.simpleName)
            .get()
            .addOnSuccessListener {
                meetingsTemp.clear()
                for (i in it.documents)
                    meetingsTemp.add(i.toObject(Meeting::class.java)!!)

                meetings = meetingsTemp.filter { m ->
                    m.creator == pref.getString("phone", "") ||
                            m.participant!!.contains(pref.getString("phone", ""))
                }

                if (meetings != null) {
                    adapter = MeetingsAdapter(meetings!!)
                    view.findViewById<RecyclerView>(R.id.meeting_rv).adapter = adapter
                }
            }
            .addOnFailureListener {
                Log.w(this::class.java.name, it.localizedMessage!!)
            }
    }
}
