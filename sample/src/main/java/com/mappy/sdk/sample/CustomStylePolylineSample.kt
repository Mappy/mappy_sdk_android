package com.mappy.sdk.sample

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mappy.common.model.GeoConstants
import com.mappy.common.model.LatLng
import com.mappy.location.LocationUtil
import com.mappy.map.*
import com.mappy.sdk.sample.style.SettingsDataHolder
import com.mappy.sdk.sample.utils.PolylineSampleUtils
import com.mappy.sdk.sample.utils.ProgressDialogHelper
import com.mappy.utils.ZoomConstants
import kotlin.math.abs

/**
 * Display Polyline with different Style [StyleSection]
 */
class CustomStylePolylineSample : AppCompatActivity() {
    private val progressDialogHelper = ProgressDialogHelper(this)
    private var mappyPolyline: MappyPolyline? = null
    private lateinit var mapController: MapController
    private val dataHolder = SettingsDataHolder()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.sample_custom_style_polyline)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.sample_polyline_map_fragment) as MappyMapFragment
        mapFragment.getMapControllerAsync {
            mapController = it
            mapController.disableBearing()
            setLoadRouteListener()

            mapFragment.setRotateGesturesEnabled(true)
            mapFragment.setTiltGesturesEnabled(true)

            if (LocationUtil.canLocalize(this)) {
                mapController.enableTracking()
            }
        }
    }

    private fun setLoadRouteListener() {
        findViewById<View>(R.id.load_route).setOnClickListener {
            displaySettingsDialog(it.context)
        }
    }

    /**
     * Display Settings
     * @param context
     */
    private fun displaySettingsDialog(context: Context) {
        val contentView =
            LayoutInflater.from(context).inflate(R.layout.custom_style_settings_dialog, null)

        loadSettingsViewModel(contentView)

        val dialog = AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog)
            .setView(contentView)
            .create()

        contentView.findViewById<View>(R.id.button_validate).setOnClickListener {
            saveSettingsViewModel(contentView)

            loadRoute(
                dataHolder.withAnimation,
                dataHolder.polyline,
                Color.RED,
                dataHolder.polylineType,
                when {
                    contentView.findViewById<RadioButton>(R.id.radio_dashed).isChecked -> arrayOf(
                        abs(dataHolder.dashedDash),
                        abs(dataHolder.dashedGap)
                    )
                    else -> null
                },
                when {
                    contentView.findViewById<RadioButton>(R.id.radio_dot).isChecked || contentView.findViewById<RadioButton>(
                        R.id.radio_dot_mapbox
                    ).isChecked
                    -> abs(dataHolder.dottedGap)
                    else -> null
                }
            )
            dialog.dismiss()
        }
        dialog.show()
    }


    /**
     * Init Settings Dialog Window components in [contentView] with viewModel data
     */
    private fun loadSettingsViewModel(contentView: View) {
        val arrayAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            PolylineSampleUtils.POLYLINE.values()
        )
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val polylineSpinner: Spinner = contentView.findViewById(R.id.polyline_spinner)
        polylineSpinner.adapter = arrayAdapter
        polylineSpinner.setSelection(dataHolder.polyline.ordinal)

        contentView.findViewById<CheckBox>(R.id.animation).isChecked = dataHolder.withAnimation
        when (dataHolder.polylineType) {
            StyleSection.SOLID -> contentView.findViewById<RadioButton>(R.id.radio_solid).isChecked =
                true
            StyleSection.DASHED -> contentView.findViewById<RadioButton>(R.id.radio_dashed).isChecked =
                true
            StyleSection.DOTTED -> contentView.findViewById<RadioButton>(R.id.radio_dot).isChecked =
                true
            StyleSection.DOTTED_NO_INTERPOLATED -> contentView.findViewById<RadioButton>(R.id.radio_dot_no_interpolated).isChecked =
                true
            StyleSection.DOTTED_MAPBOX -> contentView.findViewById<RadioButton>(R.id.radio_dot_mapbox).isChecked =
                true
        }
        contentView.findViewById<EditText>(R.id.custom_dashed_dash)
            .setText(dataHolder.dashedDash.toString())
        contentView.findViewById<EditText>(R.id.custom_dashed_gap)
            .setText(dataHolder.dashedGap.toString())
        contentView.findViewById<EditText>(R.id.custom_dotted_gap)
            .setText(dataHolder.dottedGap.toString())
    }

    /**
     * Save in viewModel the Settings Dialog Window values selected defined in [contentView]
     */
    private fun saveSettingsViewModel(contentView: View) {
        dataHolder.polyline =
            contentView.findViewById<Spinner>(R.id.polyline_spinner).selectedItem as PolylineSampleUtils.POLYLINE
        dataHolder.withAnimation = contentView.findViewById<CheckBox>(R.id.animation).isChecked
        dataHolder.polylineType = when {
            contentView.findViewById<RadioButton>(R.id.radio_solid).isChecked -> StyleSection.SOLID
            contentView.findViewById<RadioButton>(R.id.radio_dashed).isChecked -> StyleSection.DASHED
            contentView.findViewById<RadioButton>(R.id.radio_dot).isChecked -> StyleSection.DOTTED
            contentView.findViewById<RadioButton>(R.id.radio_dot_no_interpolated).isChecked -> StyleSection.DOTTED_NO_INTERPOLATED
            contentView.findViewById<RadioButton>(R.id.radio_dot_mapbox).isChecked -> StyleSection.DOTTED_MAPBOX
            else -> StyleSection.SOLID
        }
        dataHolder.dashedDash =
            contentView.findViewById<EditText>(R.id.custom_dashed_dash).text.toString()
                .toFloatOrNull() ?: 1.25f
        dataHolder.dashedGap =
            contentView.findViewById<EditText>(R.id.custom_dashed_gap).text.toString()
                .toFloatOrNull() ?: 0.5f
        dataHolder.dottedGap =
            contentView.findViewById<EditText>(R.id.custom_dotted_gap).text.toString()
                .toFloatOrNull() ?: 4.0f
    }

    /**
     * Load Route
     * @param anim          if the draw of polyline is animated or not
     * @param polyline      The polyline (journey) to use
     * @param color         the color of polyline
     * @param style         the style of Polyline
     * @param dashPattern    dash pattern to add in polyline [StyleSection.DASHED]
     * @param dotSpacing    dot spacing to add in polyline [StyleSection.DOTTED]
     */
    private fun loadRoute(
        anim: Boolean,
        polyline: PolylineSampleUtils.POLYLINE,
        @ColorInt color: Int,
        style: StyleSection,
        dashPattern: Array<out Float>?,
        dotSpacing: Float?
    ) {
        clearPolylines()
        mapController.center(GeoConstants.FRANCE.center, ZoomConstants.COUNTRY)
        progressDialogHelper.show()

        val displayRouteTask = Runnable {
            mapController.clearStylePolyline()
            mappyPolyline?.let {
                // hide Polyline if we must want animate it
                mapController.addStylePolyline(it, !anim)
            }
            // display Polyline and animate if asked
            mapController.displayWholeStylePolyLines(true, anim, null)
            progressDialogHelper.dismiss()
        }

        loadRoutes(
            polyline,
            color,
            style,
            dashPattern,
            dotSpacing,
            displayRouteTask
        )
    }

    /**
     * Clear Polylines
     */
    private fun clearPolylines() {
        mapController.clearStylePolyline()
        mappyPolyline = null
    }

    /**
     * get Mappy Polyline with options
     * @param polyline     polyline asked
     * @param color         the color of polyline
     * @param style         the style of Polyline
     * @param dashPattern    dash pattern to add in polyline [StyleSection.DASHED]
     * @param dotSpacing    dot spacing to add in polyline [StyleSection.DOTTED]
     * @param callback      runnable to call after the polyline was build
     */
    private fun loadRoutes(
        polyline: PolylineSampleUtils.POLYLINE,
        @ColorInt color: Int,
        style: StyleSection,
        dashPattern: Array<out Float>?,
        dotSpacing: Float?,
        callback: Runnable?
    ) {
        polyline[this, object : PolylineSampleUtils.PolylineListener {
            override fun onPolylineLoaded(polyline: Array<LatLng>) {
                val maps = mapOf(
                    4F to 3F,
                    10F to 4F,
                    13F to 7F,
                    16F to 10F,
                    19F to 14F,
                    22F to 18F
                )

                val builder = MappyPolyline.Builder()
                    //you can add expressions if you want to customize the lineWidth according
                    //to the level of zoom for example
                    .lineWidth(interpolate(exponential(1.5f), zoom(), *maps.toExpression()))
                    .radiusWidth(
                        interpolate(
                            exponential(1.5f),
                            zoom(),
                            *maps.multiplied(0.5F).toExpression()
                        )
                    )
                    .whiteStrokeWidth(
                        interpolate(
                            exponential(1.5f),
                            zoom(),
                            *maps.multiplied(1.5F).toExpression()
                        )
                    )
                    .withWhiteStrokeForSolid(true)
                    //you can also add expressions if you want to customize the color of the line
                    //With lineGradient you can, for example, put gradient colors according to the
                    //line progress of the polyline
                    .lineGradient(
                        interpolate(
                            linear(), lineProgress(),
                            stop(0f, rgb(6, 1, 255)), // blue
                            stop(0.1f, rgb(59, 118, 227)), // royal blue
                            stop(0.3f, rgb(7, 238, 251)), // cyan
                            stop(0.5f, rgb(0, 255, 42)), // lime
                            stop(0.7f, rgb(255, 252, 0)), // yellow
                            stop(1f, rgb(255, 30, 0)) // red
                        )
                        //Here we add a border to the main layer
                        //you can customize the width of this border
                        //in fact it's a normal layer drawn below the main one
                    )
                    .addSection(
                        MappySection.Builder(polyline)
                            .style(style)
                            .color(color)
                            .width(10.0f)
                            .alpha(0.9f)
                            .build()
                    )

                when (style) {
                    StyleSection.DASHED -> {
                        builder?.dashPattern(dashPattern)
                    }
                    StyleSection.DOTTED, StyleSection.DOTTED_NO_INTERPOLATED, StyleSection.DOTTED_MAPBOX -> {
                        builder?.dotSpacing(dotSpacing)
                    }
                    else -> {
                        //nothing to do here
                    }
                }

                mappyPolyline = builder.build()
                callback?.run()
            }
        }]
    }
}

private fun Map<Float, Float>.multiplied(factor: Float): Map<Float, Float> =
    mutableMapOf<Float, Float>().also {
        for ((k, v) in this) {
            it[k] = v * factor
        }
    }

private fun Map<Float, Float>.toExpression(): Array<Stop> = mutableListOf<Expression.Stop>().also {
    for ((k, v) in this) {
        it.add(stop(k, v))
    }
}.toTypedArray()
