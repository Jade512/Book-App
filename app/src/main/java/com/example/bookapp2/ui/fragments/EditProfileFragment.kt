package com.example.bookapp2.ui.fragments

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.bookapp2.R
import com.example.bookapp2.databinding.FragmentEditProfileBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.lang.Exception


class EditProfileFragment : Fragment() {

    lateinit var binding : FragmentEditProfileBinding
    private lateinit var mAuth: FirebaseAuth
    private var imageUri : Uri?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(layoutInflater)

        mAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        binding.avatar.setOnClickListener {
            showImageAttach()
        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }

        return binding.root
    }
    private var name = ""
    private fun validateData() {
        name = binding.nameEdt.text.toString().trim()
        if (name.isEmpty()){
            Toast.makeText(activity, "Please Enter Your Name", Toast.LENGTH_SHORT).show()
        }
        else {
            if (imageUri == null){
                updateProfile("")
            }
            else {
                uploadImage()
            }
        }
    }

    private fun uploadImage() {
        val filePathAndName = "ProfileImages/${mAuth.uid}"
        val strRef = FirebaseStorage.getInstance().getReference(filePathAndName)
        strRef.putFile(imageUri!!)
            .addOnSuccessListener {
                val uriTask : Task<Uri> = it.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadImageUrl = "${uriTask.result}"
                updateProfile(uploadImageUrl)
            }
            .addOnFailureListener{
                Toast.makeText(activity, "Failed to upload Image ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfile(uploadImageUrl: String) {
        val hashMap : HashMap<String, Any> = HashMap()
        hashMap["name"] = name
        if (imageUri != null){
            hashMap["profileImage"] = uploadImageUrl
        }
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.e("Edit profile", it.message.toString())
            }
    }

    private fun showImageAttach() {
        val popupMenu = PopupMenu(this@EditProfileFragment.requireContext(),binding.avatar)
        popupMenu.menu.add(Menu.NONE,0,0,"Camera")
        popupMenu.menu.add(Menu.NONE,1,1,"Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val id = item.itemId
            if (id == 0){
                pickImageCamera()
            }
            else if (id == 1) {
                pickImageGallery()
            }

            true
        }
    }

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture(),
        ActivityResultCallback {
            if (it) {
                binding.avatar.setImageURI(imageUri)
            }
        }
    )

    private val getPicture = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback {
            if (it.resultCode == AppCompatActivity.RESULT_OK){
                imageUri = it.data!!.data
                binding.avatar.setImageURI(imageUri)
            }
            else {
                Toast.makeText(activity, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getPicture.launch(intent)
    }

    private fun pickImageCamera() {

        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE,"Title")
        values.put(MediaStore.Images.Media.DESCRIPTION,"Description")

        imageUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)
        takePicture.launch(imageUri)
    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").value
                    val profileImage = snapshot.child("profileImage").value
                    val timestamp = snapshot.child("timestamp").value

                    binding.nameEdt.setText(name.toString())

                    try {
                        Glide.with(this@EditProfileFragment.requireContext()).load(profileImage)
                            .placeholder(R.drawable.ic_baseline_person_24).into(binding.avatar)
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

