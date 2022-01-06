package com.mappy.sdk.sample

import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import java.util.*

class FPSCounter : FragmentActivity() {
    companion object {
        private const val DELAY = 1000L
        private const val NB_VALUES = 500
        private const val INFINITY = "All right!"

        private val HISTORY = ArrayList<Int>()
        private val COUNTER_VALUES = Array(NB_VALUES, { it.toString() })
    }

    private var frames = 0
    private var lastDraw: Long = 0
    private lateinit var counter: TextView

    private val fpsDisplayerRunnable = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            val fps = (DELAY * frames / (now - lastDraw)).toInt()
            frames = 0
            lastDraw = now

            counter.text = if (fps < NB_VALUES) COUNTER_VALUES[fps] else INFINITY
            HISTORY.add(fps)
            counter.postDelayed(this, DELAY)
        }
    }
    private val frameCounterRunnable = object : Runnable {
        override fun run() {
            frames++
            counter.post(this)
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fps_counter)
        counter = findViewById(R.id.fps_counter_text_view)
    }

    override fun onResume() {
        super.onResume()
        lastDraw = System.currentTimeMillis()
        counter.postDelayed(fpsDisplayerRunnable, DELAY)
        counter.post(frameCounterRunnable)
    }

    override fun onPause() {
        counter.removeCallbacks(frameCounterRunnable)
        counter.removeCallbacks(fpsDisplayerRunnable)
        super.onPause()
    }
}