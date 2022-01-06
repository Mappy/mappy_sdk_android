package com.mappy.sdk.sample.utils.extention

import android.widget.SeekBar

inline fun SeekBar.setOnSeekBarChangeListener(
    crossinline onStartTrackingTouch: ((seekBar: SeekBar?) -> Unit) = {},
    crossinline onStopTrackingTouch: ((seekBar: SeekBar?) -> Unit) = {},
    crossinline onProgressChanged: ((seekBar: SeekBar?, progress: Int, fromUser: Boolean) -> Unit) = { _, _, _ -> },
) {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            onProgressChanged(seekBar, progress, fromUser)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            onStartTrackingTouch(seekBar)
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            onStopTrackingTouch(seekBar)
        }
    })
}