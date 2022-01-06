package com.mappy.sdk.sample

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentActivity
import com.mappy.common.model.LatLng
import com.mappy.map.MappySnapShotter
import com.mappy.sdk.sample.utils.ProgressDialogHelper
import java.lang.Double.parseDouble
import java.lang.Float.parseFloat

class SnapShotterSample : FragmentActivity(), View.OnClickListener,
    MappySnapShotter.OnMapSnapShotResult {

    private val progressDialogHelper = ProgressDialogHelper(this)

    private lateinit var latitude: EditText
    private lateinit var longitude: EditText
    private lateinit var zoom: EditText
    private lateinit var snapshot: ImageView
    private lateinit var snapshotContainer: View
    private lateinit var snapShotter: MappySnapShotter

    private val center: LatLng
        get() = LatLng(
            parseDouble(latitude.text.toString()),
            parseDouble(longitude.text.toString())
        )

    private val zoomLevel: Float
        get() = parseFloat(zoom.text.toString())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_snapshotter)

        snapShotter = MappySnapShotter(this, WIDTH_IMAGE, HEIGHT_IMAGE)

        latitude = findViewById(R.id.sample_snapshot_latitude)
        longitude = findViewById(R.id.sample_snapshot_longitude)
        zoom = findViewById(R.id.sample_snapshot_zoom)
        snapshot = findViewById(R.id.sample_snapshot_snapshot)
        snapshotContainer = findViewById(R.id.sample_snapshot_container)

        findViewById<View>(R.id.sample_snapshot_validate).setOnClickListener(this)
    }

    private fun showProgressDialog() {
        progressDialogHelper.dismiss()
        progressDialogHelper.show()
    }

    private fun dismissProgressDialog() {
        progressDialogHelper.dismiss()
    }

    override fun onClick(view: View) {
        showProgressDialog()
        val currentFocus = currentFocus
        if (currentFocus != null) {
            val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                currentFocus.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }

        //SnapShotter must be measurable to avoid dividing by 0
        snapshotContainer.visibility = View.INVISIBLE

        //SnapShotter must be ready before calling start
        snapShotter.start(view.context, center, zoomLevel, this)
    }

    override fun onSuccess(snapshot: Bitmap) {
        this.snapshot.setImageBitmap(snapshot)
        snapshotContainer.visibility = View.VISIBLE
        testListener?.onSuccess(snapshot)
        dismissProgressDialog()
    }

    override fun onError(error: String) {
        Toast.makeText(snapshot.context, error, Toast.LENGTH_LONG).show()
        testListener?.onError(error)
        dismissProgressDialog()
    }

    companion object {
        @VisibleForTesting
        const val HEIGHT_IMAGE = 900

        @VisibleForTesting
        const val WIDTH_IMAGE = 1200

        @VisibleForTesting
        var testListener: MappySnapShotter.OnMapSnapShotResult? = null
    }
}