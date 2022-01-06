package com.mappy.sdk.sample

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import com.mappy.map.MappyMapFragment
import com.mappy.utils.Logger

class EarlyInitSample : FragmentActivity() {

    private lateinit var mapStatusLabel: TextView
    private lateinit var prepareMapPixel: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_early_init)

        val openMap = findViewById<View>(R.id.sample_early_init_open_map)
        openMap.setOnClickListener {
            startActivity(Intent(this, HelloMapSample::class.java))
            finish()
        }

        mapStatusLabel = findViewById(R.id.sample_early_init_map_status)
        prepareMapPixel = findViewById(R.id.sample_early_init_map_pixel)
    }

    override fun onResume() {
        super.onResume()
        Logger.d("Resuming activity")
        supportFragmentManager.findFragmentByTag(MappyMapFragment::class.java.name)
            ?.let { onMapReady() } ?: run { initializeSdk() }
    }

    private fun initializeSdk(): Boolean {
        Logger.d("Beginning SDK initialization")
        val mapFragment = MappyMapFragment.newBuilder().build()
        setSdkReadyCallback(mapFragment)

        supportFragmentManager.beginTransaction()
            .add(R.id.sample_early_init_map_pixel, mapFragment, MappyMapFragment::class.java.name)
            .commit()
        prepareMapPixel.visibility = View.VISIBLE
        return prepareMapPixel.post { prepareMapPixel.visibility = View.GONE }
    }

    /** Optional, only useful to be notified when SDK is ready */
    private fun setSdkReadyCallback(mapFragment: MappyMapFragment) {
        mapFragment.getMapControllerAsync { mapController ->
            mapController.setOnMapFullyRenderedListener {
                onMapReady()
            }
        }
    }

    private fun onMapReady() {
        Logger.d("SDK is ready")
        mapStatusLabel.setTextColor(
            ResourcesCompat.getColor(
                resources,
                R.color.sample_map_ready_color,
                theme
            )
        )
        mapStatusLabel.setText(R.string.status_map_ready)
    }
}