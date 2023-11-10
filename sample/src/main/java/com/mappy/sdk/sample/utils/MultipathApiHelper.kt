package com.mappy.sdk.sample.utils

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.mappy.common.model.LatLng
import com.mappy.legacy.MappyDownloadManager
import com.mappy.legacy.RequestListener
import com.mappy.legacy.requestparams.MultiPathRequestParams
import com.mappy.legacy.requestparams.MultiPathTransportsRequestParams
import com.mappy.map.MapController
import com.mappy.map.MappyPolyline
import com.mappy.map.MappySection
import com.mappy.sdk.sample.R
import com.mappy.utils.ConnectivityUtil
import com.mappy.utils.Logger
import com.mappy.webservices.resource.model.dao.MappyMultiPathRoute
import com.mappy.webservices.resource.model.dao.MappyMultiPathTransportMode
import com.mappy.webservices.resource.model.dao.multipath.MultiPathTransportModeGroup
import com.mappy.webservices.resource.store.MultiPathRouteStore
import com.mappy.webservices.resource.store.MultiPathTransportsStore

class MultipathApiHelper(val context: Context, val listener: MultipathApiUser) {

    interface MultipathApiUser {
        fun getDeparture(): LatLng?
        fun getArrival(): LatLng?
        fun getArrivalLabel(): String
        fun getDepartureLabel(): String
        fun getRoutesTextContainer(): TextView
        fun getTransportModesContainer(): LinearLayout
        fun getMapController(): MapController
        fun showProgressDialog()
        fun dismissProgressDialog()
    }

    private var selectedModeIcon: AppCompatImageView? = null

    fun startTransportModesRequest() {
        listener.showProgressDialog()

        val departure = listener.getDeparture() ?: return
        val arrival = listener.getArrival() ?: return

        val params = MultiPathTransportsRequestParams(
            departure = departure,
            arrival = arrival,
            step = null,
            departureIsMyPosition = false,
            prefTransportMode = PREFERRED_TRANSPORT_MODE
        )

        MappyDownloadManager.getMultiPathTransportModes(
            params,
            object : RequestListener<MultiPathTransportsStore> {
                override fun onRequestSuccess(result: MultiPathTransportsStore) {
                    listener.dismissProgressDialog()
                    showTransportsModesSlider(result, params)
                }

                override fun onRequestFailure(throwable: Throwable) {
                    val message =
                        if (!ConnectivityUtil.isConnected(context)) R.string.routeerror_nonetworkconnexion else R.string.routeerror
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    listener.dismissProgressDialog()
                }
            })
    }

    private fun showTransportsModesSlider(
        transportsStore: MultiPathTransportsStore,
        transportParams: MultiPathTransportsRequestParams
    ) {
        val transportModesContainer =
            listener.getTransportModesContainer().apply { removeAllViews() }
        listener.getRoutesTextContainer().text = null
        val transportQueryId = transportsStore.multiPathTransports.queryId
        val possibleTransportModes = transportsStore.multiPathTransports.possibleTransportModeGroups
        var firstTransportModeGroup: MultiPathTransportModeGroup? = null
        for (transportModeGroup in possibleTransportModes) {
            if (transportModeGroup.isRecommended) {
                if (firstTransportModeGroup == null) {
                    firstTransportModeGroup = transportModeGroup
                }
                val modeIcon = generateModeIcon(transportModesContainer, transportModeGroup.icon)
                modeIcon.setOnClickListener {
                    onTransportModeSelected(
                        it as AppCompatImageView,
                        transportQueryId,
                        transportModeGroup,
                        transportParams
                    )
                }
            }
        }
        if (firstTransportModeGroup != null) {
            val firstModeIcon = transportModesContainer.getChildAt(0) as AppCompatImageView
            onTransportModeSelected(
                firstModeIcon,
                transportQueryId,
                firstTransportModeGroup,
                transportParams
            )
        }
    }

    private fun onTransportModeSelected(
        selectedModeIcon: AppCompatImageView,
        transportQueryId: String,
        transportModeGroup: MultiPathTransportModeGroup,
        transportParams: MultiPathTransportsRequestParams
    ) {
        this.selectedModeIcon?.let {
            it.setColorFilter(ContextCompat.getColor(context, R.color.selector_color_grey))
            listener.getMapController().clearPolylines()
        }

        this.selectedModeIcon = selectedModeIcon.also {
            it.setColorFilter(ContextCompat.getColor(context, R.color.selector_color_green))
            getMultiPathRoute(transportQueryId, transportModeGroup, transportParams)
        }
    }

    private fun getMultiPathRoute(
        sessionId: String,
        modeGroup: MultiPathTransportModeGroup,
        transportParams: MultiPathTransportsRequestParams
    ) {
        listener.showProgressDialog()

        val text =
            StringBuilder("Transport mode \"").append(modeGroup.label).append("\" with provider: ")

        //prepare multipath request
        val paramsBuilder = MultiPathRequestParams.Builder(transportParams)
            .setMultiPathSessionId(sessionId)
            .setDepartureLabel(listener.getDepartureLabel())
            .setArrivalLabel(listener.getArrivalLabel())

        modeGroup.possibleTransportProviders.forEachIndexed { requestIndex, transportProvider ->

            //configure the MappyMultiPathTransportMode to use
            val mode = MappyMultiPathTransportMode(
                transportProvider.name,
                modeGroup.isRecommended,
                requestIndex
            )
            val params = paramsBuilder.setTransportMode(mode).build()

            //launch the request
            MappyDownloadManager.getMultiPathRoute(
                params,
                object : RequestListener<MultiPathRouteStore> {
                    override fun onRequestSuccess(result: MultiPathRouteStore) {
                        Logger.d("… displayProviderRoutes mTransportMode=${transportProvider.name}")
                        displayProviderRoutes(transportProvider.name, result)
                        listener.dismissProgressDialog()
                    }

                    override fun onRequestFailure(throwable: Throwable) {
                        val message =
                            if (!ConnectivityUtil.isConnected(context)) R.string.routeerror_nonetworkconnexion else R.string.routeerror
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        listener.dismissProgressDialog()
                    }
                })
        }

        listener.getRoutesTextContainer().text = text.toString()
    }


    private fun displayProviderRoutes(
        transportProviderName: String,
        routeStore: MultiPathRouteStore
    ) {
        val size = routeStore.multiPathRouteList.size
        val text = StringBuilder(listener.getRoutesTextContainer().text)
        if (size > 0) {
            text.append("\n  - ").append("Provider \"").append(transportProviderName).append("\": ")
                .append(size).append(" route(s)")
            for (multiPathRoute in routeStore.multiPathRouteList) {
                text.append("\n      . ").append(labelsToString(multiPathRoute.getLabels()))
                    .append(multiPathRoute.price)
                val mappyMultiPathRoute = MappyMultiPathRoute(transportProviderName, multiPathRoute)
                displayMultiPathRoute(mappyMultiPathRoute)
            }
            listener.getMapController().displayWholePolyLines()
        } else {
            text.append("\n    ").append(transportProviderName).append(": no route received!")
            Toast.makeText(context, text.toString(), Toast.LENGTH_LONG).show()
        }
        listener.getRoutesTextContainer().text = text.toString()
    }

    private fun displayMultiPathRoute(routes: MappyMultiPathRoute) =
        listener.getMapController().addPolyline(
            MappyPolyline.Builder()
                .addSection(MappySection.Builder(routes).build())
                .build()
        )

    private fun getModeResource(icon: String) = when (icon) {
        "tc" -> R.drawable.vector_55
        "voiture-de-transport-avec-chauffeur" -> R.drawable.vector_57427
        "css" -> R.drawable.vector_57401
        "train" -> R.drawable.vector_57421
        "autocar" -> R.drawable.vector_57418
        "taxi" -> R.drawable.vector_57404
        "bike" -> R.drawable.vector_54
        "carpooling" -> R.drawable.vector_57402
        "walk" -> R.drawable.vector_53
        "car" -> R.drawable.vector_52
        else -> R.drawable.vector_52
    }

    private fun generateModeIcon(
        transportModesContainer: LinearLayout,
        icon: String
    ): AppCompatImageView {
        val iconSize = context.resources.getDimensionPixelSize(R.dimen.itinerary_vehicle)
        val iconPadding = context.resources.getDimensionPixelSize(R.dimen.itinerary_vehicle_padding)
        val modeIcon = AppCompatImageView(context)
        modeIcon.setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
        val layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
        transportModesContainer.addView(modeIcon, layoutParams)
        modeIcon.setImageResource(getModeResource(icon))
        modeIcon.setColorFilter(ContextCompat.getColor(context, R.color.selector_color_grey))
        return modeIcon
    }

    private fun labelsToString(labels: List<String>): String {
        val text = StringBuilder(labels[0]).append(": ")
        for (label in labels) {
            text.append(label).append(", ")
        }
        return text.toString()
    }

    companion object {
        private const val PREFERRED_TRANSPORT_MODE = "car"
    }
}