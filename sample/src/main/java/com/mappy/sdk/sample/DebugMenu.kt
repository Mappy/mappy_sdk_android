package com.mappy.sdk.sample

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.mappy.services.utils.PlatformConfig

class DebugMenu : Activity(), AdapterView.OnItemSelectedListener {
    companion object {
        val FROM = arrayOf("title", "message")
        val TO = intArrayOf(android.R.id.text1, android.R.id.text2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug_select_platform)

        val platformSpinner = findViewById<Spinner>(R.id.platform_list)

        val platformList = ArrayList<String>()
        for (platform in PlatformConfig.Platform.values()) {
            platformList.add(platform.toString())
        }

        val platformListAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, platformList)
        platformListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        platformSpinner.adapter = platformListAdapter

        val selectedPlatformName = PlatformConfig.getPlatform(this)
        val selectedPlatformOrdinal = PlatformConfig.Platform.valueOf(selectedPlatformName).ordinal
        platformSpinner.setSelection(selectedPlatformOrdinal)
        platformSpinner.onItemSelectedListener = this

        updateSdkPropertiesListView()
    }


    private fun updateSdkPropertiesListView() {
        val sdkPropertiesListView = findViewById<ListView>(R.id.sdk_properties_list)
        sdkPropertiesListView.adapter =
            SimpleAdapter(this, getPlatformUrls(), android.R.layout.simple_list_item_2, FROM, TO)
    }

    private fun getPlatformUrls() = arrayListOf(
        add(FROM, "url_panoramic", PlatformConfig.urlPanoramic),
        add(FROM, "url_location_by_query", PlatformConfig.urlLocationByQuery),
        add(FROM, "url_location_by_coordinate", PlatformConfig.urlLocationByCoordinate),
        add(FROM, "url_categories_menu", PlatformConfig.urlCategoriesMenu),
        add(FROM, "url_poi_by_id", PlatformConfig.urlPoiById),
        add(FROM, "url_pois_by_journey", PlatformConfig.urlPoisByJourney),
        add(FROM, "url_suggestion", PlatformConfig.urlSuggestion),
        add(FROM, "url_style_vecto_mappy", PlatformConfig.urlStyleVectoBase),
        add(FROM, "url_multipath_transports", PlatformConfig.urlMultiPathTransports),
        add(FROM, "url_multipath_routes", PlatformConfig.urlMultiPathRoutes),
        add(FROM, "url_multipath_roadbook", PlatformConfig.urlMultiPathRoadBook),
        add(FROM, "url_multipath_transport_modes", PlatformConfig.urlMultiPathTransportModes),
        add(FROM, "url_vehicles", PlatformConfig.urlVehicles),
        add(FROM, "url_inventory", PlatformConfig.urlInventory),
        add(FROM, "url_gps_routing_server", PlatformConfig.urlGPSRoutingServer),
        add(FROM, "url_hotel_rooms", PlatformConfig.urlHotelRooms),
        add(FROM, "url_panoramic_preview", PlatformConfig.urlPanoramicPreview),
        add(FROM, "url_poi_by_rubrics", PlatformConfig.urlPoisByRubric)
    )

    private fun add(from: Array<String>, name: String, value: String): HashMap<String, Any> {
        val map = HashMap<String, Any>(2)
        map[from[0]] = name
        map[from[1]] = value
        return map
    }

    override fun onNothingSelected(adapterView: AdapterView<*>) {}

    override fun onItemSelected(adapterView: AdapterView<*>, view: View, pos: Int, id: Long) {
        val selectedPlatform = PlatformConfig.Platform.values()[pos]
        PlatformConfig.overridePlatform(view.context, selectedPlatform.name)
        updateSdkPropertiesListView()
    }
}