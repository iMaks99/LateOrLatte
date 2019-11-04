package com.example.lateorlatte


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lateorlatte.dto.User
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A simple [Fragment] subclass.
 */
class RegistrationFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var pref: SharedPreferences
    private var TAG = this.javaClass.name

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.hide()
        activity?.actionBar?.hide()

        db = FirebaseFirestore.getInstance()
        pref = activity!!.getSharedPreferences("lol", Context.MODE_PRIVATE)

        val email = view.findViewById<EditText>(R.id.registration_email_et)
        val phone = view.findViewById<EditText>(R.id.registration_phone_et)
        val submit = view.findViewById<Button>(R.id.registration_confirm_bt)

        submit.setOnClickListener { v ->
            if (phone.text.isEmpty())
                Toast.makeText(activity, "Заполните все выделенные поля", Toast.LENGTH_SHORT).show()
            else {
                db.collection(User::class.java.simpleName)
                    .whereEqualTo("phone", phone.text.toString())
                    .get()
                    .addOnSuccessListener { queryDocumentSnapshot ->
                        if (queryDocumentSnapshot.isEmpty)
                            db.collection(User::class.java.simpleName).document()
                                .set(User(null, email.text.toString(), phone.text.toString()))

                        pref.edit().putString("phone", phone.text.toString()).apply()

                        Toast.makeText(activity, "Успех!", Toast.LENGTH_SHORT).show()

                        activity!!.supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.main_content, MenuFragment(), MenuFragment::class.java.simpleName)
                            .commit()
                    }
                    .addOnFailureListener { exception ->
                        Log.w(
                            TAG,
                            "User registration error: ",
                            exception
                        )
                    }
            }
        }
    }
}
