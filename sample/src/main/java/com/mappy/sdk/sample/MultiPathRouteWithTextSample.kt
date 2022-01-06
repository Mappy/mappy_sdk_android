package com.mappy.sdk.sample

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity
import com.mappy.map.MappyMapFragment
import com.mappy.sdk.sample.fragment.ItineraryFormFragment

/**
 * Activity to showcase a sample of an itinerary , must extend MappySpiceActivity to use an
 * instantiated download manager
 */
class MultiPathRouteWithTextSample : FragmentActivity(),
    ItineraryFormFragment.ItineraryFormListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_route_text)

        val fragment = supportFragmentManager.findFragmentById(R.id.itinerary_form_fragment)!!

        val mapFragment =
            fragment.childFragmentManager.findFragmentById(R.id.fragment_itinerary_form_mapFragment) as MappyMapFragment

        mapFragment.getMapControllerAsync {
            it.disableBearing()
        }
    }

    override fun hideKeyboard() {
        currentFocus?.let {
            val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    override fun showKeyboard() {
        currentFocus?.let {
            val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onBackPressed() {
        val itineraryFormFragment =
            supportFragmentManager.findFragmentById(R.id.itinerary_form_fragment) as ItineraryFormFragment
        if (!itineraryFormFragment.handleBackPressed()) {
            super.onBackPressed()
        }
    }
}