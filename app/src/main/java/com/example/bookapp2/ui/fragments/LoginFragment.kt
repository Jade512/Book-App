package com.example.bookapp2.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.bookapp2.R
import com.example.bookapp2.databinding.FragmentLoginBinding
import com.example.bookapp2.ui.activities.AdminActivity
import com.example.bookapp2.ui.activities.UserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private val signUpFragment = SignUpFragment()
    private val forgotPassWordFragment = ForgotPassWordFragment()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        mAuth = FirebaseAuth.getInstance()
        val fragmentManager = this@LoginFragment.parentFragmentManager

        binding.noAccountTv.setOnClickListener {
            fragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_view_tag, signUpFragment, SignUpFragment::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.loginBtn.setOnClickListener {
            validateData()
        }

        binding.forgotTv.setOnClickListener {
            fragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_view_tag,forgotPassWordFragment, ForgotPassWordFragment::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }

        return binding.root
    }

    private var email = " "
    private var password = " "

    private fun validateData() {
        email = binding.emailEdt.text.toString().trim()
        password = binding.passwordEdt.text.toString().trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(activity, "Invalid Email Format", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()) {
            Toast.makeText(activity, "Please Enter Your Password", Toast.LENGTH_SHORT).show()
        }
        else {
            login()
        }
    }

    private fun login() {
        mAuth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                checkUser()
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Email or Password incorrect", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        val user = mAuth.currentUser!!
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(user.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userType = snapshot.child("userType").value
                    if (userType == "user"){
                        startActivity(Intent(this@LoginFragment.requireContext(), UserActivity::class.java))
                    }
                    else if (userType == "admin") {
                        startActivity(Intent(this@LoginFragment.requireContext(), AdminActivity::class.java))
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }

            })
    }


}