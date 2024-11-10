package com.zekecode.akira_financialtracker.ui.adapters

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable

class SuggestionsAdapter(
    context: Context,
    suggestions: List<String>
) : ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, suggestions), Filterable {

    private var allSuggestions: List<String> = suggestions
    private var filteredSuggestions: List<String> = suggestions

    fun updateSuggestions(newSuggestions: List<String>) {
        allSuggestions = newSuggestions
        filteredSuggestions = newSuggestions
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return filteredSuggestions.size
    }

    override fun getItem(position: Int): String? {
        return filteredSuggestions[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                if (constraint.isNullOrEmpty()) {
                    results.values = allSuggestions
                    results.count = allSuggestions.size
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    val filteredList = allSuggestions.filter {
                        it.lowercase().contains(filterPattern)
                    }
                    results.values = filteredList
                    results.count = filteredList.size
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.values is List<*>) {
                    filteredSuggestions = results.values as List<String>
                    notifyDataSetChanged()
                }
            }
        }
    }
}
