package com.mappy.sdk.sample.fragment

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mappy.map.MapController
import com.mappy.map.MappyMapFragment
import com.mappy.sdk.sample.R
import com.mappy.sdk.sample.utils.MultipathApiHelper
import com.mappy.sdk.sample.utils.ProgressDialogHelper
import com.mappy.webservices.resource.model.dao.MappyLocation

class ItineraryFormFragment : Fragment(), LocationGeocodeFragment.LocationGeocodeListener,
    MultipathApiHelper.MultipathApiUser {
    private lateinit var mapController: MapController
    private lateinit var progressDialogHelper: ProgressDialogHelper
    private lateinit var departureEditText: TextView
    private lateinit var arrivalEditText: TextView
    private lateinit var transportModeSliderContainer: LinearLayout
    private lateinit var routeTextContainer: TextView
    private lateinit var label: View

    private var listener: ItineraryFormListener? = null
    private var departureLocation: MappyLocation? = null
    private var arrivalLocation: MappyLocation? = null
    private lateinit var multipathHelper: MultipathApiHelper


    override fun onAttach(context: Context) {
        super.onAttach(context)
        progressDialogHelper = ProgressDialogHelper(context)
        multipathHelper = MultipathApiHelper(context, this)
        listener = context as ItineraryFormListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_itinerary_form, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        label = view.findViewById(R.id.fragment_itinerary_form_label)
        departureEditText = view.findViewById(R.id.fragment_itinerary_form_departure)
        arrivalEditText = view.findViewById(R.id.fragment_itinerary_form_arrival)
        transportModeSliderContainer =
            view.findViewById(R.id.fragment_itinerary_form_slider_transport_modes_container)
        routeTextContainer = view.findViewById(R.id.fragment_itinerary_form_routes_text_container)


        val mapFragment =
            childFragmentManager.findFragmentById(R.id.fragment_itinerary_form_mapFragment) as MappyMapFragment
        mapFragment.getMapControllerAsync {
            mapController = it
            setTextListener()
        }
    }

    private fun setTextListener() {
        val onClickListener = View.OnClickListener {
            openItineraryGeocodeFragment(it.id == R.id.fragment_itinerary_form_departure)
        }
        val onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                label.requestFocus()
                openItineraryGeocodeFragment(v.id == R.id.fragment_itinerary_form_departure)
            }
        }

        departureEditText.setOnClickListener(onClickListener)
        arrivalEditText.setOnClickListener(onClickListener)
        departureEditText.onFocusChangeListener = onFocusChangeListener
        arrivalEditText.onFocusChangeListener = onFocusChangeListener
    }

    private fun openItineraryGeocodeFragment(isDeparture: Boolean) {
        val supportFragmentManager = childFragmentManager
        if (supportFragmentManager.findFragmentByTag(LocationGeocodeFragment.TAG) == null) {
            val previousText =
                (if (isDeparture) departureEditText else arrivalEditText).text.toString()
            val fragment =
                LocationGeocodeFragment.newInstance(isDeparture, previousText, mapController)
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_itinerary_form_container, fragment, LocationGeocodeFragment.TAG)
                .commitAllowingStateLoss()
        }
    }

    private fun validateRouteInput(context: Context, notify: Boolean): Boolean {
        if (departureLocation == null) {
            if (notify) {
                Toast.makeText(context, R.string.routeerror_nostart, Toast.LENGTH_LONG).show()
            }
            return false
        } else if (arrivalLocation == null) {
            if (notify) {
                Toast.makeText(context, R.string.routeerror_noend, Toast.LENGTH_LONG).show()
            }
            return false
        }
        return true
    }

    override fun onSearchDone(location: MappyLocation, isDeparture: Boolean) {
        val editText: TextView
        if (isDeparture) {
            departureLocation = location
            editText = departureEditText
        } else {
            arrivalLocation = location
            editText = arrivalEditText
        }

        mapController.clearPolylines()
        if (validateRouteInput(editText.context, false)) {
            multipathHelper.startTransportModesRequest()
        }

        editText.text = getLabelForLocation(location)

        val supportFragmentManager = childFragmentManager
        val fragment = supportFragmentManager.findFragmentByTag(LocationGeocodeFragment.TAG)
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commitAllowingStateLoss()
        }
    }


    override fun showKeyBoard() {
        listener?.showKeyboard()
    }

    override fun hideKeyBoard() {
        listener?.hideKeyboard()
    }

    private fun getLabelForLocation(mappyLocation: MappyLocation?): String {
        var label = ""
        if (mappyLocation != null) {
            label = mappyLocation.label
            if (!TextUtils.isEmpty(label)) {
                label += ", "
            }
            label += mappyLocation.address.label
        }
        return label
    }

    fun handleBackPressed(): Boolean {
        val supportFragmentManager = childFragmentManager
        val fragment = supportFragmentManager.findFragmentByTag(LocationGeocodeFragment.TAG)
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commitAllowingStateLoss()
            return true
        }
        return false
    }

    override fun showProgressDialog() {
        progressDialogHelper.dismiss()
        progressDialogHelper.show()
    }

    override fun dismissProgressDialog() = progressDialogHelper.dismiss()

    override fun getDeparture() = departureLocation?.coordinate

    override fun getArrival() = arrivalLocation?.coordinate

    override fun getArrivalLabel() = arrivalEditText.text.toString()

    override fun getDepartureLabel() = departureEditText.text.toString()

    override fun getRoutesTextContainer() = routeTextContainer

    override fun getTransportModesContainer() = transportModeSliderContainer

    override fun getMapController() = mapController

    interface ItineraryFormListener {
        fun hideKeyboard()

        fun showKeyboard()
    }
}