package com.example.lateorlatte

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.lateorlatte.dto.Meeting
import com.example.lateorlatte.dto.User
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class TrackingParticipantsAdapter(
    var participants: HashMap<String, User>,
    var meeting: Meeting
) :
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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (participants.size > position) {
            holder.username.text = participants.values.toList()[position].phone

            if (participants.values.toList()[position].location != null) {

                val dist = distance(
                    participants.values.toList()[position].location!!.latitude,
                    participants.values.toList()[position].location!!.longitude,
                    meeting.location!!.latitude,
                    meeting.location!!.longitude
                )
                holder.dist.text = "%.2f м.".format(dist)

                if (dist < 25) {
                    val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val currentDate = sdfDate.format(Date())
                    val meetingDate = sdfDate.format(meeting.date!!)

                    val sdfTime = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val currentTime = sdfTime.format(Date())
                    val meetingTime = sdfTime.format(meeting.time!!)

                    if (currentDate == meetingDate && currentTime >= meetingTime) {
                        val builder = AlertDialog.Builder(context)

                        builder.setTitle("Вы добрались до места!")

                        builder.setMessage("Вы добрались до места вовремя! Получите свою скидку у наших партнеров")
                        builder.setIcon(android.R.drawable.ic_dialog_alert)

                        builder.setPositiveButton("Получить ссылку"){ _, _ ->
                            Snackbar.make((context as TrackingMapActivity).findViewById(android.R.id.content),"clicked yes",Snackbar.LENGTH_LONG).show()

                            val intent = Intent(context, PartnersMapActivity::class.java)
                            context.startActivity(intent)
                        }

                        builder.setNeutralButton("Не сейчас"){ _, _ ->
                            Snackbar.make((context as TrackingMapActivity).findViewById(android.R.id.content),"clicked cancel\n operation cancel",Snackbar.LENGTH_LONG).show()
                        }

                        val alertDialog: AlertDialog = builder.create()

                        alertDialog.setCancelable(false)
                        alertDialog.show()
                    } else if (currentDate == meetingDate && currentTime < meetingTime) {
                        val builder = AlertDialog.Builder(context)

                        builder.setTitle("Вы добрались до места!")

                        builder.setMessage("Вы опоздали! К сожалению скидку вы сможете получить")
                        builder.setIcon(android.R.drawable.ic_dialog_alert)

                        builder.setPositiveButton("Окей"){ _, _ ->
                            Snackbar.make((context as TrackingMapActivity).findViewById(android.R.id.content),"clicked yes",Snackbar.LENGTH_LONG).show()

                            /*val intent = Intent(context, PartnersMapActivity::class.java)
                            context.startActivity(intent)*/
                        }

                       /* builder.setNeutralButton("Не сейчас"){ _, _ ->
                            Snackbar.make((context as TrackingMapActivity).findViewById(android.R.id.content),"clicked cancel\n operation cancel",Snackbar.LENGTH_LONG).show()
                        }*/

                        val alertDialog: AlertDialog = builder.create()

                        alertDialog.setCancelable(false)
                        alertDialog.show()
                    }
                }

            }
        }
    }

    private fun distance(
        lat_a: Double,
        lng_a: Double,
        lat_b: Double,
        lng_b: Double
    ): Double {
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

        return distance * meterConversion
    }
}