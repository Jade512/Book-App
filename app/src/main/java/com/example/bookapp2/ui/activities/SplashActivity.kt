package com.example.bookapp2.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bookapp2.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var mAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        MainScope().launch {
            checkUser()
            delay(3000L)
        }

    }

    private fun checkUser() {
        val user = mAuth.currentUser
        if (user == null) {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
        else {
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(user.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userType = snapshot.child("userType").value
                        if (userType == "user"){
                            startActivity(Intent(this@SplashActivity, UserActivity::class.java))
                            finish()
                        }
                        else if (userType == "admin") {
                            startActivity(Intent(this@SplashActivity, AdminActivity::class.java))
                            finish()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
        }
}