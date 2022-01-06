package com.mappy.sdk.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.mappy.common.model.GeoConstants
import com.mappy.location.LocationUtil
import com.mappy.location.MappyLocationProvider
import com.mappy.location.MappyLocationServices
import com.mappy.map.MapController
import com.mappy.map.MappyMapFragment
import com.mappy.utils.PermissionHelper
import com.mappy.utils.ZoomConstants

abstract class UserExternalLocationSample : FragmentActivity() {
    private lateinit var mapController: MapController
    private lateinit var myLocationButton: View
    private var trackingRequested = false;

    private lateinit var locationProvider: MappyLocationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // init to use the external location service with an external GPS Location provider
        locationProvider = createExternalLocationProvider()
        MappyLocationServices.initExternalLocationServices(locationProvider)

        setContentView(R.layout.sample_user_location)
        myLocationButton = findViewById(R.id.sample_user_location_button)
        myLocationButton.isSelected = false

        val mapFragment = MappyMapFragment.newBuilder()
            .tracking(true)
            .bearing(false, true)
            .locationMarkerBearing(true, true)
            .userLocationThemeRes(R.style.MappyTheme_UserLocationLayer)
            .build()

        supportFragmentManager.beginTransaction()
            .replace(R.id.user_location_sample_map_fragment, mapFragment)
            .commitAllowingStateLoss()

        mapFragment.getMapControllerAsync {
            mapController = it
            mapController.center(GeoConstants.PARIS, ZoomConstants.TOWN)
            if (LocationUtil.canLocalize(this@UserExternalLocationSample)) {
                mapController.isMyLocationEnabled = true
            }

            myLocationButton.isSelected = mapController.isTracking
            myLocationButton.setOnClickListener { requestToggleTracking() }
            mapController.setOnMyLocationTrackingModeChangeListener {
                myLocationButton.isSelected = it
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocationUtil.resumeLocation(this)
    }

    override fun onPause() {
        super.onPause()
        LocationUtil.pauseLocation()
    }

    private fun requestToggleTracking() {
        trackingRequested = true
        if (LocationUtil.canLocalize(this@UserExternalLocationSample)) {
            onToggleTrackingRequested()
        }
    }

    @SuppressLint("MissingPermission")
    private fun onToggleTrackingRequested() {
        myLocationButton.isSelected = !myLocationButton.isSelected
        if (myLocationButton.isSelected) {
            mapController.enableTracking()
        } else {
            mapController.disableTracking()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MappyLocationServices.releaseExternalLocationServices()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (PermissionHelper.isLocationGranted(requestCode, grantResults)) {
            mapController.isMyLocationEnabled = true
            notifyLocationAvailable()
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    protected open fun notifyLocationAvailable() {
        if (trackingRequested) {
            onToggleTrackingRequested()
        } else {
            mapController.enableLocationMarkerBearing(true)
        }
    }

    protected abstract fun createExternalLocationProvider(): MappyLocationProvider
}
