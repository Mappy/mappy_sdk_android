package com.mappy.sdk.sample.style

import com.mappy.map.StyleSection
import com.mappy.sdk.sample.utils.PolylineSampleUtils

data class SettingsDataHolder (
    var polyline: PolylineSampleUtils.POLYLINE = PolylineSampleUtils.POLYLINE.BREST_BUCAREST,
    var withAnimation: Boolean = false,
    var polylineType: StyleSection = StyleSection.SOLID,
    var dashedDash: Float = 1.25f,
    var dashedGap: Float = 0.5f,
    var dottedGap: Float = 4.0f,
)