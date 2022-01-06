package com.mappy.sdk.sample

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.mappy.common.model.GeoConstants
import com.mappy.common.model.LatLng
import com.mappy.location.LocationUtil
import com.mappy.map.MapController
import com.mappy.map.MappyMapFragment
import com.mappy.map.MappyPolyline
import com.mappy.map.MappySection
import com.mappy.sdk.sample.utils.PolylineSampleUtils
import com.mappy.sdk.sample.utils.ProgressDialogHelper
import com.mappy.utils.ZoomConstants
import java.util.*

class StylePolylineSample : FragmentActivity() {
    private val progressDialogHelper = ProgressDialogHelper(this)
    private val mappyPolylines = ArrayList<MappyPolyline>()
    private lateinit var mapController: MapController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_style_polyline)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.sample_polyline_map_fragment) as MappyMapFragment
        mapFragment.getMapControllerAsync {
            mapController = it
            mapController.disableBearing()
            setLoadRouteListener()
            setLoadAnimRouteListener()
            setClearListener()

            mapFragment.setRotateGesturesEnabled(true)
            mapFragment.setTiltGesturesEnabled(true)

            if (LocationUtil.canLocalize(this)) {
                mapController.enableTracking()
            }
        }
    }

    private fun setLoadRouteListener() {
        findViewById<View>(R.id.load_route_button).setOnClickListener {
            clearPolylines()
            mapController.center(GeoConstants.FRANCE.center, ZoomConstants.COUNTRY)
            progressDialogHelper.show()

            val displayRouteTask = Runnable {
                mapController.clearStylePolyline()
                for (mappyPolyline in mappyPolylines) {
                    mapController.addStylePolyline(mappyPolyline, true)
                }
                mapController.displayWholeStylePolyLines(true, false, null)
                progressDialogHelper.dismiss()
            }

            loadRoutes(
                PolylineSampleUtils.POLYLINE.BREST_BUCAREST,
                ContextCompat.getColor(
                    this@StylePolylineSample,
                    R.color.sample_polyline_brest_bucarest
                ),
                false,
                displayRouteTask
            )
            loadRoutes(
                PolylineSampleUtils.POLYLINE.PARIS_MARSEILLE,
                ContextCompat.getColor(
                    this@StylePolylineSample,
                    R.color.sample_polyline_paris_marseille
                ),
                true,
                displayRouteTask
            )
        }
    }

    private fun setLoadAnimRouteListener() {
        findViewById<View>(R.id.load_route_with_animation).setOnClickListener {
            mapController.center(GeoConstants.FRANCE.center, ZoomConstants.COUNTRY)
            clearPolylines()
            progressDialogHelper.show()

            val displayRouteTask = Runnable {
                if (mappyPolylines.size == 2) {
                    for (mappyPolyline in mappyPolylines) {
                        mapController.addStylePolyline(mappyPolyline, false)
                    }
                    mapController.displayWholeStylePolyLines(true, true, null)
                    progressDialogHelper.dismiss()
                }
            }

            loadRoutes(
                PolylineSampleUtils.POLYLINE.BREST_BUCAREST,
                ContextCompat.getColor(
                    this@StylePolylineSample,
                    R.color.sample_polyline_brest_bucarest
                ),
                false,
                displayRouteTask
            )
            loadRoutes(
                PolylineSampleUtils.POLYLINE.PARIS_MARSEILLE,
                ContextCompat.getColor(
                    this@StylePolylineSample,
                    R.color.sample_polyline_paris_marseille
                ),
                true,
                displayRouteTask
            )
        }
    }

    private fun setClearListener() =
        findViewById<View>(R.id.clear_button).setOnClickListener { clearPolylines() }

    private fun clearPolylines() {
        mapController.clearStylePolyline()
        mappyPolylines.clear()
    }

    private fun loadRoutes(
        polyline: PolylineSampleUtils.POLYLINE,
        color: Int,
        withBorder: Boolean,
        callback: Runnable?
    ) {
        polyline[this, object : PolylineSampleUtils.PolylineListener {
            override fun onPolylineLoaded(polyline: Array<LatLng>) {
                val newMappyPolyline = MappyPolyline.Builder()
                    .withWhiteStrokeForSolid(withBorder)
                    .addSection(
                        MappySection.Builder(polyline)
                            .color(color)
                            .build()
                    )
                    .build()
                mappyPolylines.add(newMappyPolyline)
                callback?.run()
            }
        }]
    }
}
