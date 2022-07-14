package com.example.bookapp2.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp2.MyApplication
import com.example.bookapp2.R
import com.example.bookapp2.filter.FilterPdfAdmin
import com.example.bookapp2.model.Pdf
import com.example.bookapp2.ui.fragments.EditPdfFragment
import com.example.bookapp2.ui.fragments.PdfDetailFragment
import com.github.barteksc.pdfviewer.PDFView

class AdapterPdfAdmin(
     c: Context,
     List : ArrayList<Pdf>
) : RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>() , Filterable{

    var context : Context
    var pdfArrayList : ArrayList<Pdf>
    private val filterList : ArrayList<Pdf>
    private var filter :  FilterPdfAdmin?= null

    init {
        context = c
        pdfArrayList = List
        filterList = pdfArrayList
    }

    inner class HolderPdfAdmin(itemView: View) : RecyclerView.ViewHolder(itemView){
        val pdfView = itemView.findViewById<PDFView>(R.id.pdfView)!!
        val progressBar : ProgressBar = itemView.findViewById(R.id.progressBarBook)
        val titleTv : TextView = itemView.findViewById(R.id.titleTvBook)
        val desTv : TextView = itemView.findViewById(R.id.desTvBook)
        val categoryTv : TextView = itemView.findViewById(R.id.categoryTvBook)
        val sizeTv : TextView = itemView.findViewById(R.id.sizeTv)
        val dateTv : TextView = itemView.findViewById(R.id.dateTv)
        val moreBtn : ImageButton = itemView.findViewById(R.id.imbMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        return HolderPdfAdmin(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_rv_book,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        val model = pdfArrayList[position]
        val pdfId = model.id
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

        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model,holder)
        }

        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("bookId",pdfId)
            val pdfDetailFragment = PdfDetailFragment()
            pdfDetailFragment.arguments = bundle
            val activity = it.context as AppCompatActivity
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view_tag_admin, pdfDetailFragment, PdfDetailFragment::class.java.simpleName)
                .addToBackStack(null)
                .commit()
        }

    }

    private fun moreOptionsDialog(model: Pdf, holder: HolderPdfAdmin) {
        val id = model.id
        val url = model.url
        val title = model.title

        val options = arrayOf("Edit", "Delete")
        val builder = AlertDialog.Builder(context)
            .setItems(options){dialog, position ->
                if (position == 0)
                {
                    val bundle = Bundle()
                    bundle.putString("bookId",id)
                    val activity = context as AppCompatActivity
                    val editPdfFragment = EditPdfFragment()
                    editPdfFragment.arguments = bundle
                    activity.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view_tag_admin,editPdfFragment,EditPdfFragment::class.java.simpleName)
                        .addToBackStack(null)
                        .commit()
                }
                else if (position == 1){
                    MyApplication.deleteBook(context, id, url, title)
                }
            }
            .show()

    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterPdfAdmin(filterList,this)
        }
        return filter as FilterPdfAdmin
    }


}