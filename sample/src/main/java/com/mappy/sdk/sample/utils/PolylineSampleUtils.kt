package com.mappy.sdk.sample.utils

import android.content.Context
import android.os.Handler
import com.mappy.common.model.LatLng
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread

class PolylineSampleUtils {

    /**
     *
     * holder of the PolyLines.
     *
     *knows how to build the PolyLines from the csv files in
     * assets for simple use.
     */
    enum class POLYLINE(private val mFileName: String) {
        BREST_BUCAREST("brest_bucarest"),
        ISSY_CRETEIL("issy_creteil"),
        LILLE_TOULOUSE("lille_toulouse"),
        LIMOURS_MOSCOU("limours_moscou"),
        PARIS_LIMOURS("paris_limours"),
        PARIS_MARSEILLE("paris_marseille"),
        STRASBOURG_BORDEAUX("strasbourg_bordeaux"),
        PARIS_INTRA("paris_intra");

        private var data: Array<LatLng>? = null
        private var initializing = false

        /**
         * get the PolyLine data in another Thread
         *
         * @param context  Context to open the assets
         * @param listener PolylineListener to be notified when the data is available
         */
        operator fun get(context: Context, listener: PolylineListener) {
            if (data != null) {
                listener.onPolylineLoaded(data!!)
            } else if (!initializing) {
                initializing = true
                val handler = Handler()
                thread {
                    data = parseCSVFile(context, "polylines/$mFileName")
                    handler.post { listener.onPolylineLoaded(data!!) }
                }
            }
        }
    }

    /**
     *
     * holder of the Polygons.
     *
     *knows how to build the Polygons from the csv files in
     * assets for simple use.
     */
    enum class POLYGON(private val fileName: String) {
        PARIS_01("Paris01");

        private var data: Array<LatLng>? = null
        private var mIsInitializing = false


        /**
         * get the Polygon data in another Thread
         *
         * @param context  Context to open the assets
         * @param listener PolylineListener to be notified when the data is available
         */
        operator fun get(context: Context, listener: PolygonListener) {
            if (data != null) {
                listener.onPolygonLoaded(data!!)
            } else if (!mIsInitializing) {
                mIsInitializing = true
                val handler = Handler()
                thread {
                    data = parseCSVFile(context, "polygons/$fileName")
                    handler.post { listener.onPolygonLoaded(data!!) }
                }
            }
        }
    }

    interface PolylineListener {
        fun onPolylineLoaded(polyline: Array<LatLng>)
    }

    interface PolygonListener {
        fun onPolygonLoaded(polygon: Array<LatLng>)
    }

    companion object {
        private fun parseCSVFile(context: Context, fileName: String): Array<LatLng>? {
            val settings = CsvParserSettings()
            settings.format.setLineSeparator("\n")
            val parser = CsvParser(settings)
            var reader: BufferedReader? = null

            try {
                reader = BufferedReader(InputStreamReader(context.assets.open("$fileName.csv")))
                val allRows = parser.parseAll(reader)

                return Array(allRows.size, { getLatLngFromString(allRows[it]) })
            } finally {
                reader?.close()
            }
        }

        private fun getLatLngFromString(string: Array<String>): LatLng {
            val latLong =
                string[0].split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val lat = java.lang.Double.parseDouble(latLong[0])
            val lng = java.lang.Double.parseDouble(latLong[1])
            return LatLng(lat, lng)
        }
    }
}