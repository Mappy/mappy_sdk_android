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
import android.widget.*
import androidx.fragment.app.Fragment
import com.mappy.common.model.GeoBounds
import com.mappy.map.MapController
import com.mappy.sdk.sample.R
import com.mappy.sdk.sample.utils.ProgressDialogHelper
import com.mappy.services.MappyDownloadManager
import com.mappy.services.RequestListener
import com.mappy.services.requests.GetLocationByQueryRequest
import com.mappy.services.requests.SuggestionsRequest
import com.mappy.utils.TextFormatUtil
import com.mappy.webservices.resource.json.Suggestion
import com.mappy.webservices.resource.model.dao.MappyLocation
import com.mappy.webservices.resource.store.LocationStore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit

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
    private var suggestions: List<Suggestion>? = null

    private var searchSuggestionSubject: PublishSubject<String>? = null
    private var textWatchDisposable: Disposable? = null

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
        createObservables(context)
    }

    override fun onDetach() {
        super.onDetach()

        MappyDownloadManager.cancelSuggestionsRequest()
        listener = null

        textWatchDisposable?.dispose()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments = arguments!!
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


            val params = GetLocationByQueryRequest.Params(
                query,
                geobounds ?: GeoBounds(),
                extendsBoundingBox = true,
                isForRoute = true,
                filter = GetLocationByQueryRequest.POI_XOR_ADDRESS
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

    private fun createObservables(context: Context) {
        val suggest = PublishSubject.create<String>()
        searchSuggestionSubject = suggest
        val observable = suggest.debounce(100, TimeUnit.MILLISECONDS)
        textWatchDisposable = observable.flatMap { query -> launchSuggestionRequest(query) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { this.updateSuggestions(it.suggestionList) },
                { createObservables(context) })
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

                searchSuggestionSubject?.onNext(text)
            }
        })
    }

    private fun setOnItemClickListener(listView: ListView) {
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            suggestions?.let {
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

    @Synchronized
    private fun launchSuggestionRequest(text: String) =
        MappyDownloadManager.getSuggestions(SuggestionsRequest.Params(text))

    @Synchronized
    private fun updateSuggestions(suggestions: List<Suggestion>?) {
        this.suggestions = suggestions
        data.clear()
        if (suggestions != null) {
            for (suggestion in suggestions) {
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
