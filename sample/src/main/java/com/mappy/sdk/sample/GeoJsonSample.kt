package com.mappy.sdk.sample

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.mappy.map.GeoJsonHolder
import com.mappy.map.MapController
import com.mappy.map.MappyMapFragment
import com.mappy.sdk.sample.utils.GeoJsonSampleUtils
import com.mappy.sdk.sample.utils.ProgressDialogHelper


class GeoJsonSample : FragmentActivity() {
    companion object {
        private const val POLYGON_ALPHA = 0.5f
        private const val POLYLINE_ALPHA = 0.75f
    }

    private val progressDialogHelper = ProgressDialogHelper(this)
    val geoJsonHolders = HashMap<GeoJsonSampleUtils.GeoJson, GeoJsonHolder>()

    private lateinit var mapController: MapController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_geojson)


        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.sample_geojson_map_fragment) as MappyMapFragment
        mapFragment.getMapControllerAsync {
            mapController = it
            mapController.disableBearing()
            setLoadPointListener()
            setLoadPolygonListener()
            setLoadLineStringListener()
            setClearListener()
        }
    }

    private fun setLoadPointListener() =
        findViewById<View>(R.id.sample_geojson_load_point).setOnClickListener {
            loadGeoJson(it.context, GeoJsonSampleUtils.GeoJson.SIMPLE_POINT)
        }

    private fun setLoadPolygonListener() =
        findViewById<View>(R.id.sample_geojson_load_polygon).setOnClickListener {
            loadGeoJson(it.context, GeoJsonSampleUtils.GeoJson.SIMPLE_POLYGON)
        }

    private fun setLoadLineStringListener() =
        findViewById<View>(R.id.sample_geojson_load_linestring).setOnClickListener {
            loadGeoJson(it.context, GeoJsonSampleUtils.GeoJson.SIMPLE_LINESTRING)
        }

    private fun setClearListener() =
        findViewById<View>(R.id.sample_geojson_clear).setOnClickListener {
            mapController.clearGeoJson()
            geoJsonHolders.clear()
        }

    private fun loadGeoJson(context: Context, geoJson: GeoJsonSampleUtils.GeoJson) {
        progressDialogHelper.show()
        geoJson[this, object : GeoJsonSampleUtils.GeoJsonListener {

            override fun onGeoJsonLoaded(json: String) {
                val geoJsonHolder = geoJsonHolders[geoJson]
                if (geoJsonHolder != null) {
                    mapController.removeGeoJson(geoJsonHolder)
                    geoJsonHolders.remove(geoJson)
                }

                val polygonFillColor =
                    ContextCompat.getColor(context, R.color.sample_geojson_simple_polygon_fill)
                val polygonStrokeColor =
                    ContextCompat.getColor(context, R.color.sample_geojson_simple_polygon_stroke)
                val polylineColor =
                    ContextCompat.getColor(context, R.color.sample_geojson_simple_polyline_color)

                val builder = GeoJsonHolder.newBuilder(json)
                    .stylePoint(R.drawable.user_location_sample_marker_default_blue, 0.5f, 0.5f)
                    .stylePolygon(polygonStrokeColor, polygonFillColor, POLYGON_ALPHA)
                    .styleLineStrings(polylineColor, POLYLINE_ALPHA, 5.5f, true)

                mapController.addGeoJson(builder, object : MapController.GeoJsonAddedListener {
                    override fun geoJsonAddedSuccess(geoJsonHolder: GeoJsonHolder) {
                        geoJsonHolders[geoJson] = geoJsonHolder
                        mapController.displayWholeGeoJsons()
                        progressDialogHelper.dismiss()
                    }

                    override fun geoJsonAddedError(error: Throwable) {
                        error.printStackTrace()
                    }
                })
            }
        }]
    }
}