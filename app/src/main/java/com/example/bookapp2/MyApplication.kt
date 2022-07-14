package com.example.bookapp2

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.bookapp2.util.Constant.MAX_BYTE_PDF
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        fun formatTimeStamp(timestamp : Long) : String {
            val sdf = SimpleDateFormat("dd MM yyyy - HH:mm")
            return sdf.format(timestamp)
        }

        @SuppressLint("SetTextI18n")
        fun loadPdfSize(pdfUrl : String, pdfTitle: String, sizeTv : TextView){
            val strRef = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            strRef.metadata
                .addOnSuccessListener {
                    val bytes = it.sizeBytes.toDouble()
                    val kb = bytes/1024
                    val mb = kb/1024
                    if (mb >=1){
                        sizeTv.text = String.format("%.2f",mb) + "MB"
                    }
                    else if (kb >=1 ){
                        sizeTv.text = String.format("%.2f",kb) + "KB"
                    }
                    else {
                        sizeTv.text = String.format("%.2f",bytes) + "bytes"
                    }
                }
                .addOnFailureListener {
                    Log.e("Error","Load Size Failed due to ${it.message}")
                }
        }

        fun loadPdfFromUrlSinglePage(
            pdfUrl: String, pdfTitle: String,
            pdfView: PDFView, progressBar: ProgressBar,
            pageTv: TextView?
        ){
            val strRef = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            strRef.getBytes(MAX_BYTE_PDF)
                .addOnSuccessListener {
                    pdfView.fromBytes(it)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError {
                            progressBar.visibility = View.INVISIBLE
                        }.onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                        }
                        .onLoad { nPage ->
                            progressBar.visibility = View.INVISIBLE
                            if (pageTv != null){
                                pageTv.text = "$nPage"
                            }
                        }
                        .load()
                }
                .addOnFailureListener {
                    Log.e("Error","Load Size Failed due to ${it.message}")
                }
        }

        fun loadCategory(categoryId : String, categoryTv : TextView){
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val category : String = "${snapshot.child("category").value}"

                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }

        fun deleteBook(context: Context, bookId : String, bookUrl : String, bookTitle : String){
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageRef.delete()
                .addOnSuccessListener {
                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to delete due to ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to delete due to ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        fun incrementBookView(bookId: String){
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var viewCount = "" + snapshot.child("viewsCount").value
                        if (viewCount == "" || viewCount == "null"){
                            viewCount = "0"

                        }

                        val newsViewCount = viewCount.toLong() +  1
                        val hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = newsViewCount

                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    } })
        }

        fun removeFromFavorite(context: Context, bookId: String){
            val TAG = "Application"
            val mAuth = FirebaseAuth.getInstance()
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(mAuth.uid!!).child("Favorites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d(TAG,"Success Remove")
                }
                .addOnFailureListener {
                    Log.e(TAG,"Failed to remove favorite book due to ${it.message}")
                }
        }
    }
}