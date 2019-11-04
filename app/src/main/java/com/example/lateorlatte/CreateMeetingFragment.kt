package com.example.lateorlatte


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.lateorlatte.dto.User
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A simple [Fragment] subclass.
 */
class CreateMeetingFragment : Fragment() {

    private lateinit var pref: SharedPreferences
    private lateinit var db: FirebaseFirestore
    var creator: User? = null
    private var TAG = this.javaClass.name

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_meeting, container, false)
    }

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
                            val participantCh =
                                view.findViewById<ChipGroup>(R.id.new_meeting_phones_chg)

                            val v = LayoutInflater.from(context).inflate(R.layout.new_meeting_chip, participantCh, false)
                            val chip = v.findViewById<Chip>(R.id.new_meeting_chip)
                            chip.text = participant
                            chip.isClickable = true
                            chip.isCloseIconVisible = true
                            chip.setOnCloseIconClickListener {
                                participantCh.removeView(chip)
                            }
                            participantCh.addView(chip)
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
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}
