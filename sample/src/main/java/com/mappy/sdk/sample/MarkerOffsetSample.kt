package com.mappy.sdk.sample

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.mappy.common.model.GeoBounds
import com.mappy.common.model.GeoConstants
import com.mappy.common.model.LatLng
import com.mappy.map.MapController
import com.mappy.map.MappyMapFragment
import com.mappy.map.MappyMarker

/**
 * Sample to demonstrate how to use the markers offset feature.
 */
class MarkerOffsetSample : FragmentActivity() {

    private val defaultMarker
        get() = MappyMarker.newBuilder(GeoConstants.PARIS)  // default markers (anchor by default is bottom)

    private val unbalancedMarker
        get() = MappyMarker.newBuilder(GeoConstants.PARIS)  // use the same location as defaultMarker.
            .icon(R.drawable.ic_unbalanced_pin_border)  // optional: use an unbalanced icon as the default one is balanced
            .zOrder(2)                                  // optional: for convenience purpose make sure this marker is on top of default.
            .offset(
                6.8f,
                15.5f
            )                        // feature: offset the icon so that the "center" is the same as defaultMarker.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_marker_offset)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.sample_marker_mapFragment) as MappyMapFragment

        mapFragment.getMapControllerAsync {
            it.initMarkers()
            it.centerCamera()
        }
    }

    /**
     * Convenience method to add the markers to the map controller.
     */
    private fun MapController.initMarkers() {
        addMarker(defaultMarker)
        addMarker(unbalancedMarker)
        // don't forget to call this method otherwise maker will not show.
        refreshMarkerSymbolLayer()
    }

    /**
     * Convenience method to center the camera on top of our markers.
     */
    private fun MapController.centerCamera() {
        boundingBox = GeoBounds(PARIS_LEFT, PARIS_RIGHT, PARIS)
    }

    companion object {
        val PARIS = GeoConstants.PARIS
        val PARIS_RIGHT = LatLng(48.849, 2.352)
        val PARIS_LEFT = LatLng(48.849, 2.348)
    }
}