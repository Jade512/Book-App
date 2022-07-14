package com.example.bookapp2.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.bookapp2.MyApplication
import com.example.bookapp2.R
import com.example.bookapp2.adapter.AdapterPdfFavorite
import com.example.bookapp2.databinding.FragmentProfileBinding
import com.example.bookapp2.model.Pdf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.Exception

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var booksArrayList: ArrayList<Pdf>
    private lateinit var adapterFV : AdapterPdfFavorite
    private val editProfileFragment = EditProfileFragment()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        mAuth = FirebaseAuth.getInstance()
        loadUser()
        loadFavoriteBooks()
        val fragmentManager = this@ProfileFragment.parentFragmentManager

        binding.editBtn.setOnClickListener {
            fragmentManager.beginTransaction().apply {
                val ref = FirebaseDatabase.getInstance().getReference("Users")
                ref.child(mAuth.currentUser!!.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userType = snapshot.child("userType").value
                            if (userType == "user"){
                                replace(R.id.fragment_container_view_tag_user,editProfileFragment,EditProfileFragment::class.java.simpleName)
                                    .addToBackStack(null)
                                    .commit()
                            }
                            else if (userType == "admin") {
                                replace(R.id.fragment_container_view_tag_admin,editProfileFragment,EditProfileFragment::class.java.simpleName)
                                    .addToBackStack(null)
                                    .commit()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })

            }
        }

        return binding.root
    }

    private fun loadFavoriteBooks() {
        booksArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    booksArrayList.clear()
                    for (ds in snapshot.children){
                        val bookId = ds.child("bookId").value
                        val modelPdf = Pdf()
                        modelPdf.id = bookId.toString()

                        booksArrayList.add(modelPdf)
                    }

                    binding.FavoriteTV.text  = booksArrayList.size.toString()
                    try {
                        adapterFV = AdapterPdfFavorite(requireActivity().applicationContext,booksArrayList)
                    }
                    catch (e: Exception) {
                        Log.e("PROFILE_FRAGMENT",e.message.toString())
                    }
                    binding.rvBooksFv.adapter = adapterFV
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadUser() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = snapshot.child("email").value
                    val name = snapshot.child("name").value
                    val profileImage = snapshot.child("profileImage").value
                    val timestamp = snapshot.child("timestamp").value
                    val uid = snapshot.child("uid").value
                    val userType = snapshot.child("userType").value
                    val date = MyApplication.formatTimeStamp(timestamp.toString().toLong())

                    binding.nameTv.text = name.toString()
                    binding.emailTv.text = email.toString()
                    binding.memberTv.text = date
                    binding.accountTypeTv.text = userType.toString()

                    try {
                        Glide.with(this@ProfileFragment.requireContext()).load(profileImage)
                            .placeholder(R.drawable.ic_baseline_person_24).into(binding.profileIv)
                    }
                    catch (e: Exception){
                        Log.e("ProFile","${e.message}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }


}