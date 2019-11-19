package com.example.lateorlatte

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity.RESULT_ERROR
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import java.io.IOException


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val AUTOCOMPLETE_REQUEST_CODE = 3

    }

    private lateinit var mMap: GoogleMap
    private lateinit var mPlacesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var address: Address
    private lateinit var place: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (!Places.isInitialized()) {
            Places.initialize(this, getString(R.string.google_maps_key))
        }

        mPlacesClient = Places.createClient(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        setUpMap()
    }

    private fun getAddress(latLng: LatLng): String {
        val geocoder = Geocoder(this)
        var addressText = ""

        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses.isNotEmpty()) {
                address = addresses[0]

                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n${address.getAddressLine(
                        i
                    )}"
                }
            }
        } catch (e: IOException) {
            Log.e(this::class.java.name, e.localizedMessage!!)
        }
        return addressText
    }

    private fun placeMarketOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        val titleStr = getAddress(location)
        markerOptions.title(titleStr)
        mMap.addMarker(markerOptions)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.maps_search, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search_place -> onSearchCalled()
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onSearchCalled() {
        val fields =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.PHOTO_METADATAS, Place.Field.ADDRESS, Place.Field.LAT_LNG)

        val intent = Autocomplete
            .IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .setCountry("RU")
            .build(this)

        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                Log.i(this::class.java.name, "${place.name}, ${place.id}, ${place.address}")
                Snackbar.make(
                    findViewById(android.R.id.content),
                    place.id.toString(),
                    Snackbar.LENGTH_LONG
                ).show()

                this.place = place.address!!
            } else if (resultCode == RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data!!)
                Log.e(this::class.java.name, status.statusMessage!!)
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Error!",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                placeMarketOnMap(currentLatLng)
            }

        }
    }

}
