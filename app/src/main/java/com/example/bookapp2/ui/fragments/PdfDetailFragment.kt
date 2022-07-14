package com.example.bookapp2.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.example.bookapp2.MyApplication
import com.example.bookapp2.R
import com.example.bookapp2.adapter.AdapterComment
import com.example.bookapp2.databinding.DialogCommentBinding
import com.example.bookapp2.databinding.FragmentPdfDetailBinding
import com.example.bookapp2.model.Comment
import com.example.bookapp2.ui.activities.AdminActivity
import com.example.bookapp2.ui.activities.UserActivity
import com.example.bookapp2.util.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream
import java.lang.Exception


class PdfDetailFragment : Fragment() {

    private lateinit var binding: FragmentPdfDetailBinding
    private lateinit var mAuth : FirebaseAuth
    private lateinit var cmtList : ArrayList<Comment>
    private lateinit var adapterComment: AdapterComment
    private val pdfViewFragment = PdfViewFragment()
    private companion object {
        const val TAG = "PDF_DETAILS"
    }
    private var bookId = ""
    private var bookTitle = ""
    private var bookUrl = ""
    private var isMyFavorite = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPdfDetailBinding.inflate(layoutInflater)
        mAuth = FirebaseAuth.getInstance()
        bookId = arguments?.getString("bookId")!!
        if (mAuth.currentUser != null) {
            checkIsFavorite()
        }
        MyApplication.incrementBookView(bookId)
        loadBookDetails()
        showComment()

        binding.btnRead.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("bookId",bookId)
            val fragmentManager = this@PdfDetailFragment.parentFragmentManager
            pdfViewFragment.arguments = bundle
            fragmentManager.beginTransaction().apply {

                if (mAuth.currentUser == null){
                    replace(R.id.fragment_container_view_tag_user, pdfViewFragment, PdfViewFragment::class.java.simpleName)
                        .addToBackStack(null)
                        .commit()
                }
                else {
                    val ref = FirebaseDatabase.getInstance().getReference("Users")
                    ref.child(mAuth.currentUser!!.uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val userType = snapshot.child("userType").value
                                if (userType == "user"){
                                    replace(R.id.fragment_container_view_tag_user, pdfViewFragment, PdfViewFragment::class.java.simpleName)
                                        .addToBackStack(null)
                                        .commit()
                                }
                                else if (userType == "admin") {
                                    replace(R.id.fragment_container_view_tag_admin, pdfViewFragment, PdfViewFragment::class.java.simpleName)
                                        .addToBackStack(null)
                                        .commit()
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                }

            }
        }

        binding.btnDownload.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this@PdfDetailFragment.requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG,"onCreate: Storage permission is already granted")
                downloadBook()
            }
            else {
                requestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        binding.btnFavorite.setOnClickListener {
            if (mAuth.currentUser == null){
                Toast.makeText(activity, "You're not logged in", Toast.LENGTH_SHORT).show()
            }
            else {
                if (isMyFavorite){
                    removeFromFavorite()
                }
                else {
                    addToFavorite()
                }
            }
        }

        binding.addCmtBtn.setOnClickListener {
            if (mAuth.currentUser == null) {
                Toast.makeText(activity, "Please Log in", Toast.LENGTH_SHORT).show()
            }
            else {
                addCommentDialog()
            }
        }

        return binding.root
    }

    private fun addCommentDialog() {
        val cmtDialog = DialogCommentBinding.inflate(LayoutInflater.from(this@PdfDetailFragment.requireContext()))
        val builder = AlertDialog.Builder(this@PdfDetailFragment.requireContext(), R.style.CustomDialog)
        builder.setView(cmtDialog.root)

        val alertDialog = builder.create()
        alertDialog.show()

        cmtDialog.backBtn.setOnClickListener {
            alertDialog.dismiss()
        }

        cmtDialog.cmtBtn.setOnClickListener {
            comment = cmtDialog.commentEdt.text.toString().trim()
            if (comment.isEmpty()) {
                Toast.makeText(activity, "Type your comments", Toast.LENGTH_SHORT).show()
            }
            else {
                alertDialog.dismiss()
                addComment()
            }
        }
    }

    private fun addComment() {
        val timestamp = System.currentTimeMillis().toString()
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = timestamp
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp
        hashMap["comment"] = comment
        hashMap["uid"] = mAuth.uid.toString()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {

            }
            .addOnFailureListener {
                Log.e("DETAIL",it.message.toString())
            }
    }

    private fun addToFavorite() {
        val timestamp = System.currentTimeMillis()

        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG,"Success Add")
            }
            .addOnFailureListener {
                Log.e(TAG,"Failed to add favorite ${it.message}")
            }
    }

    private fun removeFromFavorite() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!).child("Favorites").child(bookId)
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG,"Success Remove")
            }
            .addOnFailureListener {
                Log.e(TAG,"Failed to remove favorite book due to ${it.message}")
            }
    }

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        if (it){
            Log.d(TAG,"onCreate: Storage permission is already granted")
        }
        else {
            Log.d(TAG,"onCreate: Storage permission is denied")
        }
    }

    private fun downloadBook() {
        binding.progress.visibility = View.VISIBLE
        val strRef = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        strRef.getBytes(Constant.MAX_BYTE_PDF)
            .addOnSuccessListener {
                saveToFolder(it)
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Download Failed due to ${it.message}", Toast.LENGTH_SHORT).show()
                binding.progress.visibility = View.INVISIBLE
            }
    }

    private fun saveToFolder(it: ByteArray?) {
        val nameWithExtension = "$bookTitle.pdf"
        try {
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdir()
            val filePath = downloadsFolder.path + "/" + nameWithExtension
            val out = FileOutputStream(filePath)
            out.write(it)
            out.close()
            Toast.makeText(activity, "Downloaded", Toast.LENGTH_SHORT).show()
            incrementDownloadCount()
            binding.progress.visibility = View.INVISIBLE
        }
        catch (e: Exception){
            Log.e(TAG,e.message.toString())
            binding.progress.visibility = View.INVISIBLE
        }
    }

    private fun incrementDownloadCount() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var downloadCount = "${snapshot.child("downloadCount").value}"
                    if (downloadCount == "" || downloadCount == "null"){
                        downloadCount = "0"
                    }
                    val newDownloadCount : Long = downloadCount.toLong() + 1
                    val hashMap = HashMap<String,Any>()
                    hashMap["downloadCount"] = newDownloadCount
                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnFailureListener {
                            Log.e(TAG,"Failed to update downloadCount ${it.message}")
                        }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun showComment() {
        cmtList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    cmtList.clear()
                    for (ds in snapshot.children){
                        val model = ds.getValue(Comment::class.java)
                        cmtList.add(model!!)
                    }
                    adapterComment = AdapterComment(this@PdfDetailFragment.requireContext(),cmtList)
                    binding.rvComments.adapter = adapterComment
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private var comment = ""

    private fun loadBookDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "" + snapshot.child("categoryId").value
                    val description = "" + snapshot.child("description").value
                    val downloadCount ="" +  snapshot.child("downloadCount").value
                    val timestamp = "" + snapshot.child("timestamp").value
                    val title = "" + snapshot.child("title").value
                    val url ="" + snapshot.child("url").value
                    val viewCount = "" + snapshot.child("viewsCount").value

                    bookTitle = title
                    bookUrl = url

                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    MyApplication.loadCategory(categoryId, binding.categoryTv)
                    MyApplication.loadPdfSize(url, title, binding.sizeTv)
                    MyApplication.loadPdfFromUrlSinglePage(
                        url,
                        title,
                        binding.pdfView,
                        binding.progressBar,
                        binding.pageTv
                    )

                    binding.tvTitle.text = title
                    binding.descriptionTv.text = description
                    binding.ViewerTv.text = viewCount
                    binding.downloadTv.text = downloadCount
                    binding.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun checkIsFavorite() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object : ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    isMyFavorite = snapshot.exists()
                    if (isMyFavorite){
                        binding.btnFavorite.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.ic_baseline_favorite_24,0,0)
                        binding.btnFavorite.text = "Unfavorite"
                    }
                    else {
                        binding.btnFavorite.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.ic_baseline_favorite_border_24,0,0)
                        binding.btnFavorite.text = "Favorite"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }


}