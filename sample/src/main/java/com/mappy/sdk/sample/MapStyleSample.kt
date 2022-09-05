package com.mappy.sdk.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.mappy.map.MapController
import com.mappy.map.MappyMapFragment
import com.mappy.map.domain.model.Layer
import com.mappy.map.domain.model.Theme


class MapStyleSample : FragmentActivity() {
    private lateinit var mapController: MapController
    private lateinit var spinnerTheme: Spinner
    private lateinit var spinnerLayer: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_map_style)

        spinnerTheme = findViewById(R.id.sample_map_theme)
        spinnerLayer = findViewById(R.id.sample_map_layer)

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
            mapController.setVisualOptions(Theme.DEFAULT, Layer.NEUTRAL)
            initSpinner()
        }
    }

    private fun initSpinner() {
        spinnerTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                mapController.setTheme(Theme.DEFAULT)
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val theme = when (position) {
                    0 -> Theme.DEFAULT
                    1 -> Theme.SIMPLE
                    2 -> Theme.NATURE
                    3 -> Theme.NIGHT
                    4 -> Theme.SATELLITE
                    5 -> Theme.MAAS_IDF
                    else -> Theme.DEFAULT
                }
                mapController.setTheme(theme)
                toastStyle()
            }
        }

        spinnerLayer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                mapController.setLayer(Layer.NEUTRAL)
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val layer = when (position) {
                    0 -> Layer.NEUTRAL
                    1 -> Layer.TRAFFIC
                    2 -> Layer.CRITAIR
                    3 -> Layer.BICYCLE
                    4 -> Layer.TRANSPORTS
                    5 -> Layer.GPS
                    else -> Layer.NEUTRAL
                }
                mapController.setLayer(layer)
                toastStyle()
            }
        }
    }

    private fun toastStyle() = Toast.makeText(
        this,
        """${mapController.visualOptions.theme.urlPrefix}_${mapController.visualOptions.layer.urlSuffix}""",
        Toast.LENGTH_SHORT
    ).show()

}