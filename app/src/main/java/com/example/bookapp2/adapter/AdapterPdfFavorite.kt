package com.example.bookapp2.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp2.MyApplication
import com.example.bookapp2.R
import com.example.bookapp2.model.Pdf
import com.example.bookapp2.ui.fragments.PdfDetailFragment
import com.example.bookapp2.ui.fragments.PdfViewFragment
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterPdfFavorite(
    private val c : Context,
    private val List: ArrayList<Pdf>
) : RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite>(){
    val context : Context = c
    val booksArrayList : ArrayList<Pdf> = List

    inner class HolderPdfFavorite(itemView: View) : RecyclerView.ViewHolder(itemView){
        val pdfView = itemView.findViewById<PDFView>(R.id.pdfViewFv)!!
        val progressBar : ProgressBar = itemView.findViewById(R.id.progressBarBookFv)
        val titleTv : TextView = itemView.findViewById(R.id.titleTvBookFavorite)
        val desTv : TextView = itemView.findViewById(R.id.desTvBookFavorite)
        val categoryTv : TextView = itemView.findViewById(R.id.categoryTvBookFavorite)
        val sizeTv : TextView = itemView.findViewById(R.id.sizeTvFavorite)
        val dateTv : TextView = itemView.findViewById(R.id.dateTvFv)
        val favorite : ImageButton = itemView.findViewById(R.id.imFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfFavorite {
        return HolderPdfFavorite(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_rv_favoritebook,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HolderPdfFavorite, position: Int) {
        val model = booksArrayList[position]
        loadBookDetails(model,holder)

        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("bookId",model.id)
            val pdfDetailFragment = PdfDetailFragment()
            pdfDetailFragment.arguments = bundle
            val activity = it.context as AppCompatActivity
            val mAuth = FirebaseAuth.getInstance()
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(mAuth.currentUser!!.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userType = snapshot.child("userType").value
                        if (userType == "user"){
                            activity.supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container_view_tag_user,pdfDetailFragment,
                                    PdfDetailFragment::class.java.simpleName)
                                .addToBackStack(null)
                                .commit()
                        }
                        else if (userType == "admin") {
                            activity.supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container_view_tag_admin,pdfDetailFragment,
                                    PdfDetailFragment::class.java.simpleName)
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })

        }

        holder.favorite.setOnClickListener {
            MyApplication.removeFromFavorite(context,model.id)
        }
    }

    private fun loadBookDetails(model: Pdf, holder: HolderPdfFavorite) {
        val bookId = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val categoryId = snapshot.child("categoryId").value
                        val description = snapshot.child("description").value
                        val downloadCount = snapshot.child("downloadCount").value
                        val title = snapshot.child("title").value
                        val timestamp = snapshot.child("timestamp").value
                        val url = snapshot.child("url").value
                        val uid = snapshot.child("uid").value
                        val viewsCount = snapshot.child("viewsCount").value

                        model.isFavorite = true
                        model.title = title.toString()
                        model.description = description.toString()
                        model.categoryId = categoryId.toString()
                        model.timestamp = timestamp.toString().toLong()
                        model.uid = uid.toString()
                        model.url = url.toString()
                        model.viewCount = viewsCount.toString().toLong()
                        model.downloadCount = downloadCount.toString().toLong()

                        val date = MyApplication.formatTimeStamp(timestamp.toString().toLong())

                        MyApplication.loadCategory(categoryId.toString(),holder.categoryTv)
                        MyApplication.loadPdfSize(url.toString(),title.toString(),holder.sizeTv)
                        MyApplication.loadPdfFromUrlSinglePage(url.toString(),title.toString(),holder.pdfView,holder.progressBar,null)

                        holder.titleTv.text = title.toString()
                        holder.desTv.text = description.toString()
                        holder.dateTv.text = date

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
    }

    override fun getItemCount(): Int {
        return booksArrayList.size
    }

}