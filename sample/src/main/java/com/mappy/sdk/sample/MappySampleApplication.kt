package com.mappy.sdk.sample

import androidx.multidex.MultiDexApplication
import com.mappy.MappySDK

class MappySampleApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        MappySDK.initialize(applicationContext)
    }
}
