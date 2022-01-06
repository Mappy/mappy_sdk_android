package com.mappy.sdk.sample

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.mappy.common.model.GeoBounds
import com.mappy.common.model.GeoConstants
import com.mappy.common.model.LatLng
import com.mappy.map.MapController
import com.mappy.map.MappyMapFragment
import com.mappy.map.MappyMarker
import com.mappy.sdk.sample.utils.MappyBitmapUtils

class MarkerSample : FragmentActivity() {

    private lateinit var mapController: MapController
    var toast: Toast? = null
        private set
    private var selectedMarkerTag: String? = null

    private val onTapListener: MappyMarker.OnTapListener = MappyMarker.OnTapListener {
        toast?.cancel()

        selectedMarkerTag = it.tag?.toString() ?: NO_POPUP
        toast = Toast.makeText(this, selectedMarkerTag, Toast.LENGTH_LONG).apply { show() }
        mapController.selectMarker(it)
        it.title.isNullOrEmpty() && it.snippet.isNullOrEmpty() // return true to avoid displaying InfoWindow
    }

    private val onTapGlobalListener: MappyMarker.OnTapListener = MappyMarker.OnTapListener {
        toast?.cancel()

        selectedMarkerTag = it.tag?.toString() ?: NO_POPUP
        toast = Toast.makeText(this, selectedMarkerTag, Toast.LENGTH_LONG).apply { show() }
        mapController.selectMarker(it)
        it.title.isNullOrEmpty() && it.snippet.isNullOrEmpty() // return true to avoid displaying InfoWindow
    }

    /**
     * Build a marker quickly
     */
    private val defaultMarkerBuilder =
        MappyMarker.newBuilder(GeoConstants.PARIS)// setting the LatLng, here the tapGlobalListener will be call.

    /**
     * Build a marker from a custom drawable (from drawableId)
     */
    private val customResourceIDMarker = MappyMarker.newBuilder(PARIS_LEFT) // setting the LatLng
        .title("ResourceId") // add a title
        .snippet("get from bitmap") // add a snippet
        .tapListener(onTapListener) // set the tap listener
        .infoWindow(true) // enable info window if onTapListener.onTap returns false
        .infoWindowListener(object :
            MappyMarker.InfoWindowEventListener { // set the info window listener
            override fun onInfoWindowClick(mappyMarker: MappyMarker): Boolean {
                toast?.cancel()
                toast = Toast.makeText(
                    this@MarkerSample,
                    "clic sur `$MARKER_TAG_RESOURCE_ID` InfoWindow",
                    Toast.LENGTH_LONG
                ).apply { show() }
                return true //return true to close the InfoWindow, false, to keep it displayed. Use `mapController.deselectMarker(marker)` to hide InfoWindow.
            }

            //inflate a custom layout to display wanted information.
            override fun inflateInfoWindow(context: Context, mappyMarker: MappyMarker): View? {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.marker_sample_info_window, null, false)

                view.findViewById<ImageView>(R.id.marker_sample_info_window_icon)
                    .setImageResource(R.drawable.categorie_jardinerie_bitmap)
                view.findViewById<TextView>(R.id.marker_sample_info_window_title).text =
                    mappyMarker.title
                view.findViewById<TextView>(R.id.marker_sample_info_window_description).text =
                    mappyMarker.snippet

                return view
            }
        })
        .tag(MARKER_TAG_RESOURCE_ID) // tag the Marker with a custom data that you'll get back onMarkerClick
        .icon(R.drawable.categorie_jardinerie_bitmap) // add a custom icon with a drawable id

    /**
     * Build a marker from a custom Drawable
     */
    private val customVectorMarker = MappyMarker.newBuilder(PARIS_RIGHT) // setting the LatLng
        .title("vecto") // add a title
        .snippet("get from vector drawable") // add a snippet
        .tapListener(onTapListener) // set the tap listener
        .infoWindow(true) // enable info window if onTapListener.onTap returns false
        .infoWindowListener(object :
            MappyMarker.InfoWindowEventListener { // set the info window listener
            override fun onInfoWindowClick(mappyMarker: MappyMarker): Boolean {
                toast?.cancel()
                toast = Toast.makeText(
                    this@MarkerSample,
                    "clic sur `$MARKER_TAG_VECTOR` InfoWindow",
                    Toast.LENGTH_LONG
                ).apply { show() }
                return true //return true to close the InfoWindow, false, to keep it displayed. Use `mapController.deselectMarker(marker)` to hide InfoWindow.
            }

            // use native Info Window.
            override fun inflateInfoWindow(context: Context, mappyMarker: MappyMarker): View? = null
        })
        .tag(MARKER_TAG_VECTOR) // tag the Marker with a custom data that you'll get back onMarkerClick
        .iconName("bitmapByVectorIcon") //should use the iconName if the icon is a bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_marker)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.sample_marker_mapFragment) as MappyMapFragment

        selectedMarkerTag = savedInstanceState?.getString(SELECTED_MARKER_TAG, null)

        mapFragment.getMapControllerAsync {
            mapController = it
            mapController.disableBearing()
            initMarkers()

            setOnMapLongClickListener()
            setClearListener()

            mapController.boundingBox = GeoBounds(PARIS_LEFT, PARIS_RIGHT, GeoConstants.PARIS)

            selectedMarkerTag?.let { tag ->
                mapController.allMarkers.find { marker -> marker.tag == null && NO_POPUP == tag || tag == marker.tag }
                    ?.let { marker ->
                        mapController.selectMarker(marker)
                    }
            }
        }

        customVectorMarker.icon(
            MappyBitmapUtils.getRubricMarkerBitmap(
                this,
                R.drawable.categorie_jardinerie
            )
        ) // add a custom icon with the built Bitmap
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
        mapController.setOnTapListener(onTapGlobalListener)
        mapController.addMarker(defaultMarkerBuilder)
        mapController.addMarker(customResourceIDMarker)
        mapController.addMarker(customVectorMarker)

        mapController.refreshMarkerSymbolLayer()
    }

    private fun setClearListener() {
        findViewById<View>(R.id.sample_marker_reinitialize).setOnClickListener {
            mapController.clearMarkers()
            initMarkers()
        }
    }

    private fun setOnMapLongClickListener() {
        mapController.setOnMapLongClickListener { point ->
            mapController.addMarker(point, point.toString())
            mapController.refreshMarkerSymbolLayer()
            true
        }
    }

    companion object {
        val PARIS_RIGHT = LatLng(48.849, 2.352)
        val PARIS_LEFT = LatLng(48.849, 2.348)

        private const val SELECTED_MARKER_TAG = "MarkerSample.selected.marker.tag"
        private const val MARKER_TAG_RESOURCE_ID = "Jardinerie from ResourceId / Paris Left"
        private const val MARKER_TAG_VECTOR = "Jardinerie from drawable/vecto / Paris Right"
        private const val NO_POPUP = "no popup"
    }
}