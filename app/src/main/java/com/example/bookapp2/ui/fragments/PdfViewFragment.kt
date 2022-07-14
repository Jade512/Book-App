package com.example.bookapp2.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.bookapp2.R
import com.example.bookapp2.databinding.FragmentPdfViewBinding
import com.example.bookapp2.util.Constant
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class PdfViewFragment : Fragment() {

    private lateinit var binding: FragmentPdfViewBinding
    private companion object{
        const val TAG = "PDF_VIEW_TAG"
    }
    private var bookId = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPdfViewBinding.inflate(layoutInflater)
        bookId = arguments?.getString("bookId")!!

        loadBookDetail()
        return binding.root

    }

    private fun loadBookDetail() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pdfUrl = snapshot.child("url").value
                    loadBookFromUrl(pdfUrl.toString())
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun loadBookFromUrl(pdfUrl: String) {
        val sRef = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        sRef.getBytes(Constant.MAX_BYTE_PDF)
            .addOnSuccessListener {
                binding.pdfView.fromBytes(it)
                    .swipeHorizontal(false)
                    .onPageChange{ page, pageCount ->
                        val currentPage =  page + 1
                        binding.subTitleTv.text ="$currentPage/$pageCount"
                    }
                    .onError {error->
                        Log.e(TAG,error.message.toString())
                    }
                    .onPageError { page, t ->
                        Log.e(TAG,t.message.toString() + "$page")
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to Reading due to ${it.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
    }

}