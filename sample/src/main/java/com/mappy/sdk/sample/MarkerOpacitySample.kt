package com.mappy.sdk.sample

import android.os.Bundle
import android.widget.SeekBar
import androidx.fragment.app.FragmentActivity
import com.mappy.common.model.GeoBounds
import com.mappy.common.model.GeoConstants
import com.mappy.common.model.LatLng
import com.mappy.map.MapController
import com.mappy.map.MappyMapFragment
import com.mappy.map.MappyMarker
import com.mappy.sdk.sample.utils.extention.setOnSeekBarChangeListener
import kotlin.math.max
import kotlin.math.min

/**
 * Sample to demonstrate how to use the markers opacity feature.
 */
class MarkerOpacitySample : FragmentActivity() {

    private val markers
        get() = (0..10).map {
            MappyMarker.newBuilder(GeoConstants.PARIS + (0.01 * it).lng)
                .opacity(it.toFloat() / 10f)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_marker_opacity)

        val opacitySeekBar: SeekBar = findViewById(R.id.opacity_seekBar)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.sample_marker_mapFragment) as MappyMapFragment

        mapFragment.getMapControllerAsync {
            it.initMarkers()
            it.centerCamera()
            opacitySeekBar.initSeekBar(it)
        }
    }

    /**
     * Convenience method to add the markers to the map controller.
     */
    private fun MapController.initMarkers() {
        markers.forEach { addMarker(it) }
        // don't forget to call this method otherwise maker will not show.
        refreshMarkerSymbolLayer()
    }

    /**
     * Convenience method to center the camera on top of our markers.
     */
    private fun MapController.centerCamera() {
        boundingBox = GeoBounds(markers.map { it.position })
    }

    /**
     * Convenience method to init the seekBar.
     */
    private fun SeekBar.initSeekBar(controller: MapController) {
        setOnSeekBarChangeListener(onProgressChanged = { _, progress: Int, _ ->
            controller.allMarkers.forEachIndexed { index, marker ->
                val opacity = index.toFloat() / 10f - (1f - progress.toFloat() / 100f)
                marker.opacity = max(0f, min(1f, opacity))
            }
            controller.refreshMarkerSymbolLayer()
        })
    }

    /**
     * Convenience method to transform a Double into a longitude LatLng.
     */
    private val Double.lng get() = LatLng(0.0, this)

    /**
     * Convenience method to add two LatLng together.
     */
    private operator fun LatLng.plus(offset: LatLng): LatLng =
        LatLng(latitude + offset.latitude, longitude + offset.longitude)

    companion object {
        val PARIS = GeoConstants.PARIS
    }
}