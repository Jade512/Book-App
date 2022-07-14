package com.example.bookapp2.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bookapp2.adapter.AdapterPdfUser
import com.example.bookapp2.databinding.FragmentBookUserBinding
import com.example.bookapp2.model.Pdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.Exception


class BookUserFragment : Fragment() {

    private lateinit var binding: FragmentBookUserBinding

    companion object {
        const val TAG = "BOOKS_USER"

        fun newInstance(categoryId: String, category: String, uid: String): BookUserFragment {
            val fragment = BookUserFragment()
            val args = Bundle()
            args.putString("categoryId",categoryId)
            args.putString("category",category)
            args.putString("uid",uid)
            fragment.arguments = args
            return fragment
        }

    }
    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList : ArrayList<Pdf>
    private lateinit var adapterPdfUser : AdapterPdfUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookUserBinding.inflate(layoutInflater)

        when (category) {
            "All" -> {
                loadAllBook()
            }
            "Most Viewed" -> {
                loadMostViewedBooks("viewsCount")
            }
            "Most Downloaded" -> {
                loadMostDownloadedBooks("downloadCount")
            }
            else -> {
                loadCategorizedBooks()
            }
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPdfUser.filter.filter(p0)
                }
                catch (e: Exception){
                    Log.e(TAG,"${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        return binding.root

    }

    private fun loadCategorizedBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children)
                    {
                        val model = ds.getValue(Pdf::class.java)
                        pdfArrayList.add(model!!)
                    }
                    try {
                        adapterPdfUser = AdapterPdfUser(requireActivity().applicationContext,pdfArrayList)
                    }
                    catch (e: Exception){

                    }
                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadMostDownloadedBooks(s: String) {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(s).limitToLast(10)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children)
                    {
                        val model = ds.getValue(Pdf::class.java)
                        pdfArrayList.add(model!!)
                    }
                    try {
                        adapterPdfUser = AdapterPdfUser(requireActivity().applicationContext,pdfArrayList)
                    }
                    catch (e: Exception){

                    }

                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadMostViewedBooks(s: String) {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(s).limitToLast(10)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children)
                    {
                        val model = ds.getValue(Pdf::class.java)
                        pdfArrayList.add(model!!)
                    }
                    try {
                        adapterPdfUser = AdapterPdfUser(context!!,pdfArrayList)
                    }
                    catch (e: Exception){

                    }

                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadAllBook() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children)
                {
                    val model = ds.getValue(Pdf::class.java)
                    pdfArrayList.add(model!!)
                }
                try {
                    adapterPdfUser = AdapterPdfUser(requireActivity().applicationContext,pdfArrayList)
                }
                catch (e: Exception){
                }
                binding.booksRv.adapter = adapterPdfUser
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

}