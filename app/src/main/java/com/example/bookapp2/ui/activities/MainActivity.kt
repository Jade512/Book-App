package com.example.bookapp2.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.bookapp2.ui.fragments.MainFragment
import com.example.bookapp2.R
import com.example.bookapp2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainFragment = MainFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addFragment(mainFragment)

    }

    private fun addFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragment_container_view_tag, fragment)
        transaction.commit()
    }
}