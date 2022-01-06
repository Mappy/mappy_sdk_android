package com.mappy.sdk.sample

import android.os.Bundle
import android.widget.ZoomControls
import androidx.fragment.app.FragmentActivity
import com.mappy.map.MappyMapFragment

class HelloMapSample : FragmentActivity() {

    private lateinit var zoomControls: ZoomControls

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_hello_map)

        zoomControls = findViewById(R.id.sample_hello_map_zoomControls)
    }

    override fun onResume() {
        super.onResume()
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.sample_hello_map_mapFragment) as MappyMapFragment
        mapFragment.getMapControllerAsync { mapController ->
            mapController.disableBearing()
            zoomControls.setOnZoomInClickListener {
                mapController.zoomIn()
            }
            zoomControls.setOnZoomOutClickListener {
                mapController.zoomOut()
            }
            mapFragment.setRotateGesturesEnabled(true)
            mapFragment.setTiltGesturesEnabled(true)
        }
    }

    override fun onPause() {
        zoomControls.setOnZoomInClickListener(null)
        zoomControls.setOnZoomOutClickListener(null)
        super.onPause()
    }
}