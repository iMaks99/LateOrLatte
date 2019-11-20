package com.example.lateorlatte

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lateorlatte.dto.Meeting
import java.text.SimpleDateFormat
import java.util.*

class MeetingsAdapter(var meetings: List<Meeting>) :
    RecyclerView.Adapter<MeetingsAdapter.ViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        context = parent.context

        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.meeting_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return meetings.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.eventAddress.text = meetings[position].address
        holder.eventParticipantCount.text = "${meetings[position].participant!!.size + 1} участников"
        holder.eventDate.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(meetings[position].date!!) + " " +
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(meetings[position].time!!)

        holder.itemView.setOnClickListener {
            showFragment((context as MainActivity).supportFragmentManager, MeetingInfoFragment.newInstance(meetings[position]))
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventAddress = itemView.findViewById<TextView>(R.id.event_address)!!
        val eventParticipantCount = itemView.findViewById<TextView>(R.id.event_part_count)!!
        val eventDate = itemView.findViewById<TextView>(R.id.event_date)!!
    }
}