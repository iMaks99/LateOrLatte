package com.example.lateorlatte


import android.annotation.SuppressLint
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
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.lateorlatte.dto.User
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class CreateMeetingFragment : Fragment() {

    private lateinit var pref: SharedPreferences
    private lateinit var db: FirebaseFirestore
    var creator: User? = null
    private var TAG = this.javaClass.name
    private var chips: ArrayList<Chip> = ArrayList()

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
                                .inflate(R.layout.new_meeting_chip, participantCh, false)
                            val chip = v.findViewById<Chip>(R.id.new_meeting_chip)
                            chip.text = participant
                            chip.isClickable = true
                            chip.isCloseIconVisible = true
                            chip.setOnCloseIconClickListener {
                                participantCh.removeView(chip)
                                chips.remove(chip)
                            }
                            participantCh.addView(chip)
                            chips.add(chip)
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
                date.setText("$d.$m.$y")
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

        val address = view.findViewById<EditText>(R.id.new_meeting_address_ev)
        address.setOnClickListener {
      /*      activity!!.supportFragmentManager.beginTransaction()
                .addToBackStack(TAG)
                .replace(R.id.main_content, MapsFragment())
                .commit()*/

            val intent = Intent(activity, MapsActivity::class.java)
            startActivity(intent)
        }

        val submit = view.findViewById<Button>(R.id.new_meeting_create_meeting_btn)
        submit.setOnClickListener {
            if (chips.size == 0 || date.text.isEmpty() || time.text.isEmpty() || address.text.isEmpty()) {
                Snackbar.make(view, "Заполните все поля!", Snackbar.LENGTH_LONG).show()
            } else {
              //  val meeting = Meeting("Starbucks на Арбате",
                    //    Location("55.7505936", "37.5954206"),

           //     )
             //   db.collection(Meeting::class.java.simpleName)
              //      .add()
            }
        }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}
