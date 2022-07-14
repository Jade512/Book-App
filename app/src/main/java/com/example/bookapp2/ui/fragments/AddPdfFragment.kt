package com.example.bookapp2.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bookapp2.databinding.FragmentAddPdfBinding
import com.example.bookapp2.model.Category
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class AddPdfFragment : Fragment() {

    private lateinit var binding : FragmentAddPdfBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var categoryArrayList: ArrayList<Category>
    private var pdfUri : Uri?= null
    private val TAG = "PDF_ADD_TAG"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddPdfBinding.inflate(layoutInflater)
        mAuth = FirebaseAuth.getInstance()
        loadPdfCategory()
        binding.categoryTv.setOnClickListener {
            categoryPicked()
        }

        binding.backBtn.setOnClickListener {

        }

        binding.attachImb.setOnClickListener {
            pdfPickIntent()
        }

        binding.smBtn.setOnClickListener {
            validateData()
        }
        return binding.root
    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        title = binding.titleEdt.text.toString().trim()
        description = binding.desEdt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        if (title.isEmpty()){
            Toast.makeText(activity, "Please Enter Title", Toast.LENGTH_SHORT).show()
        }
        else if (description.isEmpty())
        {
            Toast.makeText(activity, "Please Enter Description", Toast.LENGTH_SHORT).show()
        }
        else if (category.isEmpty()) {
            Toast.makeText(activity, "Please Pick Category", Toast.LENGTH_SHORT).show()
        }
        else if (pdfUri == null) {
            Toast.makeText(activity, "Please Pick Pdf", Toast.LENGTH_SHORT).show()
        }
        else {
            binding.progressBar.visibility = View.VISIBLE
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        val timestamp = System.currentTimeMillis()
        val filePathAndName = "Book/$timestamp"
        val storageRef = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageRef.putFile(pdfUri!!)
            .addOnSuccessListener {
                val uriTask : Task<Uri> = it.storage.downloadUrl
                while(!uriTask.isSuccessful);
                val uploadPdfUrl = "${uriTask.result}"
                uploadPdfIntoDb(uploadPdfUrl,timestamp)
                binding.progressBar.visibility = View.INVISIBLE
            }
            .addOnFailureListener{
                Toast.makeText(activity, "Failed Upload due to ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPdfIntoDb(uploadPdfUrl: String, timestamp: Long) {
        val uid  = mAuth.uid
        val hashMap : HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"]  = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadCount"] = 0

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show()
                pdfUri = null
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed due to ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private val getPdf = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback {
            if (it.resultCode == AppCompatActivity.RESULT_OK){
                pdfUri = it.data!!.data
            }
            else {
                Toast.makeText(activity, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private fun pdfPickIntent() {
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        getPdf.launch(intent)
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun loadPdfCategory() {
        categoryArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(Category::class.java)
                    categoryArrayList.add(model!!)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun categoryPicked() {
        val categories = arrayOfNulls<String>(categoryArrayList.size)
        for (i in 0 until categoryArrayList.size){
            categories[i] = categoryArrayList[i].category
        }

        val builder = AlertDialog.Builder(this@AddPdfFragment.requireContext())
        builder.setTitle("Pick Category")
            .setItems(categories){dialog , which ->
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id

                binding.categoryTv.text = selectedCategoryTitle
            }
            .show()
    }


}