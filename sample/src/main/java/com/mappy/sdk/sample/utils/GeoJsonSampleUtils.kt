package com.mappy.sdk.sample.utils

import android.content.Context
import android.os.Handler
import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.io.StringWriter
import kotlin.concurrent.thread

object GeoJsonSampleUtils {
    /**
     *
     * holder of the GeoJson.
     *
     * knows how to build the GeoJson from the json files in assets for simple use.
     */
    enum class GeoJson(private val mFileName: String) {
        SIMPLE_POINT("simple_point"),
        SIMPLE_LINESTRING("simple_linestring"),
        SIMPLE_POLYGON("simple_polygon");

        private var json: String? = null
        private var initializing = false

        /**
         * get the GeoJson data async
         *
         * @param context  Context to open the assets
         * @param listener GeoJsonListener to be notified when the data is available
         */
        operator fun get(context: Context, listener: GeoJsonListener) {
            if (!json.isNullOrEmpty()) {
                listener.onGeoJsonLoaded(json!!)
            } else if (!initializing) {
                initializing = true
                val handler = Handler()
                thread {
                    json = parseFile(context, "geojson/$mFileName")
                    handler.post {
                        listener.onGeoJsonLoaded(json!!)
                        initializing = false
                    }
                }
            }
        }
    }

    private fun parseFile(context: Context, fileName: String): String? {
        var inputStream: InputStream? = null
        var json: String? = null

        try {
            inputStream = context.assets.open("$fileName.json")
            val writer = StringWriter()
            IOUtils.copy(inputStream!!, writer, "UTF-8")
            json = writer.toString()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            IOUtils.closeQuietly(inputStream)
        }
        return json
    }

    interface GeoJsonListener {
        fun onGeoJsonLoaded(json: String)
    }
}