package com.example.lateorlatte


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.lateorlatte.dto.Meeting
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_event_info.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class MeetingInfoFragment : Fragment() {

    private lateinit var pref: SharedPreferences
    private var meeting: Meeting? = null

    companion object {
        fun newInstance(meeting: Meeting): MeetingInfoFragment {
            val args = Bundle()
            args.putParcelable("meeting", meeting)
            val fragment = MeetingInfoFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_info, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pref = activity!!.getSharedPreferences("lol", Context.MODE_PRIVATE)

        meeting = arguments!!.getParcelable("meeting")

        if (meeting != null) {

            meeting_organizer_chip.text = meeting!!.creator

            for (item in meeting!!.participant!!) {
                val v = LayoutInflater.from(context)
                    .inflate(
                        R.layout.meeting_participant_view_chip,
                        meeting_participants_chg,
                        false
                    )
                val chip = v.findViewById<Chip>(R.id.meeting_chip)
                chip.text = item
                meeting_participants_chg.addView(chip)
            }

            meeting_time_tv.text =
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(meeting!!.date!!) +
                        " " +
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(meeting!!.time!!)

            meeting_place_tv.text = meeting!!.address

            meeting_go_btn.setOnClickListener {
                val intent = Intent(activity, TrackingMapActivity::class.java)
                intent.putExtra("meeting", meeting!!)
          //      intent.putExtra("meetingAddress", meeting!!)
        /*        intent.putExtra("meetingLocLat", meeting!!.location!!.latitude)
                intent.putExtra("meetingLocLong", meeting!!.location!!.longitude)*/
                startActivity(intent)
            }
        }
    }
}
