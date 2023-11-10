package com.mappy.sdk.sample.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mappy.common.model.GeoBounds
import com.mappy.legacy.MappyDownloadManager
import com.mappy.legacy.RequestListener
import com.mappy.legacy.requestparams.LocationByQueryRequestParams
import com.mappy.legacy.requestparams.SuggestionsRequestParams
import com.mappy.map.MapController
import com.mappy.sdk.sample.R
import com.mappy.sdk.sample.utils.ProgressDialogHelper
import com.mappy.utils.TextFormatUtil
import com.mappy.webservices.resource.json.SuggestionJson
import com.mappy.webservices.resource.model.dao.MappyLocation
import com.mappy.webservices.resource.store.LocationStore
import com.mappy.webservices.resource.store.SuggestionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class LocationGeocodeFragment : Fragment() {

    private var departure: Boolean = false
    private var previousText: String? = null
    private var geobounds: GeoBounds? = null
    private var boundingBox: String? = null

    private lateinit var progressDialogHelper: ProgressDialogHelper
    private var listener: LocationGeocodeListener? = null

    private lateinit var editText: EditText
    private lateinit var validate: Button

    private lateinit var adapter: SimpleAdapter
    private val data = ArrayList<HashMap<String, CharSequence>>()
    private var suggestionJsons: List<SuggestionJson>? = null
    private var textWatchJob: Job? = null
    private var searchSuggestionFlow: MutableSharedFlow<String> = MutableSharedFlow()

    interface LocationGeocodeListener {
        fun onSearchDone(location: MappyLocation, isDeparture: Boolean)

        fun showKeyBoard()

        fun hideKeyBoard()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentFragment = parentFragment
        listener = if (parentFragment is LocationGeocodeListener) {
            parentFragment
        } else {
            context as LocationGeocodeListener
        }
        progressDialogHelper = ProgressDialogHelper(context)
        initializeSuggestionSearchCoroutines(context)
    }

    override fun onDetach() {
        super.onDetach()

        MappyDownloadManager.cancelSuggestionsRequest()
        listener = null

        textWatchJob?.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments = requireArguments()
        departure = arguments.getBoolean(IS_DEPARTURE_KEY)
        previousText = arguments.getString(PREVIOUS_TEXT_KEY)
        geobounds = arguments.getParcelable(GEO_BOUNDS_KEY)
        boundingBox = arguments.getString(BOUNDING_BOX_KEY)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_location_geocode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editText = view.findViewById(R.id.edit_text)
        validate = view.findViewById(R.id.validate_button)
        val listView = view.findViewById<ListView>(R.id.suggestion_list)

        editText.hint = if (departure) "Départ" else "Arrivée"

        adapter = SimpleAdapter(view.context, data, android.R.layout.simple_list_item_2, FROM, TO)
        listView.adapter = adapter

        setValidateListener()
        setTextWatcher()
        setOnItemClickListener(listView)
        editText.setText(previousText)
    }

    override fun onResume() {
        super.onResume()
        editText.requestFocus()
        listener?.showKeyBoard()
    }

    override fun onPause() {
        super.onPause()
        listener?.hideKeyBoard()
    }

    private fun setValidateListener() {
        validate.setOnClickListener(View.OnClickListener {
            val query = editText.text.toString()
            val context = it.context
            if (TextUtils.isEmpty(query)) {
                Toast.makeText(
                    context,
                    "Vous devez saisir un texte pour valider",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            }
            progressDialogHelper.show()


            val params = LocationByQueryRequestParams(
                query,
                geobounds ?: GeoBounds(),
                extendsBoundingBox = true,
                isForRoute = true,
                filter = LocationByQueryRequestParams.POI_XOR_ADDRESS
            )
            MappyDownloadManager.getLocationByQuery(
                params,
                object : RequestListener<LocationStore> {
                    override fun onRequestSuccess(result: LocationStore) {
                        if (!isVisible) {
                            return
                        }
                        val mappyLocations = result.mappyLocations
                        if (mappyLocations.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Aucun résultat n'a été trouvé",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            listener?.onSearchDone(mappyLocations[0], departure)
                        }
                        progressDialogHelper.dismiss()
                    }

                    override fun onRequestFailure(throwable: Throwable) {
                        progressDialogHelper.dismiss()
                    }
                })
        })
    }

    private fun initializeSuggestionSearchCoroutines(context: Context) {
        searchSuggestionFlow
            .debounce(100)
            .flatMapLatest { query -> flowOf(launchSuggestionRequest(query)) }
            .onEach { suggestions ->
                updateSuggestions(suggestions.suggestionJsonList)
            }
            .launchIn(CoroutineScope(Dispatchers.Main))
    }

    private fun setTextWatcher() {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val toString = s.toString()
                if (TextUtils.isEmpty(toString)) {
                    updateSuggestions(null)
                    return
                }
                val text = TextFormatUtil.normalizeToAscii(toString)
                    .replace("\"".toRegex(), " ")
                    .trim { it <= ' ' }

                CoroutineScope(Dispatchers.Default).launch {
                    searchSuggestionFlow.emit(text)
                }
            }
        })
    }

    private fun setOnItemClickListener(listView: ListView) {
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            suggestionJsons?.let {
                if (it.size > position) {
                    val query = it[position].getStringLabels(",")
                    editText.setText(
                        query.replace("<em>".toRegex(), "").replace("</em>".toRegex(), "")
                    )
                    validate.performClick()
                }
            }
        }
    }

    private suspend fun launchSuggestionRequest(query: String): SuggestionStore {
        return suspendCancellableCoroutine { continuation ->
            MappyDownloadManager.getSuggestions(
                SuggestionsRequestParams(query),
                object : RequestListener<SuggestionStore> {

                    override fun onRequestSuccess(result: SuggestionStore) {
                        continuation.resume(result)
                    }

                    override fun onRequestFailure(throwable: Throwable) {
                        continuation.resume(SuggestionStore())
                    }
                })
        }
    }

    @Synchronized
    private fun updateSuggestions(suggestionJsons: List<SuggestionJson>?) {
        this.suggestionJsons = suggestionJsons
        data.clear()
        if (suggestionJsons != null) {
            for (suggestion in suggestionJsons) {
                val map = HashMap<String, CharSequence>(2)
                if (suggestion.labels.isNotEmpty()) {
                    map[FROM[0]] =
                        Html.fromHtml(suggestion.type.toString() + if (TextUtils.isEmpty(suggestion.labels[0])) "" else ": " + suggestion.labels[0])
                } else {
                    map[FROM[0]] = Html.fromHtml(suggestion.type.toString())
                }
                if (suggestion.labels.size > 1) {
                    map[FROM[1]] =
                        Html.fromHtml(if (TextUtils.isEmpty(suggestion.labels[1])) "" else suggestion.labels[1])
                } else {
                    map[FROM[1]] = ""
                }
                data.add(map)
            }
        }
        adapter.notifyDataSetChanged()
    }

    companion object {
        const val TAG = "LocationGeocodeFragment"
        private const val IS_DEPARTURE_KEY = "$TAG.IsDeparture"
        private const val PREVIOUS_TEXT_KEY = "$TAG.PreviousText"
        private const val GEO_BOUNDS_KEY = "$TAG.GeoBounds"
        private const val BOUNDING_BOX_KEY = "$TAG.BoundingBox"

        private val FROM = arrayOf("firstLine", "secondLine")
        private val TO = intArrayOf(android.R.id.text1, android.R.id.text2)

        fun newInstance(
            isDeparture: Boolean,
            previousText: String,
            mapController: MapController
        ): LocationGeocodeFragment {
            val instance = LocationGeocodeFragment()
            val arguments = Bundle()
            arguments.putBoolean(IS_DEPARTURE_KEY, isDeparture)
            arguments.putString(PREVIOUS_TEXT_KEY, previousText)
            arguments.putParcelable(GEO_BOUNDS_KEY, mapController.boundingBox)
            arguments.putString(
                BOUNDING_BOX_KEY,
                mapController.boundingBox?.getBoundingBoxString(mapController.zoom)
            )
            instance.arguments = arguments
            return instance
        }
    }
}
