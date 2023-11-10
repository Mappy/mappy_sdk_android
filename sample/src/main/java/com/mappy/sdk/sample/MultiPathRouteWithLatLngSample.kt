package com.mappy.sdk.sample

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.mappy.common.model.LatLng
import com.mappy.map.MapController
import com.mappy.map.MappyMapFragment
import com.mappy.sdk.sample.utils.MultipathApiHelper
import com.mappy.sdk.sample.utils.ProgressDialogHelper
import com.mappy.utils.Logger

class MultiPathRouteWithLatLngSample : FragmentActivity(), MultipathApiHelper.MultipathApiUser {

    lateinit var internalMapController: MapController
    private val progressDialogHelper = ProgressDialogHelper(this)

    var internalDeparture: LatLng = TOWNS_LAT_LNG[0]
    var internalArrival: LatLng = TOWNS_LAT_LNG[2]

    lateinit var transportModeSliderContainer: LinearLayout
    lateinit var routeTextContainer: TextView

    private lateinit var departureSpinner: Spinner
    private lateinit var arrivalSpinner: Spinner

    private val multipathHelper = MultipathApiHelper(this, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_route_latlng_picker)

        departureSpinner = findViewById(R.id.sample_route_latlng_picker_departure)
        arrivalSpinner = findViewById(R.id.sample_route_latlng_picker_arrival)

        transportModeSliderContainer =
            findViewById(R.id.sample_route_latlng_transport_modes_container)
        routeTextContainer = findViewById(R.id.sample_route_latlng_routes_container)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.sample_route_latlng_picker_mapFragment) as MappyMapFragment
        mapFragment.getMapControllerAsync {
            internalMapController = it
            internalMapController.disableBearing()

            initSpinner(departureSpinner, 0, object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    internalDeparture = TOWNS_LAT_LNG[position]
                    internalMapController.clearPolylines()
                    multipathHelper.startTransportModesRequest()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            })
            initSpinner(arrivalSpinner, 1, object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    internalArrival = TOWNS_LAT_LNG[position]
                    internalMapController.clearPolylines()
                    multipathHelper.startTransportModesRequest()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            })
            multipathHelper.startTransportModesRequest()
        }
    }

    private fun initSpinner(
        spinner: Spinner,
        selection: Int,
        listener: AdapterView.OnItemSelectedListener
    ) {
        val dataAdapter = ArrayAdapter(spinner.context, android.R.layout.simple_spinner_item, TOWNS)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = dataAdapter

        spinner.onItemSelectedListener = listener
        spinner.setSelection(selection)
    }

    override fun showProgressDialog() {
        progressDialogHelper.dismiss()
        if (!isFinishing) {
            progressDialogHelper.show()
        }
    }

    override fun dismissProgressDialog() = progressDialogHelper.dismiss()

    override fun getDeparture() = internalDeparture

    override fun getArrival() = internalArrival

    override fun getArrivalLabel() = arrivalSpinner.selectedItem as String

    override fun getDepartureLabel() = departureSpinner.selectedItem as String

    override fun getRoutesTextContainer() = routeTextContainer

    override fun getTransportModesContainer() = transportModeSliderContainer

    override fun getMapController() = internalMapController

    companion object {
        private val TOWNS = listOf(
            "Paris",
            "Versailles",
            "Lyon",
            "Marseille",
            "Bordeaux",
            "Lille",
            "Londres",
            "Ajaccio"
        )

        private val TOWNS_LAT_LNG = listOf(
            LatLng(48.85, 2.35),
            LatLng(48.804824, 2.120337),
            LatLng(45.767802, 4.836040),
            LatLng(43.338835, 5.408303),
            LatLng(44.837900, -0.579707),
            LatLng(50.630542, 3.071461),
            LatLng(51.51279, -0.09184),
            LatLng(41.918897, 8.736841)
        )
    }
}