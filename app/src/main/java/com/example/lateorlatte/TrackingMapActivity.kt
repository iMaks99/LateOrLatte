package com.example.lateorlatte

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.lateorlatte.dto.Meeting
import com.example.lateorlatte.dto.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_tracking_map.*
import kotlinx.android.synthetic.main.tracking_bottom_sheet.*
import pub.devrel.easypermissions.EasyPermissions


class TrackingMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private lateinit var pref: SharedPreferences
    private lateinit var db: FirebaseFirestore

    private var participants: ArrayList<User> = ArrayList()
    private lateinit var meeting: Meeting
    private lateinit var meetingAddress: String
    private var meetingLocLat: Double? = null
    private var meetingLocLong: Double? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_map)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        meeting = intent.getParcelableExtra("meeting")!!
        //   meetingAddress = intent.getStringExtra("meetingAddress")!!
        //   meetingLocLat = intent.getDoubleExtra("meetingLocLat", 0.0)
        //   meetingLocLong = intent.getDoubleExtra("meetingLocLong", 0.0)

        pref = getSharedPreferences("lol", Context.MODE_PRIVATE)
        db = FirebaseFirestore.getInstance()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.tracking_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initComponent()
        getUsers()

        tackingParticipantFab.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun getUsers() {

        val phones = meeting.participant
        phones!!.add(meeting.creator!!)

        val adapter = TrackingParticipantsAdapter(participants, meeting.location!!)
        tracking_part_rv.adapter = adapter

        for (p in phones) {
            db.collection(User::class.java.simpleName)
                .whereEqualTo("phone", p)
                .get()
                .addOnSuccessListener {
                    for (item in it)
                        participants.add(item.toObject(User::class.java))

                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Log.w(this::class.java.name, it.localizedMessage!!)
                }
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        /*     mMap.setOnMapClickListener {
                 placeMarketOnMap(it!!)
             }*/

        setUpMap()

    }

    private fun initComponent() {

        val llBottomSheet = findViewById<LinearLayout>(R.id.tracking_bsh)

        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetCallback() {
            @SuppressLint("RestrictedApi")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            /*    if(newState == BottomSheetBehavior.STATE_HIDDEN)
                    tackingParticipantFab.visibility = View.VISIBLE
                if(newState == BottomSheetBehavior.STATE_COLLAPSED)
                    tackingParticipantFab.visibility = View.INVISIBLE*/
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })


    }


/*private fun placeMarketOnMap(location: LatLng) {
    val markerOptions = MarkerOptions().position(location)
    val titleStr = getAddress(location)
    markerOptions.title(titleStr)

    val marker = mMap.addMarker(markerOptions)
    marker.showInfoWindow()
    markers.add(marker)
    menu.findItem(R.id.undo).isVisible = true
}*/

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun setUpMap() {
        val perms = android.Manifest.permission.ACCESS_FINE_LOCATION

        if (EasyPermissions.hasPermissions(this, perms)) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    lastLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        } else {
            EasyPermissions.requestPermissions(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                LOCATION_PERMISSION_REQUEST_CODE,
                perms
            )
        }
    }
}
