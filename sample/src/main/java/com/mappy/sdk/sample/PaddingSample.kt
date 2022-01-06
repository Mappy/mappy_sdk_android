package com.mappy.sdk.sample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.mappy.common.model.GeoBounds
import com.mappy.common.model.LatLng
import com.mappy.map.MapController
import com.mappy.map.MappyMapFragment
import com.mappy.sdk.sample.utils.PaddingSeekBar

class PaddingSample : FragmentActivity() {
    private lateinit var mapController: MapController

    private lateinit var horizontalPaddingSeekBar: PaddingSeekBar
    private lateinit var verticalPaddingSeekBar: PaddingSeekBar

    private lateinit var paddingLeft: View
    private lateinit var paddingTop: View
    private lateinit var paddingRight: View
    private lateinit var paddingBottom: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_padding)

        paddingLeft = findViewById(R.id.sample_padding_left)
        paddingTop = findViewById(R.id.sample_padding_top)
        paddingRight = findViewById(R.id.sample_padding_right)
        paddingBottom = findViewById(R.id.sample_padding_bottom)

        horizontalPaddingSeekBar =
            findViewById<View>(R.id.sample_padding_horizontal_padding) as PaddingSeekBar
        horizontalPaddingSeekBar.setRangeValues(minPercent, maxPercent)
        horizontalPaddingSeekBar.setNotifyWhileDragging(true)

        verticalPaddingSeekBar =
            findViewById<View>(R.id.sample_padding_vertical_padding) as PaddingSeekBar
        verticalPaddingSeekBar.setRangeValues(minPercent, maxPercent)
        verticalPaddingSeekBar.setNotifyWhileDragging(true)

        val mapFragment = MappyMapFragment.newBuilder()
            .padding(resources.getDimensionPixelSize(R.dimen.padding_sample_default_padding))
            .build()

        supportFragmentManager.beginTransaction()
            .replace(R.id.sample_padding_map_container, mapFragment)
            .commitAllowingStateLoss()

        mapFragment.getMapControllerAsync {
            mapController = it
            mapFragment.setTiltGesturesEnabled(true)
            mapFragment.setRotateGesturesEnabled(true)
        }

        findViewById<View>(R.id.sample_padding_center_btn).setOnClickListener {
            mapController.clearMarkers()
            mapController.center(
                GeoBounds(
                    LatLng(48.905509, 2.418961),
                    LatLng(48.819746, 2.238721)
                ).center, true
            )
        }
    }


    override fun onResume() {
        super.onResume()
        horizontalPaddingSeekBar.setOnRangeSeekBarChangeListener(object :
            PaddingSeekBar.OnRangeSeekBarChangeListener {
            override fun onRangeSeekBarValuesChanged(
                bar: PaddingSeekBar,
                minValue: Int,
                maxValue: Int
            ) {
                val measuredWidth = horizontalPaddingSeekBar.measuredWidth

                val left = measuredWidth * minValue / maxPercent
                val right = measuredWidth * (maxPercent - maxValue) / maxPercent
                setDimensionToView(paddingLeft, left, paddingLeft.height)
                setDimensionToView(paddingRight, right, paddingRight.height)

                mapController.setPadding(
                    paddingLeft.width,
                    paddingTop.measuredHeight,
                    paddingRight.width,
                    paddingBottom.measuredHeight
                )
            }
        })
        verticalPaddingSeekBar.setOnRangeSeekBarChangeListener(object :
            PaddingSeekBar.OnRangeSeekBarChangeListener {
            override fun onRangeSeekBarValuesChanged(
                bar: PaddingSeekBar,
                minValue: Int,
                maxValue: Int
            ) {
                val measuredWidth = horizontalPaddingSeekBar.measuredWidth

                val top = measuredWidth * minValue / maxPercent
                val bottom = measuredWidth * (maxPercent - maxValue) / maxPercent
                setDimensionToView(paddingTop, paddingTop.width, top)
                setDimensionToView(paddingBottom, paddingBottom.width, bottom)

                mapController.setPadding(
                    paddingLeft.measuredWidth,
                    paddingTop.height,
                    paddingRight.measuredWidth,
                    paddingBottom.height
                )
            }
        })
    }

    private fun setDimensionToView(view: View, width: Int, height: Int) {
        val layoutParams = view.layoutParams
        layoutParams.width = width
        layoutParams.height = height
        view.layoutParams = layoutParams
    }

    override fun onPause() {
        horizontalPaddingSeekBar.setOnRangeSeekBarChangeListener(null)
        verticalPaddingSeekBar.setOnRangeSeekBarChangeListener(null)
        super.onPause()
    }

    companion object {
        private const val minPercent = 0
        private const val maxPercent = 100
    }
}
