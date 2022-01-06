package com.mappy.sdk.sample

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.mappy.common.model.GeoConstants
import com.mappy.common.model.LatLng
import com.mappy.map.*
import com.mappy.sdk.sample.utils.PolylineSampleUtils
import com.mappy.sdk.sample.utils.ProgressDialogHelper
import com.mappy.utils.ZoomConstants

class PolylineAndPolygonSample : FragmentActivity() {
    private val progressDialogHelper = ProgressDialogHelper(this)
    private var mappyPolyline: MappyPolyline? = null
    private var mappyPolygon: MappyPolygon? = null
    private lateinit var mapController: MapController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_polyline)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.sample_polyline_map_fragment) as MappyMapFragment
        mapFragment.getMapControllerAsync {
            mapController = it
            mapController.disableBearing()
            setLoadRouteListener()
            setLoadPolygonListener()
            setClearListener()
        }
    }

    private fun setLoadRouteListener() {
        findViewById<View>(R.id.load_route_button).setOnClickListener {
            mapController.center(GeoConstants.FRANCE.center, ZoomConstants.COUNTRY)
            if (mappyPolyline == null) {
                loadRoutes(PolylineSampleUtils.POLYLINE.PARIS_MARSEILLE, "#00346B")
                loadRoutes(PolylineSampleUtils.POLYLINE.BREST_BUCAREST, "#00846B")
            }
        }
    }

    private fun setLoadPolygonListener() {
        findViewById<View>(R.id.load_polygon).setOnClickListener {
            val fillColor =
                ContextCompat.getColor(it.context, R.color.sample_polyline_paris_01_fill)
            val strokeColor =
                ContextCompat.getColor(it.context, R.color.sample_polyline_paris_01_stroke)
            loadPolygon(PolylineSampleUtils.POLYGON.PARIS_01, fillColor, strokeColor, POLYGON_ALPHA)
        }
    }

    private fun setClearListener() {
        findViewById<View>(R.id.clear_button).setOnClickListener {
            mapController.clearPolylines()
            mapController.clearPolygons()
            mappyPolygon = null
            mappyPolyline = null
        }
    }

    private fun loadRoutes(polyline: PolylineSampleUtils.POLYLINE, color: String) {
        progressDialogHelper.show()
        polyline[this, object : PolylineSampleUtils.PolylineListener {
            override fun onPolylineLoaded(polyline: Array<LatLng>) {
                mappyPolyline?.addSection(
                    MappySection.Builder(polyline)
                        .color(Color.parseColor(color))
                        .build()
                ) ?: run {

                    val tmpPolyline = MappyPolyline.Builder()
                        .withWhiteStrokeForSolid(false)
                        .addSection(
                            MappySection.Builder(polyline)
                                .color(Color.parseColor(color))
                                .build()
                        )
                        .build()
                    mappyPolyline = tmpPolyline
                }

                mapController.clearPolylines()
                mapController.addPolyline(mappyPolyline)
                mapController.displayWholePolyLines()
                progressDialogHelper.dismiss()
            }
        }]
    }

    private fun loadPolygon(
        polygon: PolylineSampleUtils.POLYGON,
        fillColor: Int,
        strokeColor: Int,
        alpha: Float
    ) {
        progressDialogHelper.show()
        polygon[this, object : PolylineSampleUtils.PolygonListener {
            override fun onPolygonLoaded(polygon: Array<LatLng>) {
                val tmpPolygon = mappyPolygon ?: MappyPolygon()
                tmpPolygon.add(polygon)
                tmpPolygon.fillColor(fillColor)
                tmpPolygon.strokeColor(strokeColor)
                tmpPolygon.alpha(alpha)

                mappyPolygon = tmpPolygon
                mapController.clearPolygons()
                mapController.addPolygon(mappyPolygon)
                mapController.displayWholePolygons()
                progressDialogHelper.dismiss()
            }
        }]
    }

    companion object {
        private const val POLYGON_ALPHA = 0.5f
    }
}