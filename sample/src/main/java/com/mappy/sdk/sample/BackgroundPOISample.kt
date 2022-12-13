package com.mappy.sdk.sample

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.mappy.common.model.GeoBounds
import com.mappy.common.model.GeoConstants
import com.mappy.common.model.LatLng
import com.mappy.map.MapController
import com.mappy.map.MappyMapFragment
import com.mappy.map.model.BackgroundPOI

class BackgroundPOISample : FragmentActivity() {
    companion object {
        private val BOUNDS_LEFT = LatLng(48.840, 2.330)
        private val BOUNDS_RIGHT = LatLng(48.832, 2.340)
    }

    lateinit var mapController: MapController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_background_poi)
    }

    override fun onResume() {
        super.onResume()
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.sample_background_poi_map_fragment) as MappyMapFragment

        mapFragment.getMapControllerAsync {
            mapController = it
            mapController.disableBearing()
            val minBounds = GeoBounds(BOUNDS_LEFT, BOUNDS_RIGHT, GeoConstants.PARIS)
            mapController.setBoundingBox(
                minBounds,
                resources.getDimensionPixelSize(com.mappy.map.R.dimen.mappy__bounding_box_default_padding),
                false
            )

            setMapListener()
        }
    }

    private fun setMapListener() {
        mapController.setOnMapClickListener(object : MapController.OnMapClickListener {
            override fun onMapClick(latLng: LatLng, backgroundPOI: BackgroundPOI?): Boolean {
                Toast.makeText(
                    this@BackgroundPOISample,
                    backgroundPOI?.name ?: "Rien ici !",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }

            override fun onPostMapClick(point: LatLng, backgroundPOI: BackgroundPOI?) = false
            override fun onMapClickConsumed() {}

            override fun isNotSimpleTouch(isMarkerTouch: Boolean) {}
        })
    }
}