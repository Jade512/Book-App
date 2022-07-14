package com.example.bookapp2.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.bookapp2.R
import com.example.bookapp2.adapter.AdapterCategory
import com.example.bookapp2.databinding.FragmentDashBoardAdminBinding
import com.example.bookapp2.model.Category
import com.example.bookapp2.ui.activities.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception


class DashBoardAdminFragment : Fragment() {

    private lateinit var binding: FragmentDashBoardAdminBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var categoryArrayList: ArrayList<Category>
    private lateinit var adapter: AdapterCategory
    private val addCategoryFragment = AddCategoryFragment()
    private val addPdfFragment = AddPdfFragment()
    private val profileFragment = ProfileFragment()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashBoardAdminBinding.inflate(layoutInflater)
        mAuth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkUser()
        loadCategories()
        loadImage()

        binding.logoutBtn.setOnClickListener {
            mAuth.signOut()
            startActivity(Intent(this.requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }
        val fragmentTransaction = this@DashBoardAdminFragment.parentFragmentManager
        binding.addCategoryBtn.setOnClickListener {
            fragmentTransaction.beginTransaction().apply {
                replace(R.id.fragment_container_view_tag_admin, addCategoryFragment, AddCategoryFragment::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.addPdfFab.setOnClickListener {
            fragmentTransaction.beginTransaction().apply {
                replace(R.id.fragment_container_view_tag_admin, addPdfFragment, AddPdfFragment::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.searchEdt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapter.filter.filter(p0)
                }
                catch (e : Exception){
                }
            }
            override fun afterTextChanged(p0: Editable?) {

            }
        })

        binding.btnProfile.setOnClickListener{
            fragmentTransaction.beginTransaction().apply {
                replace(R.id.fragment_container_view_tag_admin, profileFragment, ProfileFragment::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun loadImage() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val image = snapshot.child("profileImage").value
                    Glide.with(this@DashBoardAdminFragment.requireContext()).load(image).placeholder(R.drawable.ic_baseline_person_24)
                        .into(binding.btnProfile)
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadCategories() {
        categoryArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object  : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(Category::class.java)
                    categoryArrayList.add(model!!)
                }
                adapter = AdapterCategory(this@DashBoardAdminFragment.requireContext(),categoryArrayList)
                binding.rvCategories.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun checkUser() {
        val user = mAuth.currentUser
        if (user == null){
            startActivity(Intent(this.requireContext(), MainActivity::class.java))
        }
        else {
            val email = user.email
            binding.subTitleTv.text = email
        }
    }

}