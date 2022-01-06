package com.mappy.sdk.sample

import android.app.Activity
import android.os.Bundle

import com.mappy.common.model.GeoConstants
import com.mappy.gps.MappyGPSIntentBuilder

/**
 * Open GPS
 */
class OpenGPSByIntent : Activity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (MappyGPSIntentBuilder.isAvailable(this)) {
            MappyGPSIntentBuilder(GeoConstants.PARIS)
                .withTransportMode(MappyGPSIntentBuilder.TransportMode.CAR)
                .withCarOption(MappyGPSIntentBuilder.CarOption.FASTEST)
                .startGPS(this)
        } else {
            MappyGPSIntentBuilder.openGPSOnPlayStore(this)
        }
        finish()
    }
}