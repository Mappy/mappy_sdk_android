package com.mappy.sdk.sample

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.mappy.common.model.GeoBounds
import com.mappy.common.model.GeoConstants
import com.mappy.common.model.LatLng
import com.mappy.map.MapController
import com.mappy.map.MappyMapFragment
import com.mappy.sdk.sample.utils.AddressUtil
import com.mappy.sdk.sample.utils.ProgressDialogHelper
import com.mappy.services.MappyDownloadManager
import com.mappy.services.RequestListener
import com.mappy.services.requests.GetLocationByCoordinatesRequest
import com.mappy.services.requests.GetLocationByQueryRequest
import com.mappy.utils.ConnectivityUtil
import com.mappy.webservices.resource.model.dao.MappyLocation
import com.mappy.webservices.resource.store.LocationStore

class GeocodeSample : FragmentActivity(), View.OnClickListener,
    MapController.OnMapLongClickListener {
    companion object {
        const val TOWN_ZOOM_LEVEL_FOR_GEOCODE_SAMPLE = 9.0
    }

    private val progressDialogHelper = ProgressDialogHelper(this)
    private lateinit var mapController: MapController

    private lateinit var addressInput: TextView
    private lateinit var addressButton: View
    private lateinit var resultAddress: TextView
    private lateinit var resultCoordinates: TextView

    /**
     * same listener for both [GetLocationByQueryRequest] and [GetLocationByCoordinatesRequest]
     */
    private lateinit var requestListener: RequestListener<LocationStore>

    private val context: Context
        get() = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_geocode)
        addressInput = findViewById(R.id.sample_geocode_address_input)
        addressButton = findViewById(R.id.sample_geocode_address_validate)

        resultAddress = findViewById(R.id.sample_geocode_result_address)
        resultCoordinates = findViewById(R.id.sample_geocode_result_coordinates)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.sample_geocode_map_fragment) as MappyMapFragment

        mapFragment.getMapControllerAsync {
            mapController = it
            mapController.disableBearing()
            mapController.setOnMapLongClickListener(this@GeocodeSample)
            mapController.center(GeoConstants.PARIS, TOWN_ZOOM_LEVEL_FOR_GEOCODE_SAMPLE)
        }

        requestListener = object : RequestListener<LocationStore> {
            override fun onRequestFailure(throwable: Throwable) {
                val message =
                    if (!ConnectivityUtil.isConnected(context)) R.string.routeerror_nonetworkconnexion else R.string.routeerror
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                progressDialogHelper.dismiss()
            }

            override fun onRequestSuccess(result: LocationStore) {
                if (result.mappyLocations.isNotEmpty()) {
                    setResult(result.mappyLocations[0])
                } else {
                    progressDialogHelper.dismiss()
                    Toast.makeText(
                        context,
                        "Votre saisie ne correspond pas à une adresse connue",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        addressButton.setOnClickListener(this)
    }

    override fun onPause() {
        addressButton.setOnClickListener(null)
        super.onPause()
    }

    override fun onClick(view: View) {
        prepareResult()
        val queriedAddress = addressInput.text.toString()
        val params = GetLocationByQueryRequest.Params(
            queriedAddress,
            mapController.boundingBox ?: GeoBounds(),
            extendsBoundingBox = true,
            isForRoute = false,
            filter = GetLocationByQueryRequest.ADDRESS
        )
        MappyDownloadManager.getLocationByQuery(params, requestListener)
    }

    override fun onMapLongClick(point: LatLng): Boolean {
        prepareResult()
        val params = GetLocationByCoordinatesRequest.Params(point)
        MappyDownloadManager.getLocationByCoordinatesWithPanoramicId(params, requestListener)
        return true
    }

    private fun prepareResult() {
        progressDialogHelper.show()
        resultCoordinates.text = ""
        resultAddress.text = ""
        mapController.clear()
    }

    private fun setResult(firstResponse: MappyLocation) {
        val mappyAddress = firstResponse.address

        var addressLabel: String
        val splitAddressLabel = mappyAddress.splitLabel
        if (splitAddressLabel?.isNotEmpty() == true) {
            addressLabel = ""
            var sep = ""
            for (split in splitAddressLabel) {
                addressLabel += sep + split
                sep = "\n"
            }
        } else {
            addressLabel = AddressUtil.getFormattedGeoAddress(mappyAddress)
        }

        val coordinate = firstResponse.coordinate
        resultAddress.text = addressLabel
        resultCoordinates.text = coordinate.toString()
        progressDialogHelper.dismiss()
        mapController.addMarker(coordinate)
        mapController.center(coordinate, false)
        mapController.refreshMarkerSymbolLayer()
    }
}