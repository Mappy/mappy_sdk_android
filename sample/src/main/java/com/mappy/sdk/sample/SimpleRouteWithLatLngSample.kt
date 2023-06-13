package com.mappy.sdk.sample

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.mappy.common.model.LatLng
import com.mappy.map.MapController
import com.mappy.map.MappyMapFragment
import com.mappy.map.MappyPolyline
import com.mappy.map.MappySection
import com.mappy.sdk.sample.utils.ProgressDialogHelper
import com.mappy.legacy.RequestListener
import com.mappy.legacy.utils.MappyRouteRequestBuilder
import com.mappy.webservices.resource.model.dao.MappyMultiPathRoute

class SimpleRouteWithLatLngSample : FragmentActivity() {

    private val progressDialogHelper = ProgressDialogHelper(this)
    private lateinit var mapController: MapController

    private var departure: LatLng? = null
    private var arrival: LatLng? = null

    private lateinit var routesContainer: TextView

    private lateinit var departureSpinner: Spinner
    private lateinit var arrivalSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_route_latlng_picker)

        departureSpinner = findViewById(R.id.sample_route_latlng_picker_departure)
        arrivalSpinner = findViewById(R.id.sample_route_latlng_picker_arrival)

        findViewById<View>(R.id.sample_route_latlng_transport_modes_scroll_view).visibility =
            View.GONE
        routesContainer = findViewById(R.id.sample_route_latlng_routes_container)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.sample_route_latlng_picker_mapFragment) as MappyMapFragment

        mapFragment.getMapControllerAsync {
            mapController = it
            mapController.disableBearing()

            initSpinner(departureSpinner, 0, object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    val latLng = TOWNS_LAT_LNG[position]
                    if (latLng !== departure) {
                        departure = latLng
                        startNewRoute()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    departure = null
                }
            })
            initSpinner(arrivalSpinner, 1, object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    val latLng = TOWNS_LAT_LNG[position]
                    if (latLng !== arrival) {
                        arrival = latLng
                        startNewRoute()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    arrival = null
                }
            })

            departure = TOWNS_LAT_LNG[0]
            arrival = TOWNS_LAT_LNG[1]
            startNewRoute()
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

        spinner.setSelection(selection)
        spinner.onItemSelectedListener = listener
    }

    private fun startNewRoute() {
        // MappyRouteRequestBuilder#requestCarRoute will throw IllegalArgumentException if departure or arrival is null
        val departure = departure ?: return
        val arrival = arrival ?: return

        if (!progressDialogHelper.isShowing()) {
            progressDialogHelper.show()
        }

        routesContainer.text = "Chargement…"
        mapController.clearStylePolyline()
        MappyRouteRequestBuilder()
            .departure(departure, false)
            .arrival(arrival)
            .requestCarRoute(object : RequestListener<List<MappyMultiPathRoute>> {
                override fun onRequestFailure(throwable: Throwable) {
                    // MappyRouteRequestException is there is an error while fetching routes.
                    // or
                    // MappyNoRouteFoundException if there is no route found

                    Toast.makeText(
                        this@SimpleRouteWithLatLngSample,
                        throwable.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    progressDialogHelper.dismiss()
                }

                override fun onRequestSuccess(result: List<MappyMultiPathRoute>) {
                    displayProviderRoutes(result)
                    progressDialogHelper.dismiss()
                }
            })
    }

    private fun displayProviderRoutes(mappyMultiPathRoutes: List<MappyMultiPathRoute>) {
        val size =
            mappyMultiPathRoutes.size // size is strictly positive, if not MappyNoRouteFoundException will be thrown and intercept by onRequestFailure
        val text = StringBuilder("Réponse").append(if (size > 1) "s" else "")
        mappyMultiPathRoutes.forEach {
            text.append("\n  - ").append(size).append(" route")
            if (size > 1) {
                text.append("s")
            }
            text.append("\n      . ")
            putLabelFromMappyMultiPathRoute(it, text).append(it.priceLabel)
            mapController.addStylePolyline(
                MappyPolyline.Builder()
                    .addSection(MappySection.Builder(it).build())
                    .build(), false
            )
        }
        mapController.displayWholeStylePolyLines(true, true, null)
        routesContainer.text = text.toString()
    }

    private fun putLabelFromMappyMultiPathRoute(
        mappyMultiPathRoute: MappyMultiPathRoute,
        text: StringBuilder
    ): StringBuilder {
        val title = mappyMultiPathRoute.title
        if (title.isNullOrEmpty()) {
            return text
        }
        text.append(title).append(" : ")

        val subtitle1 = mappyMultiPathRoute.subtitle1
        if (subtitle1.isNullOrEmpty()) {
            return text
        }
        text.append(subtitle1).append(", ")

        val subtitle2 = mappyMultiPathRoute.subtitle2
        if (subtitle2.isNullOrEmpty()) {
            return text
        }
        text.append(subtitle2).append(", ")

        val subtitle3 = mappyMultiPathRoute.subtitle3
        if (subtitle3.isNullOrEmpty()) {
            return text
        }
        text.append(subtitle3).append(", ")
        return text
    }

    companion object {
        private val TOWNS = arrayOf(
            "Paris",
            "Versailles",
            "Lyon",
            "Marseille",
            "Bordeaux",
            "Lille",
            "Londres",
            "Ajaccio"
        )
        private val TOWNS_LAT_LNG = arrayOf(
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