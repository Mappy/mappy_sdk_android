package com.mappy.sdk.sample.utils

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.ImageView
import com.mappy.sdk.sample.R


/**
 * Widget that lets users select a minimum and maximum value on a given numerical range.
 * The range value types can be one of Long, Double, Integer, Float, Short, Byte or BigDecimal.<br>
 * <br>
 * Improved {@link MotionEvent} handling for smoother use, anti-aliased painting for improved aesthetics.
 *
 * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
 * @author Peter Sinnott (psinnott@gmail.com)
 * @author Thomas Barrasso (tbarrasso@sevenplusandroid.org)
 * @author Alex Florescu (florescu@yahoo-inc.com)
 * @author Michael Keppler (bananeweizen@gmx.de)
 */
class PaddingSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ImageView(context, attrs, defStyle) {

    companion object {
        const val DEFAULT_MINIMUM = 0
        const val DEFAULT_MAXIMUM = 100
        const val HEIGHT_IN_DP = 30
        val DEFAULT_COLOR = Color.argb(0xFF, 0x33, 0xB5, 0xE5)

        const val INVALID_POINTER_ID = 255
        const val ACTION_POINTER_INDEX_MASK = 0x0000ff00
        const val ACTION_POINTER_INDEX_SHIFT = 8
    }

    private val initialPadding = PixelUtil.dpToPx(context, 8).toFloat()
    private val lineHeight = PixelUtil.dpToPx(context, 1).toFloat()

    private val thumbImage = BitmapFactory.decodeResource(resources, R.drawable.seek_thumb_normal)
    private val thumbPressedImage =
        BitmapFactory.decodeResource(resources, R.drawable.seek_thumb_pressed)
    private val thumbDisabledImage =
        BitmapFactory.decodeResource(resources, R.drawable.seek_thumb_disabled)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbWidth = thumbImage.width.toFloat()
    private val thumbHalfWidth = 0.5f * thumbWidth
    private val thumbHalfHeight = 0.5f * thumbImage.height

    private var padding = 0F
    private var absoluteMinValue = 0
    private var absoluteMaxValue = 0
    private var absoluteMinValuePrim = 0.0
    private var absoluteMaxValuePrim = 0.0
    private var normalizedMinValue = 0.0
    private var normalizedMaxValue = 1.0
    private var pressedThumb: Thumb? = null
    private var notifyWhileDragging = false
    private var listener: OnRangeSeekBarChangeListener? = null


    private var downMotionX = 0F

    private var activePointerId = INVALID_POINTER_ID

    private var scaledTouchSlop = 0

    private var dragging = false

    private val textSize = PixelUtil.dpToPx(context, 14).toFloat()
    private val distanceToTop = PixelUtil.dpToPx(context, 8)
    private val textOffset = textSize + PixelUtil.dpToPx(context, 8) + distanceToTop
    private var rect: RectF? = null

    private var singleThumb: Boolean = false

    init {
        if (attrs == null) {// only used to set default values when initialised from XML without any values specified
            absoluteMinValue = DEFAULT_MINIMUM
            absoluteMaxValue = DEFAULT_MAXIMUM
        } else {
            val a =
                getContext().obtainStyledAttributes(attrs, R.styleable.PaddingSeekBar, defStyle, 0)
            singleThumb = a.getBoolean(R.styleable.PaddingSeekBar_singleThumb, false)
            a.recycle()
        }

        setValuePrimAndNumberType()

        rect = RectF(
            padding,
            textOffset + thumbHalfHeight - lineHeight / 2,
            width - padding,
            textOffset.toFloat() + thumbHalfHeight + lineHeight / 2
        )

        // make RangeSeekBar focusable. This solves focus handling issues in case EditText widgets are being used along with the RangeSeekBar within ScollViews.
        isFocusable = true
        isFocusableInTouchMode = true
        scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    fun setRangeValues(minValue: Int, maxValue: Int) {
        absoluteMinValue = minValue
        absoluteMaxValue = maxValue
        setValuePrimAndNumberType()
    }

    private fun setValuePrimAndNumberType() {
        absoluteMinValuePrim = absoluteMinValue.toDouble()
        absoluteMaxValuePrim = absoluteMaxValue.toDouble()
    }

    fun resetSelectedValues() {
        setSelectedMinValue(absoluteMinValue)
        setSelectedMaxValue(absoluteMaxValue)
    }

    fun isNotifyWhileDragging(): Boolean {
        return notifyWhileDragging
    }

    /**
     * Should the widget notify the listener callback while the user is still dragging a thumb? Default is false.
     */
    fun setNotifyWhileDragging(flag: Boolean) {
        notifyWhileDragging = flag
    }

    /**
     * Returns the absolute minimum value of the range that has been set at construction time.
     *
     * @return The absolute minimum value of the range.
     */
    fun getAbsoluteMinValue() = absoluteMinValue

    /**
     * Returns the absolute maximum value of the range that has been set at construction time.
     *
     * @return The absolute maximum value of the range.
     */
    fun getAbsoluteMaxValue() = absoluteMaxValue

    /**
     * Returns the currently selected min value.
     *
     * @return The currently selected min value.
     */
    fun getSelectedMinValue() = normalizedToValue(normalizedMinValue)

    /**
     * Sets the currently selected minimum value. The widget will be invalidated and redrawn.
     *
     * @param value The Number value to set the minimum value to. Will be clamped to given absolute minimum/maximum range.
     */
    fun setSelectedMinValue(value: Int) {
        // in case absoluteMinValue == absoluteMaxValue, avoid division by zero when normalizing.
        if (0.0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
            setNormalizedMinValue(0.0)
        } else {
            setNormalizedMinValue(valueToNormalized(value).toDouble())
        }
    }

    /**
     * Returns the currently selected max value.
     *
     * @return The currently selected max value.
     */
    fun getSelectedMaxValue() = normalizedToValue(normalizedMaxValue)

    /**
     * Sets the currently selected maximum value. The widget will be invalidated and redrawn.
     *
     * @param value The Number value to set the maximum value to. Will be clamped to given absolute minimum/maximum range.
     */
    fun setSelectedMaxValue(value: Int) {
        // in case absoluteMinValue == absoluteMaxValue, avoid division by zero when normalizing.
        if (0.0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
            setNormalizedMaxValue(1.0)
        } else {
            setNormalizedMaxValue(valueToNormalized(value).toDouble())
        }
    }

    /**
     * Registers given listener callback to notify about changed selected values.
     *
     * @param listener The listener to notify about changed selected values.
     */
    fun setOnRangeSeekBarChangeListener(listener: OnRangeSeekBarChangeListener?) {
        this.listener = listener
    }

    /**
     * Handles thumb selection and movement. Notifies listener callback on certain events.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        val action = event.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                // Remember where the motion event started
                activePointerId = event.getPointerId(event.pointerCount - 1)
                val pointerIndex = event.findPointerIndex(activePointerId)
                downMotionX = event.getX(pointerIndex)

                pressedThumb = evalPressedThumb(downMotionX)

                // Only handle thumb presses.
                if (pressedThumb == null) {
                    return super.onTouchEvent(event)
                }

                isPressed = true
                invalidate()
                onStartTrackingTouch()
                trackTouchEvent(event)
                attemptClaimDrag()
            }
            MotionEvent.ACTION_MOVE -> if (pressedThumb != null) {

                if (dragging) {
                    trackTouchEvent(event)
                } else {
                    // Scroll to follow the motion event
                    val pointerIndex = event.findPointerIndex(activePointerId)
                    val x = event.getX(pointerIndex)

                    if (Math.abs(x - downMotionX) > scaledTouchSlop) {
                        isPressed = true
                        invalidate()
                        onStartTrackingTouch()
                        trackTouchEvent(event)
                        attemptClaimDrag()
                    }
                }

                if (notifyWhileDragging && listener != null) {
                    listener!!.onRangeSeekBarValuesChanged(
                        this,
                        getSelectedMinValue(),
                        getSelectedMaxValue()
                    )
                }
            }
            MotionEvent.ACTION_UP -> {
                if (dragging) {
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                    isPressed = false
                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should be interpreted as a tap-seek to that location.
                    onStartTrackingTouch()
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                }

                pressedThumb = null
                invalidate()
                if (listener != null) {
                    listener!!.onRangeSeekBarValuesChanged(
                        this,
                        getSelectedMinValue(),
                        getSelectedMaxValue()
                    )
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.pointerCount - 1
                downMotionX = event.getX(index)
                activePointerId = event.getPointerId(index)
                invalidate()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (dragging) {
                    onStopTrackingTouch()
                    isPressed = false
                }
                invalidate() // see above explanation
            }
        }
        return true
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex =
            ev.action and ACTION_POINTER_INDEX_MASK shr ACTION_POINTER_INDEX_SHIFT //shr = shift right

        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == activePointerId) {
            // This was our active pointer going up. Choose
            // a new active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            downMotionX = ev.getX(newPointerIndex)
            activePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    private fun trackTouchEvent(event: MotionEvent) {
        val pointerIndex = event.findPointerIndex(activePointerId)
        val x = event.getX(pointerIndex)

        if (Thumb.MIN == pressedThumb && !singleThumb) {
            setNormalizedMinValue(screenToNormalized(x))
        } else if (Thumb.MAX == pressedThumb) {
            setNormalizedMaxValue(screenToNormalized(x))
        }
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any ancestors from stealing events in the drag.
     */
    private fun attemptClaimDrag() = parent?.requestDisallowInterceptTouchEvent(true)

    /**
     * This is called when the user has started touching this widget.
     */
    internal fun onStartTrackingTouch() {
        dragging = true
    }

    /**
     * This is called when the user either releases his touch or the touch is canceled.
     */
    internal fun onStopTrackingTouch() {
        dragging = false
    }

    /**
     * Ensures correct size of the widget.
     */
    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = 200
        if (View.MeasureSpec.UNSPECIFIED != View.MeasureSpec.getMode(widthMeasureSpec)) {
            width = View.MeasureSpec.getSize(widthMeasureSpec)
        }

        var height = thumbImage.height + PixelUtil.dpToPx(context, HEIGHT_IN_DP)
        if (View.MeasureSpec.UNSPECIFIED != View.MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, View.MeasureSpec.getSize(heightMeasureSpec))
        }
        setMeasuredDimension(width, height)
    }

    /**
     * Draws the widget on the given canvas.
     */
    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.textSize = textSize
        paint.style = Paint.Style.FILL
        paint.color = Color.GRAY
        paint.isAntiAlias = true

        // draw min and max labels
        val minLabel = context.getString(R.string.sample_padding_min)
        val maxLabel = context.getString(R.string.sample_padding_max)
        val minMaxLabelSize = Math.max(paint.measureText(minLabel), paint.measureText(maxLabel))
        val minMaxHeight = textOffset.toFloat() + thumbHalfHeight + (textSize / 3).toFloat()
        canvas.drawText(minLabel, 0f, minMaxHeight, paint)
        canvas.drawText(maxLabel, width - minMaxLabelSize, minMaxHeight, paint)
        padding = initialPadding + minMaxLabelSize + thumbHalfWidth

        val rect = this.rect!!
        // draw seek bar background line
        rect.left = padding
        rect.right = width - padding
        canvas.drawRect(rect, paint)

        val selectedValuesAreDefault =
            getSelectedMinValue() == getAbsoluteMinValue() && getSelectedMaxValue() == getAbsoluteMaxValue()

        // draw seek bar active range line
        rect.left = normalizedToScreen(normalizedMinValue)
        rect.right = normalizedToScreen(normalizedMaxValue)

        paint.color = if (selectedValuesAreDefault) Color.GRAY else DEFAULT_COLOR
        canvas.drawRect(rect, paint)

        // draw minimum thumb if not a single thumb control
        if (!singleThumb) {
            drawThumb(
                normalizedToScreen(normalizedMinValue),
                Thumb.MIN == pressedThumb,
                canvas,
                selectedValuesAreDefault
            )
        }

        // draw maximum thumb
        drawThumb(
            normalizedToScreen(normalizedMaxValue),
            Thumb.MAX == pressedThumb,
            canvas,
            selectedValuesAreDefault
        )
    }

    /**
     * Overridden to save instance state when device orientation changes. This method is called automatically if you assign an id to the RangeSeekBar widget using the [.setId] method. Other members of this class than the normalized min and max values don'int need to be saved.
     */
    override fun onSaveInstanceState(): Bundle {
        val bundle = Bundle()
        bundle.putParcelable("SUPER", super.onSaveInstanceState())
        bundle.putDouble("MIN", normalizedMinValue)
        bundle.putDouble("MAX", normalizedMaxValue)
        return bundle
    }

    /**
     * Overridden to restore instance state when device orientation changes. This method is called automatically if you assign an id to the RangeSeekBar widget using the [.setId] method.
     */
    override fun onRestoreInstanceState(parcel: Parcelable) {
        val bundle = parcel as Bundle
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"))
        normalizedMinValue = bundle.getDouble("MIN")
        normalizedMaxValue = bundle.getDouble("MAX")
    }

    /**
     * Draws the "normal" resp. "pressed" thumb image on specified x-coordinate.
     *
     * @param screenCoord The x-coordinate in screen space where to draw the image.
     * @param pressed     Is the thumb currently in "pressed" state?
     * @param canvas      The canvas to draw upon.
     */
    private fun drawThumb(
        screenCoord: Float,
        pressed: Boolean,
        canvas: Canvas,
        areSelectedValuesDefault: Boolean
    ) {
        val buttonToDraw = when {
            areSelectedValuesDefault -> thumbDisabledImage
            pressed -> thumbPressedImage
            else -> thumbImage
        }

        canvas.drawBitmap(buttonToDraw, screenCoord - thumbHalfWidth, textOffset, paint)
    }

    /**
     * Decides which (if any) thumb is touched by the given x-coordinate.
     *
     * @param touchX The x-coordinate of a touch event in screen space.
     * @return The pressed thumb or null if none has been touched.
     */
    private fun evalPressedThumb(touchX: Float): Thumb? {
        val minThumbPressed = isInThumbRange(touchX, normalizedMinValue)
        val maxThumbPressed = isInThumbRange(touchX, normalizedMaxValue)

        return when {
            // if both thumbs are pressed (they lie on top of each other), choose the one with more room to drag. this avoids "stalling" the thumbs in a corner, not being able to drag them apart anymore.
            minThumbPressed && maxThumbPressed -> if (touchX / width > 0.5f) Thumb.MIN else Thumb.MAX
            minThumbPressed -> Thumb.MIN
            maxThumbPressed -> Thumb.MAX
            else -> null
        }
    }

    /**
     * Decides if given x-coordinate in screen space needs to be interpreted as "within" the normalized thumb x-coordinate.
     *
     * @param touchX               The x-coordinate in screen space to check.
     * @param normalizedThumbValue The normalized x-coordinate of the thumb to check.
     * @return true if x-coordinate is in thumb range, false otherwise.
     */
    private fun isInThumbRange(touchX: Float, normalizedThumbValue: Double) =
        Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth

    /**
     * Sets normalized min value to value so that 0 <= value <= normalized max value <= 1. The View will get invalidated when calling this method.
     *
     * @param value The new normalized min value to set.
     */
    private fun setNormalizedMinValue(value: Double) {
        normalizedMinValue = Math.max(0.0, Math.min(1.0, Math.min(value, normalizedMaxValue)))
        invalidate()
    }

    /**
     * Sets normalized max value to value so that 0 <= normalized min value <= value <= 1. The View will get invalidated when calling this method.
     *
     * @param value The new normalized max value to set.
     */
    private fun setNormalizedMaxValue(value: Double) {
        normalizedMaxValue = Math.max(0.0, Math.min(1.0, Math.max(value, normalizedMinValue)))
        invalidate()
    }

    /**
     * Converts a normalized value to a Number object in the value space between absolute minimum and maximum.
     */
    private fun normalizedToValue(normalized: Double): Int {
        val v = absoluteMinValuePrim + normalized * (absoluteMaxValuePrim - absoluteMinValuePrim)
        return (Math.round(v * 100) / 100.0).toInt()
    }

    /**
     * Converts the given Number value to a normalized double.
     *
     * @param value The Number value to normalize.
     * @return The normalized double.
     */
    private fun valueToNormalized(value: Int) =
        if (0.0 == absoluteMaxValuePrim - absoluteMinValuePrim)
            0
        else
            ((value - absoluteMinValuePrim) / (absoluteMaxValuePrim - absoluteMinValuePrim)).toInt()

    /**
     * Converts a normalized value into screen space.
     *
     * @param normalizedCoord The normalized value to convert.
     * @return The converted value in screen space.
     */
    private fun normalizedToScreen(normalizedCoord: Double) =
        (padding + normalizedCoord * (width - 2 * padding)).toFloat()

    /**
     * Converts screen space x-coordinates into normalized values.
     *
     * @param screenCoord The x-coordinate in screen space to convert.
     * @return The normalized value.
     */
    private fun screenToNormalized(screenCoord: Float): Double {
        val width = width
        if (width <= 2 * padding) {
            // prevent division by zero, simply return 0.
            return 0.0
        } else {
            val result = ((screenCoord - padding) / (width - 2 * padding)).toDouble()
            return Math.min(1.0, Math.max(0.0, result))
        }
    }

    /**
     * Callback listener interface to notify about changed range values.
     *
     * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
     */
    interface OnRangeSeekBarChangeListener {
        fun onRangeSeekBarValuesChanged(bar: PaddingSeekBar, minValue: Int, maxValue: Int)
    }

    /**
     * Thumb constants (min and max).
     */
    private enum class Thumb {
        MIN, MAX
    }
}