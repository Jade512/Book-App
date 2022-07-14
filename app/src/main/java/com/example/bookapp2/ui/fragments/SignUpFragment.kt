package com.example.bookapp2.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.bookapp2.databinding.FragmentSignUpBinding
import com.example.bookapp2.ui.activities.UserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var mAuth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(layoutInflater)
        mAuth = FirebaseAuth.getInstance()

        binding.registerBtn.setOnClickListener {
            validateData()
        }

        return binding.root
    }

    private var name = " "
    private var email = " "
    private var passoword = " "

    private fun validateData() {
        name = binding.nameEdt.text.toString().trim()
        email = binding.emailEdt.text.toString().trim()
        passoword = binding.passwordEdt.text.toString().trim()
        val cPassword = binding.passwordConfirmEdt.text.toString().trim()
        if (name.isEmpty()){
            Toast.makeText(activity, "Please Enter Your Name", Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(activity, "Invalid Email Pattern", Toast.LENGTH_SHORT).show()
        }
        else if (passoword.isEmpty()){
            Toast.makeText(activity, "Please Enter Your Password", Toast.LENGTH_SHORT).show()
        }
        else if (passoword.isEmpty()) {
            Toast.makeText(activity, "Please Enter Confirm Password", Toast.LENGTH_SHORT).show()
        }
        else if (passoword != cPassword) {
            Toast.makeText(activity, "Password doesn't match", Toast.LENGTH_SHORT).show()
        }
        else {
            signUp()
        }
    }

    private fun signUp() {
        mAuth.createUserWithEmailAndPassword(email, passoword)
            .addOnSuccessListener {
                updateUserInfo()
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed creating account due to ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserInfo() {
        val timestamp = System.currentTimeMillis()
        val uid = mAuth.uid
        val hashMap : HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = " "
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(activity, "Account created", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this.requireContext(), UserActivity::class.java))
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed saving user due to ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

}