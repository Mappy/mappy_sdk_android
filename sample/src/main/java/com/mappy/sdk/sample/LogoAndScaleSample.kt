package com.mappy.sdk.sample

import android.os.Bundle
import android.view.View
import androidx.annotation.IntDef
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.mappy.map.MapController
import com.mappy.map.MappyMapFragment

class LogoAndScaleSample : AppCompatActivity() {

    companion object {
        const val LOGO_TOP_LEFT = 0
        const val LOGO_TOP_RIGHT = 1
        const val LOGO_BOTTOM_LEFT = 2
        const val LOGO_BOTTOM_RIGHT = 3
    }

    private var withScale = true;
    private var mapController: MapController? = null

    @IntDef(LOGO_TOP_LEFT, LOGO_TOP_RIGHT, LOGO_BOTTOM_LEFT, LOGO_BOTTOM_RIGHT)
    private annotation class LogoAndScaleTyoe

    private lateinit var actionBar: ActionBar

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.sample_logo_and_scale)

        initActionBar()

        findViewById<View>(R.id.sample_logo_and_scale_display).setOnClickListener {
            withScale = !withScale
            if (withScale) {
                mapController?.showScale()
            } else {
                mapController?.hideScale()
            }
        }

        findViewById<View>(R.id.sample_logo_and_scale_display_time).setOnClickListener {
            mapController?.showScaleAndAutoHideAfter(1000 * 60)
        }
    }

    private fun initActionBar() {
        supportActionBar?.let {
            actionBar = it
            actionBar.navigationMode = ActionBar.NAVIGATION_MODE_TABS
            addNewTab("Top Left", LOGO_TOP_LEFT)
            addNewTab("Top right", LOGO_TOP_RIGHT)
            addNewTab("Bottom left", LOGO_BOTTOM_LEFT)
            addNewTab("Bottom right", LOGO_BOTTOM_RIGHT)
        }
    }

    private fun addNewTab(title: String, @LogoAndScaleTyoe logoAndScaleType: Int) {
        actionBar.addTab(actionBar.newTab()
            .setText(title)
            .setTabListener(object : ActionBar.TabListener {

                override fun onTabSelected(
                    tab: ActionBar.Tab,
                    fragmentTransaction: FragmentTransaction
                ) {
                    val logoAndScale = when (logoAndScaleType) {
                        LOGO_TOP_LEFT -> MappyMapFragment.Builder.LOGO_LOCATION_TOP_LEFT
                        LOGO_TOP_RIGHT -> MappyMapFragment.Builder.LOGO_LOCATION_TOP_RIGHT
                        LOGO_BOTTOM_LEFT -> MappyMapFragment.Builder.LOGO_LOCATION_BOTTOM_LEFT
                        LOGO_BOTTOM_RIGHT -> MappyMapFragment.Builder.LOGO_LOCATION_BOTTOM_RIGHT
                        else -> throw IllegalArgumentException("unknown case")
                    }

                    val mapFragment = MappyMapFragment.newBuilder()
                        .logoLocation(logoAndScale)
                        .withScale(withScale)
                        .build()

                    fragmentTransaction.replace(
                        R.id.sample_logo_and_scale_map_container,
                        mapFragment
                    )

                    mapFragment.getMapControllerAsync {
                        mapController = it
                    }
                }

                override fun onTabUnselected(
                    tab: ActionBar.Tab,
                    fragmentTransaction: FragmentTransaction
                ) {
                }

                override fun onTabReselected(
                    tab: ActionBar.Tab,
                    fragmentTransaction: FragmentTransaction
                ) {
                }
            })
        )
    }
}