package com.example.bookapp2.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.bookapp2.ui.fragments.DashBoardUserFragment
import com.example.bookapp2.R
import com.example.bookapp2.databinding.ActivityUserBinding

class UserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserBinding
    private val dashBoardUserFragment = DashBoardUserFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addFragment(dashBoardUserFragment)
    }

    private fun addFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragment_container_view_tag_user, fragment)
        transaction.commit()
    }
}