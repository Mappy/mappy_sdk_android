package com.mappy.sdk.sample

import android.os.Bundle
import androidx.annotation.IntDef
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.mappy.map.MapStyle
import com.mappy.map.MappyMapFragment

class CopyrightsSample : AppCompatActivity() {
    companion object {
        const val COPYRIGHTS_HORIZONTAL = 0
        const val COPYRIGHTS_VERTICAL = 1
        const val COPYRIGHTS_VERTICAL_CENTERED = 2
        const val COPYRIGHTS_RIGHT_TOP = 30
        const val COPYRIGHTS_RIGHT_CENTER = 31
        const val COPYRIGHTS_RIGHT_BOTTOM = 32
    }

    @IntDef(
        COPYRIGHTS_HORIZONTAL,
        COPYRIGHTS_VERTICAL,
        COPYRIGHTS_VERTICAL_CENTERED,
        COPYRIGHTS_RIGHT_TOP,
        COPYRIGHTS_RIGHT_CENTER,
        COPYRIGHTS_RIGHT_BOTTOM
    )
    private annotation class CopyrightsType

    private lateinit var actionBar: ActionBar

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initActionBar()
    }

    private fun initActionBar() {
        supportActionBar?.let {
            actionBar = it
            actionBar.navigationMode = ActionBar.NAVIGATION_MODE_TABS
            addNewTab("Vertical", COPYRIGHTS_VERTICAL)
            addNewTab("Horizontal", COPYRIGHTS_HORIZONTAL)
            addNewTab("Vertical centered", COPYRIGHTS_VERTICAL_CENTERED)
            addNewTab("Top right", COPYRIGHTS_RIGHT_TOP)
            addNewTab("Right centered", COPYRIGHTS_RIGHT_CENTER)
            addNewTab("Bottom right", COPYRIGHTS_RIGHT_BOTTOM)
        }
    }

    private fun addNewTab(title: String, @CopyrightsType copyrightsType: Int) {
        actionBar.addTab(actionBar.newTab()
            .setText(title)
            .setTabListener(object : ActionBar.TabListener {
                override fun onTabSelected(
                    tab: ActionBar.Tab,
                    fragmentTransaction: FragmentTransaction
                ) {
                    val copyrightsDirection = when (copyrightsType) {
                        COPYRIGHTS_HORIZONTAL -> MappyMapFragment.Builder.COPYRIGHTS_HORIZONTAL
                        COPYRIGHTS_VERTICAL -> MappyMapFragment.Builder.COPYRIGHTS_VERTICAL
                        COPYRIGHTS_VERTICAL_CENTERED -> MappyMapFragment.Builder.COPYRIGHTS_VERTICAL_CENTERED
                        COPYRIGHTS_RIGHT_TOP -> MappyMapFragment.Builder.COPYRIGHTS_RIGHT_TOP
                        COPYRIGHTS_RIGHT_CENTER -> MappyMapFragment.Builder.COPYRIGHTS_RIGHT_CENTERED
                        COPYRIGHTS_RIGHT_BOTTOM -> MappyMapFragment.Builder.COPYRIGHTS_RIGHT_BOTTOM
                        else -> throw IllegalArgumentException("unknown case")
                    }

                    val mapFragment = MappyMapFragment.newBuilder()
                        .copyrightsDirection(copyrightsDirection)
                        .style(MapStyle.PHOTO)
                        .build()

                    fragmentTransaction.replace(android.R.id.content, mapFragment)
                }

                override fun onTabUnselected(
                    tab: ActionBar.Tab,
                    fragmentTransaction: FragmentTransaction
                ) {
                }

                override fun onTabReselected(
                    tab: ActionBar.Tab,
                    fragmentTransaction: FragmentTransaction
                ) {
                }
            })
        )
    }
}