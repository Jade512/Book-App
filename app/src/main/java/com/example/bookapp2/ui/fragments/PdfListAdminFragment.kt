package com.example.bookapp2.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bookapp2.adapter.AdapterPdfAdmin
import com.example.bookapp2.databinding.FragmentPdfListAdminBinding
import com.example.bookapp2.model.Pdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.Exception


class PdfListAdminFragment : Fragment() {
    private lateinit var binding: FragmentPdfListAdminBinding
    private lateinit var pdfArrayList : ArrayList<Pdf>
    private lateinit var adapterPdfAdmin : AdapterPdfAdmin
    private var categoryId = ""
    private var category = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPdfListAdminBinding.inflate(layoutInflater)

        categoryId = arguments?.getString("categoryId")!!
        category = arguments?.getString("category")!!

        binding.backBtn.setOnClickListener {

        }
        loadPdfList()

        binding.subTitleTv.text = category

        binding.edtSearch.addTextChangedListener(object  : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPdfAdmin.filter.filter(p0)
                }
                catch (e : Exception){
                    Log.e("Error","onTextChanged: ${e.message}")
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }

        })

        return binding.root
    }

    private fun loadPdfList() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children){
                        val model = ds.getValue(Pdf::class.java)
                        if (model != null){
                            pdfArrayList.add(model)
                        }
                    }
                    try {
                        adapterPdfAdmin = AdapterPdfAdmin(activity!!.applicationContext,pdfArrayList)
                    }
                    catch (e: Exception){
                        Log.e("PDFLIST",e.message.toString())
                    }

                    binding.rvBooks.adapter = adapterPdfAdmin
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }


}