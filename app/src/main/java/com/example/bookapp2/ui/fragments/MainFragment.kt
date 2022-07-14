package com.example.bookapp2.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bookapp2.R
import com.example.bookapp2.databinding.FragmentMainBinding
import com.example.bookapp2.ui.activities.UserActivity


class MainFragment : Fragment() {
    private lateinit var binding : FragmentMainBinding
    private val loginFragment = LoginFragment()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(layoutInflater)
        val fragmentManager = this@MainFragment.parentFragmentManager
        binding.loginBtn.setOnClickListener {
            fragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_view_tag, loginFragment, LoginFragment::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
        binding.skipBtn.setOnClickListener {
            val intent = Intent(this.requireContext(), UserActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }

}