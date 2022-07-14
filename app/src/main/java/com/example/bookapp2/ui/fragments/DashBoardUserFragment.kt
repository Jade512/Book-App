package com.example.bookapp2.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.example.bookapp2.R
import com.example.bookapp2.databinding.FragmentDashBoardUserBinding
import com.example.bookapp2.model.Category
import com.example.bookapp2.ui.activities.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class DashBoardUserFragment : Fragment() {

    private lateinit var binding: FragmentDashBoardUserBinding
    private lateinit var mAuth: FirebaseAuth

    private lateinit var categoryArrayList : ArrayList<Category>
    private lateinit var viewPagerAdapter : ViewPagerAdapter
    private val profileFragment = ProfileFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashBoardUserBinding.inflate(layoutInflater)
        mAuth = FirebaseAuth.getInstance()
        checkUser()

//        setupWithViewPagerAdapter(binding.viewPager)
//        binding.tabLayout.setupWithViewPager(binding.viewPager)

//        binding.logoutBtn.setOnClickListener {
//            mAuth.signOut()
//            startActivity(Intent(requireActivity(),MainActivity::class.java))
//        }
//
//        val fragmentTransaction = this@DashBoardUserFragment.parentFragmentManager
//
//        binding.btnProfile.setOnClickListener{
//            fragmentTransaction.beginTransaction().apply {
//                replace(R.id.fragment_container_view_tag_user, profileFragment, ProfileFragment::class.java.simpleName)
//                    .addToBackStack(null)
//                    .commit()
//            }
//        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.logoutBtn.setOnClickListener {
            mAuth.signOut()
            startActivity(Intent(requireActivity(),MainActivity::class.java))
            requireActivity().finish()
        }

        val fragmentTransaction = this@DashBoardUserFragment.parentFragmentManager

        binding.btnProfile.setOnClickListener{
            fragmentTransaction.beginTransaction().apply {
                replace(R.id.fragment_container_view_tag_user, profileFragment, ProfileFragment::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager){
        viewPagerAdapter = ViewPagerAdapter(
            this@DashBoardUserFragment.parentFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            requireActivity().applicationContext
        )
        categoryArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()

                val modelAll = Category("01","All",1,"")
                val modelMostViewed = Category("01","Most Viewed",1,"")
                val modelMostDownload = Category("01","Most Downloaded",1,"")

                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                categoryArrayList.add(modelMostDownload)

                viewPagerAdapter.addFragment(
                    BookUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                        "${modelAll.uid}"
                    ), modelAll.category
                )
                viewPagerAdapter.addFragment(
                    BookUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",
                        "${modelMostViewed.uid}"
                    ), modelMostViewed.category
                )
                viewPagerAdapter.addFragment(
                    BookUserFragment.newInstance(
                        "${modelMostDownload.id}",
                        "${modelMostDownload.category}",
                        "${modelMostDownload.uid}"
                    ), modelMostDownload.category
                )
                viewPagerAdapter.notifyDataSetChanged()
                for (ds in snapshot.children){
                    val model = ds.getValue(Category::class.java)
                    categoryArrayList.add(model!!)

                    viewPagerAdapter.addFragment(
                        BookUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        ), model.category
                    )
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        viewPager.adapter = viewPagerAdapter
    }

    class ViewPagerAdapter(
        fragmentManager : FragmentManager, behavior : Int, context: Context
    ) : FragmentPagerAdapter(fragmentManager, behavior) {
        private val fragmentList: ArrayList<BookUserFragment> = ArrayList()
        private val fragmentTitleList : ArrayList<String> = ArrayList()
        private val context : Context
        init {
            this.context = context
        }
        override fun getCount(): Int {
            return fragmentList.size
        }
        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }
        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }
        fun addFragment(fragment: BookUserFragment, title : String){
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }
    }

    private fun loadImage(){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val image = snapshot.child("profileImage").value
                    Glide.with(this@DashBoardUserFragment.requireContext()).load(image).into(binding.btnProfile)
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun checkUser() {
        val user = mAuth.currentUser
        if (user == null){
            binding.subTitleTv.text = "Not logged In"
            binding.btnProfile.visibility = View.GONE
            binding.logoutBtn.visibility = View.GONE
        }
        else {
            val email = user.email
            binding.subTitleTv.text = email
            binding.btnProfile.visibility = View.VISIBLE
            binding.logoutBtn.visibility = View.VISIBLE
            loadImage()
        }
    }
}