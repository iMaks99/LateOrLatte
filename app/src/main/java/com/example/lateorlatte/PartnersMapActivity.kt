package com.example.lateorlatte

import android.content.pm.PackageManager
import android.location.Address
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.cafe_info_bottom_sheet.*

class PartnersMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var listCafe = ArrayList<CafeItem>()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var markers: ArrayList<Marker> = ArrayList()
    private lateinit var currentMarker : CafeItem
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partners_map)

        listCafe.add(CafeItem("Эклерная Клер", "улица Сретенка, 26/1", "", 55.770594, 37.632970))
        listCafe.add(
            CafeItem(
                "Nook Coffee",
                "Сущёвская ул., 14, библиотека искусств им. А.П. Боголюбова",
                "",
                55.781369,
                37.602273
            )
        )
        listCafe.add(
            CafeItem(
                "Coffeeport",
                "Большой Кисловский пер., 1, стр. 2, 1этаж",
                "",
                55.753591,
                37.605508
            )
        )
        listCafe.add(
            CafeItem(
                "Вкусно Кофе",
                "Панкратьевский пер., 2, этаж 1",
                "",
                55.771685,
                37.632732
            )
        )
        listCafe.add(CafeItem("Котомка", "Малый Казённый пер., 16", "", 55.761207, 37.655291))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        bottomSheetBehavior = BottomSheetBehavior.from(cafe_item_bottom_sheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        mMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker): Boolean {

                for(i in listCafe)
                {
                    if(i.getTitle() == marker.title)
                        currentMarker = i
                }

                address_cafe.text = currentMarker.getAddress()
                title_cafe.text = currentMarker.getTitle()

                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                return false
            }
        })

        setUpMap()
    }

    private fun placeMarketOnMap() {

        for (item in listCafe) {
            val marker = mMap.addMarker(MarkerOptions().position(LatLng(item.getX(), item.getY())).title(item.getTitle()))
            marker.showInfoWindow()
            markers.add(marker)
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
                PartnersMapActivity.LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            }
        }
        placeMarketOnMap()
    }
}
