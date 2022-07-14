package com.example.bookapp2.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.bookapp2.ui.fragments.DashBoardAdminFragment
import com.example.bookapp2.R
import com.example.bookapp2.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAdminBinding
    private val dashBoardAdminFragment = DashBoardAdminFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addFragment(dashBoardAdminFragment)
    }
    private fun addFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragment_container_view_tag_admin, fragment)
        transaction.commit()
    }
}