package com.mappy.sdk.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.mappy.map.MapController
import com.mappy.map.MapStyle
import com.mappy.map.MappyMapFragment


class MapStyleSample : FragmentActivity() {
    private lateinit var mapController: MapController
    private lateinit var togglePhoto: Button
    private lateinit var toggleTraffic: Button
    private lateinit var toggleTransport: Button
    private lateinit var spinnerFamily: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_map_style)

        togglePhoto = findViewById(R.id.sample_map_style_photo_toggle)
        toggleTraffic = findViewById(R.id.sample_map_style_traffic_toggle)
        toggleTransport = findViewById(R.id.sample_map_style_transport_toggle)
        spinnerFamily = findViewById(R.id.sample_map_family)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.sample_map_style_mapFragment) as MappyMapFragment
        mapFragment.getMapControllerAsync {
            mapController = it
            mapController.disableBearing()
            // add listener to know when the camera is in idle mode
            mapController.setOnMapCameraIdleListener {
                Log.i(
                    MapStyleSample::javaClass.name,
                    "OnCameraIdle"
                )
            }
            // add listener to know when the camera move
            mapController.setOnMapCenterChangedListener { center, zoom, reason ->
                Log.i(
                    MapStyleSample::javaClass.name,
                    "OnCameraMove"
                )
            }
            mapController.setStyle(MapStyle.STANDARD, MapStyle.Family.DEFAULT_FAMILY)
            initSpinner()
            manageWording()
        }
    }

    private fun initSpinner() {
        spinnerFamily.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                mapController.setFamily(MapStyle.Family.DEFAULT_FAMILY, null)
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val family = when (position) {
                    1 -> MapStyle.Family.MAAS_IDF_FAMILY
                    else -> MapStyle.Family.DEFAULT_FAMILY
                }
                mapController.setFamily(family, null)
            }
        }
    }

    private fun manageWording() {
        togglePhoto.text = if (mapController.isUsingPhotoStyle) "Standard" else "Photo"
        toggleTransport.text =
            if (mapController.isPublicTransportStyleEnabled) "TC ON" else "TC OFF"
        toggleTraffic.text = if (mapController.isTrafficStyleEnabled) "Trafic ON" else "Trafic OFF"
    }

    private fun toastStyle() = Toast.makeText(
        this,
        when (mapController.mapStyle) {
            MapStyle.STANDARD -> "STANDARD"
            MapStyle.DAY_TRAFFIC -> "DAY_TRAFFIC"
            MapStyle.DAY_TRANSPORT -> "DAY_TRANSPORT"
            MapStyle.DAY_BIKE_PATH -> "DAY_BIKE_PATH"
            MapStyle.NIGHT_TRAFFIC -> "NIGHT_TRAFFIC"
            MapStyle.NIGHT_TRANSPORT -> "NIGHT_TRANSPORT"
            MapStyle.NIGHT_BIKE_PATH -> "NIGHT_BIKE_PATH"
            MapStyle.PHOTO -> "PHOTO"
            MapStyle.PHOTO_TRAFFIC -> "PHOTO_TRAFFIC"
            MapStyle.PHOTO_TRANSPORT -> "PHOTO_TRANSPORT"
            MapStyle.PHOTO_BIKE_PATH -> "PHOTO_BIKE_PATH"
            MapStyle.GPS_DAY -> "GPS_DAY"
            MapStyle.GPS_NIGHT -> "GPS_NIGHT"
            else -> ""
        },
        Toast.LENGTH_SHORT
    )
        .show()

    override fun onResume() {
        super.onResume()

        togglePhoto.setOnClickListener {
            if (mapController.isUsingPhotoStyle) {
                mapController.useStandardStyle()
                togglePhoto.text = "Photo"
            } else {
                mapController.usePhotoStyle()
                togglePhoto.text = "Standard"
            }
            toastStyle()
        }

        toggleTraffic.setOnClickListener {
            mapController.toggleTrafficStyle()
            manageWording()
            toastStyle()
        }

        toggleTransport.setOnClickListener {
            mapController.toggleTransportStyle()
            manageWording()
            toastStyle()
        }
    }

    override fun onPause() {
        togglePhoto.setOnClickListener(null)
        toggleTraffic.setOnClickListener(null)
        toggleTransport.setOnClickListener(null)
        super.onPause()
    }
}