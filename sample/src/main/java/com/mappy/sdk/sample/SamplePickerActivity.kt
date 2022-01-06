package com.mappy.sdk.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mappy.utils.BuildUtil

/**
 * Displays a list of examples.
 */
class SamplePickerActivity : Activity() {
    private val wrappers = ArrayList<HolderWrapper>()

    init {
        GROUPS.forEach {
            wrappers.add(HolderWrapper(it))
            wrappers.addAll(it.samples.map { HolderWrapper(sample = it) })
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.sample_picker_activity)

        val recyclerView = findViewById<RecyclerView>(R.id.sample_picker_list)
        recyclerView.setHasFixedSize(false)
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.adapter = SamplePickerAdapter()

        BuildUtil.isDebugging = true
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    /**
     * wrapper to prepare the adapter data
     */
    internal data class HolderWrapper(
        val group: GroupHolder? = null,
        val sample: SampleHolder? = null
    )

    /**
     * holder for samples group
     */
    internal data class GroupHolder(val name: String, val samples: List<SampleHolder>)

    /**
     * holder for sample
     */
    internal data class SampleHolder(
        val className: Class<*>,
        val description: String,
        val isNew: Boolean
    )

    /**
     * recycler view adapter
     */
    internal inner class SamplePickerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        /**
         * ViewHolder representing GroupHolder
         */
        internal inner class GroupHolderView(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.sample_picker_title_item, parent, false)
        ) {
            private val title = itemView as TextView

            fun bind(data: GroupHolder) {
                title.text = data.name
            }
        }

        /**
         * ViewHolder representing SampleHolder
         */
        internal inner class SampleHolderView(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.sample_picker_item, parent, false)
        ) {
            private val title: TextView = itemView.findViewById(R.id.sample_picker_item_name)
            private val description: TextView =
                itemView.findViewById(R.id.sample_picker_item_description)
            private val new: View = itemView.findViewById(R.id.sample_picker_item_new)

            fun bind(data: SampleHolder) {
                title.text = data.className.simpleName
                description.text = data.description
                new.visibility = if (data.isNew) View.VISIBLE else View.GONE

                itemView.setOnClickListener { startActivity(Intent(it.context, data.className)) }
            }
        }

        override fun getItemViewType(position: Int) =
            if (wrappers[position].group != null) GROUP_TYPE else SAMPLE_TYPE

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
            GROUP_TYPE -> GroupHolderView(parent)
            SAMPLE_TYPE -> SampleHolderView(parent)
            else -> object : RecyclerView.ViewHolder(View(parent.context)) {} //never happens
        }


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val data = wrappers[position]
            when (holder) {
                is GroupHolderView -> holder.bind(data.group!!) //!! implicit by construction
                is SampleHolderView -> holder.bind(data.sample!!) //!! implicit by construction
            }
        }

        override fun getItemCount() = wrappers.size
    }

    companion object {
        private const val GROUP_TYPE = 1
        private const val SAMPLE_TYPE = 2

        private val GROUPS: List<GroupHolder>

        init {
            val mapSamples = listOf(
                SampleHolder(
                    HelloMapSample::class.java,
                    "manipulations simples de la carte",
                    false
                ),
                SampleHolder(
                    MapUpdateSample::class.java,
                    "changement de la position de la carte",
                    false
                ),
                SampleHolder(MapStyleSample::class.java, "changement du style de la carte", false),
                SampleHolder(
                    CopyrightsSample::class.java,
                    "placements possibles des copyrights",
                    false
                ),
                SampleHolder(
                    LogoAndScaleSample::class.java,
                    "placements possibles du logo et de l'échelle",
                    false
                ),
                SampleHolder(
                    PaddingSample::class.java,
                    "carte avec un padding pour passer sous d'autres vues",
                    true
                ),
                SampleHolder(SnapShotterSample::class.java, "Prend un snapshot de la carte", false),
                SampleHolder(
                    EarlyInitSample::class.java,
                    "Initialise le SDK avant l'affichage",
                    false
                )
            )
            val annotationSamples = listOf(
                SampleHolder(
                    UserLocationSample::class.java,
                    "carte avec le marqueur de position de l'utilisateur",
                    false
                ),
                SampleHolder(
                    MarkerSample::class.java,
                    "carte avec des markers, montre comment gérer les clics utilisateurs",
                    true
                ),
                SampleHolder(
                    MarkerOffsetSample::class.java,
                    "carte avec des markers utilisant des offsets",
                    true
                ),
                SampleHolder(
                    MarkerOpacitySample::class.java,
                    "carte avec des markers utilisant l'opacity",
                    true
                ),
                SampleHolder(
                    MarkerZOrderSample::class.java,
                    "carte avec des markers ayant différents z-order, les markers sélectionnés sont en premier plan",
                    false
                ),
                SampleHolder(BackgroundPOISample::class.java, "POI fond de plan", false),
                SampleHolder(
                    PolylineAndPolygonSample::class.java,
                    "carte avec des polylines et des polygones",
                    false
                ),
                SampleHolder(
                    StylePolylineSample::class.java,
                    "carte avec des polylines sur le style layer",
                    false
                ),
                SampleHolder(
                    CustomStylePolylineSample::class.java,
                    "carte avec des polylines personnalisées sur le style layer",
                    true
                ),
                SampleHolder(GeoJsonSample::class.java, "carte avec des geoJson", false)
            )

            val reverseGeoCoderSamples = listOf(
                SampleHolder(SearchSample::class.java, "suggestion et recherche", false),
                SampleHolder(
                    GeocodeSample::class.java,
                    "transforme une adresse en coordonnées gps\net réciproquement",
                    false
                )
            )

            val routeSamples = listOf(
                SampleHolder(
                    SimpleRouteWithLatLngSample::class.java,
                    "Itinéraires mappy à partir de coordonnées GPS",
                    false
                ),
                SampleHolder(
                    MultiPathRouteWithLatLngSample::class.java,
                    "Comparateurs d'itinéraires mappy à partir de coordonnées GPS",
                    false
                ),
                SampleHolder(
                    MultiPathRouteWithTextSample::class.java,
                    "Comparateurs d'itinéraires mappy à partir de saisie utilisateur",
                    false
                )
            )
            val panoramicSamples = listOf(
                SampleHolder(PanoramicOutdoorSample::class.java, "vue immersive extérieure", false)
            )
            val locationProviderSamples = listOf(
                SampleHolder(
                    UserExternalGoogleLocationSample::class.java,
                    "Fournisseur : Google Services",
                    false
                ),
                SampleHolder(
                    UserExternalMappyLocationSample::class.java,
                    "Fournisseur : Mappy",
                    false
                )
            )
            val utilitarianSamples = listOf(
                SampleHolder(DebugMenu::class.java, "ouvre un menu de debug", false),
                SampleHolder(
                    SDKInfos::class.java,
                    "ouvre un menu d'information pratique du sdk",
                    false
                ),
                SampleHolder(
                    FPSCounter::class.java,
                    "permet de mesure le FPS de l'utilisation de la carte",
                    false
                ),
                SampleHolder(OpenGPSByIntent::class.java, "Open GPS sample", false)
            )
            GROUPS = listOf(
                GroupHolder("Carte", mapSamples),
                GroupHolder("Annotations", annotationSamples),
                GroupHolder("Geocoder / Reverse Geocoder", reverseGeoCoderSamples),
                GroupHolder("MultiPath Itinéraires", routeSamples),
                GroupHolder("Vues panoramiques", panoramicSamples),
                GroupHolder("Fournisseur de données géographiques", locationProviderSamples),
                GroupHolder("Utilitaires", utilitarianSamples)
            )
        }
    }
}