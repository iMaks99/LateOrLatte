package com.example.lateorlatte

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.lateorlatte.dto.Meeting
import com.example.lateorlatte.dto.User
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_tracking_map.*
import kotlinx.android.synthetic.main.tracking_bottom_sheet.*
import pub.devrel.easypermissions.EasyPermissions


class TrackingMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    private lateinit var pref: SharedPreferences
    private lateinit var db: FirebaseFirestore

    private var markers: HashMap<String, Marker> = HashMap()
    private var participants: HashMap<String, User> = HashMap()
    private lateinit var meeting: Meeting
    private lateinit var adapter: TrackingParticipantsAdapter

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_map)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        meeting = intent.getParcelableExtra("meeting")!!

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

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation

                getParticipants()

                db.collection(User::class.java.simpleName)
                    .document(meeting.creatorId!!)
                    .update("location", GeoPoint(lastLocation.latitude, lastLocation.longitude))
                    .addOnSuccessListener {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Successfully updated",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
            }
        }

        createLocationRequest()
    }

    private fun getUsers() {

        val phones = meeting.participant
        phones!!.add(meeting.creator!!)

        adapter = TrackingParticipantsAdapter(participants, meeting.location!!)
        tracking_part_rv.adapter = adapter

        for (p in phones) {
            db.collection(User::class.java.simpleName)
                .whereEqualTo("phone", p)
                .get()
                .addOnSuccessListener {
                    for (item in it)
                        participants[item.id] = item.toObject(User::class.java)

                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Log.w(this::class.java.name, it.localizedMessage!!)
                }
        }
    }

    private fun getParticipants() {
        for (p in meeting.participant!!) {
            db.collection(User::class.java.simpleName)
                .whereEqualTo("phone", p)
                .get()
                .addOnSuccessListener {
                    for (item in it) {
                        val temp = item.toObject(User::class.java).withId<User>(item.id)

                        if (temp.location != null) {
                            if (!markers.containsKey(temp.id))
                                placeMarketOnMap(
                                    LatLng(temp.location!!.latitude, temp.location!!.longitude),
                                    temp.name!!, temp.id!!
                                )
                            else
                                markers[item.id]!!.position =
                                    LatLng(temp.location!!.latitude, temp.location!!.longitude)

                            participants[item.id]!!.location = temp.location
                        }
                    }

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

        setUpMap()
    }

    private fun initComponent() {

        val llBottomSheet = findViewById<LinearLayout>(R.id.tracking_bsh)

        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetCallback() {
            @SuppressLint("RestrictedApi")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }


    private fun placeMarketOnMap(location: LatLng, title: String, id: String) {
        val markerOptions = MarkerOptions().position(location)
        markerOptions.title(title)

        val marker = mMap.addMarker(markerOptions)
        marker.showInfoWindow()
        markers[id] = marker
    }

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

            placeMarketOnMap(
                LatLng(meeting.location!!.latitude, meeting.location!!.longitude),
                meeting.address.toString(),
                ""
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                LOCATION_PERMISSION_REQUEST_CODE,
                perms
            )
        }
    }

    private fun startLocationUpdates() {
        val perms = android.Manifest.permission.ACCESS_FINE_LOCATION

        if (EasyPermissions.hasPermissions(this, perms)) {

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                LOCATION_PERMISSION_REQUEST_CODE,
                perms
            )
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()

        locationRequest.interval = 60000
        locationRequest.fastestInterval = 60000

        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        this,
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }


}
