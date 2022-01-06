package com.mappy.sdk.sample.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.mappy.sdk.sample.R
import com.mappy.utils.MarkerBitmapUtils

/**
 * utilitarian class to manage bitmap for the samples.
 */
object MappyBitmapUtils {
    /**
     * create a mappy rubric marker bitmap from two drawables :<br></br>
     *
     *  * `R.drawable.map_marker_empty` which is a bitmap
     *  * `rubricIconResId` which can be a bitmap or a vector bitmap
     *
     * The bitmaps are cached to be reused instead of being recreated if they are needed again.
     *
     * @param context         Context
     * @param rubricIconResId int, resource to use as overlay of `map_marker_empty`
     * @return Bitmap
     */
    fun getRubricMarkerBitmap(context: Context, rubricIconResId: Int): Bitmap {
        val rubricImageKey = rubricIconResId.toString() + "_rubric_marker"
        var rubricIconBitmap = MarkerBitmapUtils.getBitmapFromMemCache(rubricImageKey)
        if (rubricIconBitmap == null) {
            val emptyImageKey = R.drawable.map_marker_empty.toString()
            val bitmap = MarkerBitmapUtils.getBitmapFromMemCache(emptyImageKey)
                ?: MarkerBitmapUtils.getBitmap(context, R.drawable.map_marker_empty)!!
            val resImageKey = rubricIconResId.toString()
            val overlay = MarkerBitmapUtils.getBitmapFromMemCache(resImageKey)
                ?: getOverlay(context, rubricIconResId)

            val xOffset = (bitmap.width - overlay.width) / 2
            val yOffset = (bitmap.height - overlay.height) / 2 - bitmap.height / 9
            rubricIconBitmap = overlay(bitmap, overlay, xOffset, yOffset)
            MarkerBitmapUtils.addBitmapToMemoryCache(rubricImageKey, rubricIconBitmap)
        }

        return rubricIconBitmap
    }

    private fun getOverlay(context: Context, rubricIconResId: Int): Bitmap {
        val drawable = AppCompatResources.getDrawable(context, rubricIconResId)
        val widthAndHeight = context.resources.getDimensionPixelSize(R.dimen.marker_content_size)
        val overlay = drawableToBitmap(drawable!!, widthAndHeight, widthAndHeight)
        MarkerBitmapUtils.addBitmapToMemoryCache(rubricIconResId.toString(), overlay)
        return overlay
    }

    fun overlay(bitmap: Bitmap, overlay: Bitmap, xOffset: Int, yOffset: Int): Bitmap {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(overlay, xOffset.toFloat(), yOffset.toFloat(), paint)
        return bitmap
    }


    private fun drawableToBitmap(drawable: Drawable, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}