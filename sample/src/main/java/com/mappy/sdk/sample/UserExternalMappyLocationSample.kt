package com.mappy.sdk.sample

import com.mappy.location.LocationUtil
import com.mappy.location.MappyLocationManager
import com.mappy.location.MappyLocationProvider

class UserExternalMappyLocationSample : UserExternalLocationSample() {

    override fun createExternalLocationProvider(): MappyLocationProvider {
        return MappyLocationManager.getInstance()
    }

    override fun notifyLocationAvailable() {
        LocationUtil.resumeLocation(this)
        super.notifyLocationAvailable()
    }
}
