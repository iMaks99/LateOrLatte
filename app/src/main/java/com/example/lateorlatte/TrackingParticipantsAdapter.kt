package com.example.lateorlatte

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lateorlatte.dto.User
import com.google.firebase.firestore.GeoPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class TrackingParticipantsAdapter(var participants: List<User>, var meetingLocation: GeoPoint) :
    RecyclerView.Adapter<TrackingParticipantsAdapter.ViewHolder>() {

    private lateinit var context: Context

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val status = itemView.findViewById<ImageView>(R.id.tracking_item_status)!!
        val username = itemView.findViewById<TextView>(R.id.tracking_item_user)!!
        val dist = itemView.findViewById<TextView>(R.id.tracking_item_dist)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.tracking_participant_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return participants.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (participants.size > position) {
            holder.username.text = participants[position].phone
            /*    holder.dist.text = distance(
            participants[position].location!!.latitude,
            participants[position].location!!.longitude,
            meetingLocation.latitude,
            meetingLocation.longitude
        ) */
        }
    }

    private fun distance(
        lat_a: Double,
        lng_a: Double,
        lat_b: Double,
        lng_b: Double
    ): String {
        val earthRadius = 3958.75
        val latDiff = Math.toRadians(lat_b - lat_a)
        val lngDiff = Math.toRadians(lng_b - lng_a)

        val a = sin(latDiff / 2) * sin(latDiff / 2) +
                cos(Math.toRadians(lat_a)) *
                cos(Math.toRadians(lat_b)) *
                sin(lngDiff / 2) * sin(lngDiff / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        val distance = earthRadius * c
        val meterConversion = 1609.0

        return (distance * meterConversion).toString()
    }
}