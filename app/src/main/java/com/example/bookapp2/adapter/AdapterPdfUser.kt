package com.example.bookapp2.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp2.MyApplication
import com.example.bookapp2.R
import com.example.bookapp2.filter.FilterPdfUser
import com.example.bookapp2.model.Pdf
import com.example.bookapp2.ui.fragments.PdfDetailFragment
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth

class AdapterPdfUser(
    private val c : Context,
    private val List : ArrayList<Pdf>
) : RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser>() , Filterable {

    var context : Context = c
    var pdfArraylist : ArrayList<Pdf> = List
    private val filterList  :  ArrayList<Pdf> = pdfArraylist
    private var filter : FilterPdfUser?= null

    inner class HolderPdfUser(itemView: View) : RecyclerView.ViewHolder(itemView){
        val pdfView = itemView.findViewById<PDFView>(R.id.pdfViewUser)!!
        val progressBar : ProgressBar = itemView.findViewById(R.id.progressBarUser)
        val titleTv : TextView = itemView.findViewById(R.id.titleTvUser)
        val desTv : TextView = itemView.findViewById(R.id.desTvUser)
        val categoryTv : TextView = itemView.findViewById(R.id.categoryTvUser)
        val sizeTv : TextView = itemView.findViewById(R.id.sizeTvUser)
        val dateTv : TextView = itemView.findViewById(R.id.dateTvUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        return HolderPdfUser(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_rv_pdf_user,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        val model = pdfArraylist[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp
        val date = MyApplication.formatTimeStamp(timestamp)

        holder.titleTv.text = title
        holder.desTv.text = description
        holder.dateTv.text = date

        MyApplication.loadCategory(categoryId, holder.categoryTv)

        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )

        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("bookId",bookId)
            val pdfDetailFragment = PdfDetailFragment()
            pdfDetailFragment.arguments = bundle
            val activity = it.context as AppCompatActivity
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view_tag_user, pdfDetailFragment, PdfDetailFragment::class.java.simpleName)
                .addToBackStack(null)
                .commit()
        }

    }

    override fun getItemCount(): Int {
        return pdfArraylist.size
    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterPdfUser(filterList,this)
        }
        return filter as FilterPdfUser
    }


}