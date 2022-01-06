package com.mappy.sdk.sample

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.os.Looper
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.mappy.location.MappyLocationProvider
import com.mappy.location.OnMyLocationChangedListener

class UserExternalGoogleLocationSample : UserExternalLocationSample() {
    private var externalGpsService: GoogleGpsProvider? = null

    override fun onDestroy() {
        externalGpsService?.release()
        super.onDestroy()
    }

    override fun createExternalLocationProvider(): MappyLocationProvider {
        val provider = GoogleGpsProvider(this)
        externalGpsService = provider
        return provider
    }

    private fun servicesAvailable(): Boolean {
        val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        return when (resultCode) {
            ConnectionResult.SUCCESS -> true
            else -> {
                GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode, 0).show()
                false
            }
        }
    }

    internal inner class GoogleGpsProvider(context: Context) : LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        MappyLocationProvider {
        @SuppressLint("MissingPermission")
        override fun requestLocationUpdates(
            locationRequest: LocationRequest,
            callback: LocationCallback,
            looper: Looper?
        ) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient,
                locationRequest,
                callback,
                looper
            )
        }

        @SuppressLint("MissingPermission")
        override fun requestLocationUpdates(
            locationRequest: LocationRequest,
            pendingIntent: PendingIntent
        ) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient,
                locationRequest,
                pendingIntent
            )
        }

        override fun removeLocationUpdates(callback: LocationCallback) {
            if (googleApiClient.isConnected) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, callback)
            }
        }

        override fun removeLocationUpdates(pendingIntent: PendingIntent) {
            if (googleApiClient.isConnected) {
                LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient,
                    pendingIntent
                )
            }
        }

        @SuppressLint("MissingPermission")
        override fun getLastLocation(callback: LocationCallback) {
            val lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
            if (lastLocation != null) {
                val locations = ArrayList<Location>()
                locations.add(lastLocation)
                callback?.onLocationResult(LocationResult.create(locations))
            } else {
                callback?.onLocationResult(null)
            }
        }

        override fun addLocationChangedListener(listener: OnMyLocationChangedListener) {
            externalGpsListener = listener
        }

        override fun removeLocationChangedListener(listener: OnMyLocationChangedListener) {
            if (externalGpsListener == listener) {
                externalGpsListener = null
            }
        }

        private val googleApiClient: GoogleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()
        private var externalGpsListener: OnMyLocationChangedListener? = null

        init {
            googleApiClient.connect()
        }

        @SuppressLint("MissingPermission")
        override fun onConnected(bundle: Bundle?) {
            // Get first reading. Get additional location updates if necessary
            if (servicesAvailable()) {
                val locationRequest = LocationRequest.create()
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                locationRequest.interval = 1000 * 10L
                locationRequest.fastestInterval = 1000

                // Get the best most recent location currently available
                val currentLocation =
                    LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
                if (currentLocation != null) {
                    externalGpsListener?.onMyLocationChanged(currentLocation)
                }

                LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient,
                    locationRequest,
                    this
                )
            }
        }

        override fun onLocationChanged(location: Location) {
            externalGpsListener?.onMyLocationChanged(location)
        }

        override fun onConnectionSuspended(i: Int) {}

        override fun onConnectionFailed(connectionResult: ConnectionResult) {}

        fun release() {
            if (googleApiClient.isConnected) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
                googleApiClient.disconnect()
            }
        }
    }
}
