package com.example.bookapp2.filter

import android.annotation.SuppressLint
import android.widget.Filter
import com.example.bookapp2.adapter.AdapterPdfAdmin
import com.example.bookapp2.model.Pdf

class FilterPdfAdmin(
    List : ArrayList<Pdf>,
    adapter : AdapterPdfAdmin
) : Filter() {
    var filterList : ArrayList<Pdf> = List
    private var adapterPdfAdmin : AdapterPdfAdmin = adapter



    override fun performFiltering(p0: CharSequence?): FilterResults {
        var constraint : CharSequence? = p0
        val results = FilterResults()
        if (constraint != null && constraint.isNotEmpty()){
            constraint = constraint.toString().lowercase()
            var filterModels = ArrayList<Pdf>()
            for (i in filterList.indices){
                if (filterList[i].title.lowercase().contains(constraint)){
                    filterModels.add(filterList[i])
                }
            }
            results.count = filterModels.size
            results.values = filterModels
        }
        else{
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun publishResults(p0: CharSequence, p1: FilterResults) {
        adapterPdfAdmin.pdfArrayList = p1.values as ArrayList<Pdf> /* = java.util.ArrayList<com.example.book.model.Pdf> */
        adapterPdfAdmin.notifyDataSetChanged()
    }


}