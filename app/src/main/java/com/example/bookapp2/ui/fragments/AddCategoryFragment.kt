package com.example.bookapp2.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.bookapp2.R
import com.example.bookapp2.databinding.FragmentAddCategoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class AddCategoryFragment : Fragment() {

    private lateinit var binding: FragmentAddCategoryBinding
    private lateinit var mAuth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddCategoryBinding.inflate(layoutInflater)
        mAuth = FirebaseAuth.getInstance()
        binding.backBtn.setOnClickListener {

        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }

        return binding.root
    }

    private var category = ""

    private fun validateData() {
        category = binding.categoryEdt.text.toString().trim()
        if (category.isEmpty()) {
            Toast.makeText(activity, "Please! Enter Category", Toast.LENGTH_SHORT).show()
        }
        else {
            binding.progressBar.visibility = View.VISIBLE
            addCategoryFirebase()
        }
    }

    private fun addCategoryFirebase() {
        val timestamp = System.currentTimeMillis()
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${mAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                binding.categoryEdt.text = null
                Toast.makeText(activity, "Added Successfully", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.INVISIBLE
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to add due to ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

}