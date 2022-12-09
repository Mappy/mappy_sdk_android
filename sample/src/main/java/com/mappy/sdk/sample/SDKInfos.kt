package com.mappy.sdk.sample

import android.app.ListActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.mappy.sdk.sample.BuildConfig.VERSION_NAME
import com.mappy.services.utils.PlatformConfig

/**
 * View the URL used.
 */
class SDKInfos : ListActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stringList = arrayOf(
            "SDK_VERSION=" + VERSION_NAME,
            "UrlLocationByCoordinate:" + PlatformConfig.urlLocationByCoordinate,
            "UrlLocationByQuery:" + PlatformConfig.urlLocationByQuery,
            "UrlPanoramic:" + PlatformConfig.urlPanoramic,
            "UrlPoiById:" + PlatformConfig.urlPoiById,
            "UrlSuggestion:" + PlatformConfig.urlSuggestion,
            "UrlAccount:" + PlatformConfig.urlAccount,
            "UrlStyleMappyVecto:" + PlatformConfig.urlStyleVectoBase
        )

        listAdapter = ArrayAdapter(this, R.layout.sdk_infos_layout, stringList)
        listView.isTextFilterEnabled = true
    }
}