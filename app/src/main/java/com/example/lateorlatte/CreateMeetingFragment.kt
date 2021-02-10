package com.example.lateorlatte


import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.lateorlatte.dto.Meeting
import com.example.lateorlatte.dto.User
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class CreateMeetingFragment : Fragment() {

    companion object {
        private const val MAPS_REQUEST_CODE = 2
        private const val AUTOCOMPLETE_REQUEST_CODE = 3
    }

    private lateinit var pref: SharedPreferences
    private lateinit var db: FirebaseFirestore

    private lateinit var mPlacesClient: PlacesClient

    var creator: User? = null
    private var TAG = this.javaClass.name
    private var chips: ArrayList<Chip> = ArrayList()
    private var participants: ArrayList<String> = ArrayList()
    private lateinit var location: LatLng
    private lateinit var address: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_meeting, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pref = activity!!.getSharedPreferences("lol", Context.MODE_PRIVATE)

        if (!Places.isInitialized()) {
            Places.initialize(context!!, getString(R.string.google_maps_key))
        }

        mPlacesClient = Places.createClient(context!!)


        db = FirebaseFirestore.getInstance()
        db.collection(User::class.java.simpleName)
            .whereEqualTo("phone", pref.getString("phone", ""))
            .get()
            .addOnSuccessListener { querySnapshot ->
                creator = querySnapshot.documents[0].toObject(User::class.java)
            }

        val participantCh = view.findViewById<ChipGroup>(R.id.new_meeting_phones_chg)
        val addParticipant = view.findViewById<ImageView>(R.id.new_meeting_add_participant_iv)
        addParticipant.setOnClickListener {
            val participantEv = view.findViewById<EditText>(R.id.new_meeting_phone_et)
            val participant = participantEv.text.toString()

            if (participant != creator?.phone) {

                db.collection(User::class.java.simpleName)
                    .whereEqualTo("phone", participant)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val v = LayoutInflater.from(context)
                                .inflate(R.layout.meeting_participant_create_chip, participantCh, false)
                            val chip = v.findViewById<Chip>(R.id.new_meeting_chip)
                            chip.text = participant
                            chip.isClickable = true
                            chip.isCloseIconVisible = true
                            chip.setOnCloseIconClickListener {
                                participantCh.removeView(chip)
                                chips.remove(chip)
                                participants.remove(chip.text.toString())
                            }
                            participantCh.addView(chip)
                            chips.add(chip)
                            participants.add(participant)
                            participantEv.setText("")
                        } else {

                            participantEv.hideKeyboard()
                            participantEv.setText("")
                            Snackbar.make(
                                view,
                                "Нет пользователя с таким номером",
                                Snackbar.LENGTH_LONG
                            ).show()

                        }
                    }.addOnFailureListener {
                        Log.w(TAG, "")

                    }
            }
        }

        val c = Calendar.getInstance()

        val date = view.findViewById<EditText>(R.id.new_meeting_date_ev)
        date.setOnClickListener {
            val day = c.get(Calendar.DAY_OF_MONTH)
            val month = c.get(Calendar.MONTH)
            val year = c.get(Calendar.YEAR)

            val dpd = DatePickerDialog(view.context, OnDateSetListener { _, y, m, d ->
                date.setText("$d.${m + 1}.$y")
            }, year, month, day)

            dpd.show()
        }

        val time = view.findViewById<EditText>(R.id.new_meeting_time_ev)
        time.setOnClickListener {
            val hour = c.get(Calendar.HOUR)
            val minute = c.get(Calendar.MINUTE)
            val tpd = TimePickerDialog(view.context, TimePickerDialog.OnTimeSetListener { _, h, m ->
                time.setText("$h:$m")
            }, hour, minute, true)

            tpd.show()
        }

        address = view.findViewById(R.id.new_meeting_address_ev)
        address.setOnClickListener {
            onSearchCalled()
        }

        view.findViewById<TextView>(R.id.new_meeting_choose_on_map)
            .setOnClickListener {
                val intent = Intent(activity, MapsActivity::class.java)
                startActivityForResult(intent, MAPS_REQUEST_CODE)
            }

        val submit = view.findViewById<Button>(R.id.new_meeting_create_meeting_btn)
        submit.setOnClickListener {
            if (chips.size == 0 || date.text.isEmpty() || time.text.isEmpty() || address.text.isEmpty()) {
                Snackbar.make(view, "Заполните все поля!", Snackbar.LENGTH_LONG).show()
            } else {


                val meeting = Meeting(
                    address.text.toString(),
                    GeoPoint(location.latitude, location.longitude),
                    creator!!.id,
                    creator!!.phone,
                    SimpleDateFormat("dd.MM.yyyy").parse(date.text.toString()),
                    SimpleDateFormat("HH:mm").parse(time.text.toString()),
                    participants
                )
                db.collection(Meeting::class.java.simpleName)
                    .add(meeting)
                    .addOnSuccessListener { documentReference ->
                        Snackbar.make(view, "Событие создано!", Snackbar.LENGTH_SHORT).show()
                        Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding document", e)
                    }
            }
        }
    }

    private fun onSearchCalled() {
        val fields =
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )

        val intent = Autocomplete
            .IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .setCountry("RU")
            .build(context!!)

        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                Log.i(this::class.java.name, "${place.name}, ${place.id}, ${place.address}")
                address.setText(place.address!!)
                location = place.latLng!!

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data!!)
                Log.e(this::class.java.name, status.statusMessage!!)
                Snackbar.make(
                    view!!,
                    "Error!",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        } else if (requestCode == MAPS_REQUEST_CODE && resultCode == RESULT_OK) {
            address.setText(data!!.extras!!.getString("address"))
            location = data.extras!!.getParcelable("location")!!
        }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}
