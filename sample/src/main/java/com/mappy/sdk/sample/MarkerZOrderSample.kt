package com.mappy.sdk.sample

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.mappy.common.model.GeoBounds
import com.mappy.common.model.GeoConstants
import com.mappy.common.model.LatLng
import com.mappy.map.MapController
import com.mappy.map.MappyMapFragment
import com.mappy.map.MappyMarker
import com.mappy.sdk.sample.utils.MappyBitmapUtils

class MarkerZOrderSample : FragmentActivity() {
    lateinit var mapController: MapController
    var toast: Toast? = null
    var selectedMarkerTag: String? = null

    private val onTapListener: MappyMarker.OnTapListener = MappyMarker.OnTapListener {
        toast?.cancel()

        selectedMarkerTag = if (it.tag != null) it.tag.toString() else NO_TAG
        toast = Toast.makeText(this@MarkerZOrderSample, selectedMarkerTag, Toast.LENGTH_LONG)
        toast!!.show()
        mapController.selectMarker(it)
        TextUtils.isEmpty(it.title) || TextUtils.isEmpty(it.snippet) // return false if you want to have the infoWindow
    }

    /**
     * Build a marker quickly
     */
    private val defaultMarkerBuilder = MappyMarker.newBuilder(PARIS_SOUTH) // setting the LatLng
        .tag(MARKER_1)
        .zOrder(0)// Set the z-order layer to 0 (below)
        .tapListener(onTapListener)// set the tap listener

    /**
     * Build a marker from a custom drawable (from drawableId)
     */
    private val customResourceIDMarker =
        MappyMarker.newBuilder(GeoConstants.PARIS) // setting the LatLng
            .tapListener(onTapListener) // set the tap listener
            .tag(MARKER_2) // tag the Marker with a custom data that you'll get back onMarkerClick
            .zOrder(1)  // Set the z-order layer to 1
            .icon(R.drawable.categorie_jardinerie_bitmap) // add a custom icon with a drawable id

    /**
     * Build a marker from a custom Drawable
     */
    private val customVectorMarker = MappyMarker.newBuilder(PARIS_NORTH) // setting the LatLng
        .tapListener(onTapListener) // set the tap listener
        .tag(MARKER_3) // tag the Marker with a custom data that you'll get back onMarkerClick
        .zOrder(2)  // Set the z-order layer to 2 (top, as defined in sample_marker_zorder.xml)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_marker_zorder)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.sample_marker_zorder_mapFragment) as MappyMapFragment

        selectedMarkerTag = savedInstanceState?.getString(SELECTED_MARKER_TAG, null)

        mapFragment.getMapControllerAsync {
            mapController = it
            mapController.disableBearing()
            initMarkers()

            setOnMapLongClickListener()
            setClearListener()

            mapController.boundingBox = GeoBounds(PARIS_NORTH, PARIS_SOUTH, GeoConstants.PARIS)

            selectedMarkerTag?.let {
                for (marker in mapController.allMarkers) {
                    if (marker.tag == null && NO_TAG == it || it == marker.tag) {
                        mapController.selectMarker(marker)
                    }
                    // Can also use deselectMarker to mark marker as deselected
                }
            }
        }
        customVectorMarker.icon(
            MappyBitmapUtils.getRubricMarkerBitmap(
                this,
                R.drawable.categorie_jardinerie
            )
        ) // get the BitmapDrawable composed by a .png background + .xml corresponding to a vector drawable
    }

    override fun onPause() {
        super.onPause()
        toast = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SELECTED_MARKER_TAG, selectedMarkerTag)
        super.onSaveInstanceState(outState)
    }

    /**
     * initialize different GL markers en map
     */
    private fun initMarkers() {
        mapController.addMarker(defaultMarkerBuilder)
        mapController.addMarker(customResourceIDMarker)
        mapController.addMarker(customVectorMarker)

        mapController.refreshMarkerSymbolLayer()
    }

    private fun setClearListener() =
        findViewById<View>(R.id.sample_marker_zorder_reinitialize).setOnClickListener {
            mapController.clearMarkers()
            initMarkers()
        }

    private fun setOnMapLongClickListener() = mapController.setOnMapLongClickListener {
        mapController.addMarker(it, it.toString())
        mapController.refreshMarkerSymbolLayer()
        true
    }

    companion object {
        private val PARIS_SOUTH = LatLng(48.848895, 2.349816)
        private val PARIS_NORTH = LatLng(48.851140, 2.351297)

        private const val SELECTED_MARKER_TAG = "MarkerSample.selected.marker.tag"
        private const val MARKER_1 = "Marker zOrder = 0"
        private const val MARKER_2 = "Marker zOrder = 1"
        private const val MARKER_3 = "Marker zOrder = 2"
        private const val NO_TAG = "--"
    }
}