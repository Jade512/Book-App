package com.example.bookapp2.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.bookapp2.R
import com.example.bookapp2.databinding.FragmentEditPdfBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class EditPdfFragment : Fragment() {

    private lateinit var binding: FragmentEditPdfBinding
    private var bookId = ""
    private lateinit var categoryTitleArrayList : ArrayList<String>
    private lateinit var categoryIdArrayList: ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditPdfBinding.inflate(layoutInflater)
        bookId = arguments?.getString("bookId")!!
        loadCategories()
        loadBookInfo()

        binding.backBtn.setOnClickListener {
        }

        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        binding.smBtn.setOnClickListener {
            validateData()
        }
        return binding.root
    }

    private var title = ""
    private var description = ""
    private fun validateData() {
        title = binding.titleEdt.text.toString().trim()
        description = binding.desEdt.text.toString().trim()
        if (title.isEmpty()){
            Toast.makeText(activity, "Please Enter Title", Toast.LENGTH_SHORT).show()
        }
        else if (description.isEmpty())
        {
            Toast.makeText(activity, "Please Enter Description", Toast.LENGTH_SHORT).show()
        }
        else if (selectedCategoryId.isEmpty()) {
            Toast.makeText(activity, "Pick category", Toast.LENGTH_SHORT).show()
        }
        else {
            updatePdf()
        }
    }

    private fun updatePdf() {
        binding.progressBar.visibility = View.VISIBLE
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.INVISIBLE
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed due to ${it.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.INVISIBLE
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryDialog() {
        val categories = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categories.indices){
            categories[i] = categoryTitleArrayList[i]
        }

        val builder = AlertDialog.Builder(this@EditPdfFragment.requireContext())
        builder.setTitle("Choose Category")
            .setItems(categories){dialog, position ->
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]

                binding.categoryTv.text = selectedCategoryTitle
            }
            .show()
    }

    private fun loadBookInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()
                    binding.titleEdt.setText(title)
                    binding.desEdt.setText(description)

                    val refBook = FirebaseDatabase.getInstance().getReference("Categories")
                    refBook.child("selectedCategoryId")
                        .addListenerForSingleValueEvent(object :ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                binding.categoryTv.text = snapshot.child("category").value.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadCategories() {
        categoryIdArrayList = ArrayList()
        categoryTitleArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryTitleArrayList.clear()
                categoryIdArrayList.clear()
                for (ds in snapshot.children){
                    val id = ds.child("id").value.toString()
                    val category = ds.child("category").value.toString()

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


}