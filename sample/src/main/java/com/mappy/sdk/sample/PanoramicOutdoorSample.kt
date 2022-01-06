package com.mappy.sdk.sample

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.mappy.common.model.GeoConstants
import com.mappy.common.model.LatLng
import com.mappy.panoramic.OutdoorUrlProvider
import com.mappy.panoramic.PanoramicView
import com.mappy.panoramic.model.PanoramicDescriptor
import com.mappy.utils.Logger

class PanoramicOutdoorSample : FragmentActivity() {

    private lateinit var panoramicView: PanoramicView
    private lateinit var previewImage: ImageView
    private lateinit var panoramicData: TextView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_panoramic)
        panoramicView = findViewById(R.id.panoramic_view)
        previewImage = findViewById(R.id.panoramic_preview_image)
        panoramicData = findViewById(R.id.panoramic_data)

        setPanoramicPreview(GeoConstants.PARIS)

        panoramicView.displayOutdoor(GeoConstants.PARIS, "No connection available")
        panoramicView.setListener(object : PanoramicView.PanoramicListener {
            override fun onPanoramicDisplayed() {
                // Nothing to do
            }

            override fun onPanoramicLoaded(descriptor: PanoramicDescriptor) {
                val location = descriptor.location
                val pointToLookAt = LatLng(location.lat, location.lon)

                panoramicData.text = "${descriptor.address} (${descriptor.location})"
                setPanoramicPreview(pointToLookAt)
            }
        })
    }

    override fun onPause() {
        panoramicView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        panoramicView.onResume()
    }

    private fun setPanoramicPreview(pointToLookAt: LatLng) {
        OutdoorUrlProvider.getPreviewUrl(
            pointToLookAt,
            true,
            object : OutdoorUrlProvider.PreviewUrlListener {
                override fun onOutdoorPreviewUrl(previewUrl: String) {
                    if (!previewUrl.isEmpty()) {
                        previewImage.setImageURI(Uri.parse(previewUrl))
                    }
                }

                override fun onOutdoorPreviewUrlFailed() {
                    Logger.e("Failed to get preview image URL")
                }
            })
    }
}