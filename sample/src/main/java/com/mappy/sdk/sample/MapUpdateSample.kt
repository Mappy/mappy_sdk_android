package com.mappy.sdk.sample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.mappy.common.model.GeoConstants
import com.mappy.map.MapPosition
import com.mappy.map.MappyMapFragment
import com.mappy.utils.ZoomConstants


class MapUpdateSample : FragmentActivity() {

    private lateinit var brestButton: View
    private lateinit var franceButton: View

    // Lyon position
    private val firstMapPosition by lazy {
        MapPosition.fromPosition(GeoConstants.LYON, ZoomConstants.TOWN)
    }

    // Brest position with bearing and tilt
    private val brestMapPosition by lazy {
        MapPosition.fromPosition(GeoConstants.BREST, ZoomConstants.TOWN)
            .bearing(90.0)
            .tilt(30.0)
            .duration(2000)
            .padding(resources.getDimensionPixelOffset(R.dimen.map_update_sample_padding))
    }

    // France bounding box  with no tilt and bearing
    private val franceMapPosition by lazy {
        MapPosition.fromBoundingBox(GeoConstants.FRANCE)
            .bearing(0.0)
            .tilt(0.0)
            .duration(1000)
            .padding(resources.getDimensionPixelOffset(R.dimen.map_update_sample_padding))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_map_update)

        brestButton = findViewById(R.id.sample_map_update_brest)
        franceButton = findViewById(R.id.sample_map_update_france)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.sample_map_update_mapFragment) as MappyMapFragment
        mapFragment.getMapControllerAsync { mapController ->

            mapController.disableBearing()
            // enable feature
            mapFragment.setRotateGesturesEnabled(true)
            mapFragment.setTiltGesturesEnabled(true)

            // move to first position
            mapController.moveTo(firstMapPosition)

            // animate to brest
            brestButton.setOnClickListener { mapController.animateTo(brestMapPosition) }

            // ease to France
            franceButton.setOnClickListener { mapController.easeTo(franceMapPosition) }
        }
    }
}
