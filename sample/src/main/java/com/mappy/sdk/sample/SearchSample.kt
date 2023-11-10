package com.mappy.sdk.sample

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mappy.common.model.GeoConstants
import com.mappy.legacy.MappyDownloadManager
import com.mappy.legacy.RequestListener
import com.mappy.legacy.requestparams.LocationByQueryRequestParams
import com.mappy.legacy.requestparams.SuggestionsRequestParams
import com.mappy.sdk.sample.utils.AddressUtil
import com.mappy.utils.TextFormatUtil
import com.mappy.utils.ZoomConstants
import com.mappy.webservices.resource.json.SuggestionJson
import com.mappy.webservices.resource.store.LocationStore
import com.mappy.webservices.resource.store.SuggestionStore

class SearchSample : AppCompatActivity(), View.OnClickListener {
    private lateinit var input: TextView
    private lateinit var validate: View
    private lateinit var result: TextView
    private val suggestionsAdapter = SuggestionAdapter(this)

    private val inputWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            MappyDownloadManager.getSuggestions(
                SuggestionsRequestParams(
                    s.toString(), SuggestionsRequestParams.Filter.all,
                    GeoConstants.FRANCE,
                    ZoomConstants.COUNTRY
                ), object : RequestListener<SuggestionStore> {
                    override fun onRequestSuccess(result: SuggestionStore) {
                        suggestionsAdapter.suggestionJsons = result.suggestionJsonList
                    }

                    override fun onRequestFailure(throwable: Throwable) {
                    }
                })
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_search)

        input = findViewById(R.id.sample_search_input)
        validate = findViewById(R.id.sample_search_validate)
        result = findViewById(R.id.sample_search_result)

        findViewById<RecyclerView>(R.id.sample_search_suggestions).apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = suggestionsAdapter
            addItemDecoration(
                DividerItemDecoration(
                    application,
                    (layoutManager as LinearLayoutManager).orientation
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        validate.setOnClickListener { search(input.text.toString()) }
        input.addTextChangedListener(inputWatcher)
    }

    override fun onPause() {
        super.onPause()
        validate.setOnClickListener { }
        input.removeTextChangedListener(inputWatcher)
    }

    override fun onClick(v: View) {
        val suggestionJson = v.tag as SuggestionJson
        val searchCompleteText =
            TextFormatUtil.removeHtmlItalic(suggestionJson.getStringLabels(", ", false))

        search(searchCompleteText)
    }

    private fun search(input: String) {
        MappyDownloadManager.getLocationByQuery(
            LocationByQueryRequestParams(
                input,
                GeoConstants.FRANCE,
                true,
                false
            ), object : RequestListener<LocationStore> {
                override fun onRequestSuccess(result: LocationStore) {
                    onGetLocationByQuerySuccess(result)
                }

                override fun onRequestFailure(throwable: Throwable) {
                    result.text = Log.getStackTraceString(throwable)
                }
            })
    }

    private fun onGetLocationByQuerySuccess(locationStore: LocationStore) {
        if (locationStore.mappyLocations.isEmpty()) {
            result.text = "Pas de résultat"
        } else {
            val firstLocation = locationStore.mappyLocations[0]
            val mappyAddress = firstLocation.address

            var addressLabel: String
            val splitAddressLabel = mappyAddress.splitLabel
            if (splitAddressLabel?.isNotEmpty() == true) {
                addressLabel = ""
                var sep = ""
                for (split in splitAddressLabel) {
                    addressLabel += sep + split
                    sep = "\n"
                }
            } else {
                addressLabel = AddressUtil.getFormattedGeoAddress(mappyAddress)
            }
            result.text = "${firstLocation.label}\n$addressLabel"
        }
    }

    private class SuggestionAdapter(val listener: View.OnClickListener) :
        RecyclerView.Adapter<SuggestionViewHolder>() {
        internal var suggestionJsons: List<SuggestionJson> = ArrayList()
            set(suggestions) {
                field = suggestions
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            SuggestionViewHolder(parent)

        override fun getItemCount() = suggestionJsons.size

        override fun getItemViewType(position: Int) = 1

        private fun getItemAt(position: Int) = suggestionJsons[position]

        override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
            holder.bind(getItemAt(position))
            holder.itemView.setOnClickListener(listener)
        }
    }

    private class SuggestionViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_suggestion, parent, false)
    ) {

        fun bind(suggestionJson: SuggestionJson) {
            val labels = replaceHtmlItalicWithBold(suggestionJson.labels)
            val childrenCount = (itemView as ViewGroup).childCount
            for (i in labels.size until childrenCount) {
                itemView.getChildAt(i).visibility = View.GONE
            }

            for (i in labels.indices) {
                val label = labels[i]
                val labelTextView: TextView
                if (i < childrenCount) {
                    labelTextView = itemView.getChildAt(i) as TextView
                    labelTextView.visibility = View.VISIBLE
                } else {
                    val layoutInflater = LayoutInflater.from(itemView.context)
                    labelTextView = layoutInflater.inflate(
                        R.layout.item_search_label,
                        itemView,
                        false
                    ) as TextView
                    itemView.addView(
                        labelTextView,
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    labelTextView.text = Html.fromHtml(label, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    labelTextView.text = Html.fromHtml(label)
                }
            }
            itemView.tag = suggestionJson
        }

        companion object {
            private val ITALIC_START_TAG = "<em>".toRegex()
            private val ITALIC_END_TAG = "</em>".toRegex()
            private const val BOLD_START_TAG = "<b>"
            private const val BOLD_END_TAG = "</b>"

            private fun replaceHtmlItalicWithBold(texts: List<String>) =
                texts.map { replaceHtmlItalicWithBold(it) }

            private fun replaceHtmlItalicWithBold(text: String) =
                if (text.isNotEmpty()) text.replace(ITALIC_START_TAG, BOLD_START_TAG)
                    .replace(ITALIC_END_TAG, BOLD_END_TAG) else ""
        }
    }
}