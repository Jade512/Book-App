package com.example.bookapp2.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookapp2.MyApplication
import com.example.bookapp2.R
import com.example.bookapp2.model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class AdapterComment(
    val c : Context,
    private val List : ArrayList<Comment>
) : RecyclerView.Adapter<AdapterComment.HolderComment>() {

    private val mAuth = FirebaseAuth.getInstance()

    inner class HolderComment(itemView : View) : RecyclerView.ViewHolder(itemView){
        val avatar : CircleImageView = itemView.findViewById(R.id.profileIvCmt)
        val name : TextView = itemView.findViewById(R.id.nameTvCmt)
        val cmt : TextView = itemView.findViewById(R.id.comment)
        val date : TextView = itemView.findViewById(R.id.dateTvCmt)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        return HolderComment(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_rv_comment,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        val model = List[position]
        val id = model.id
        val bookId = model.bookId
        val comment = model.comment
        val timestamp = model.timestamp
        val uid = model.uid

        val date = MyApplication.formatTimeStamp(timestamp.toLong())
        holder.date.text = date
        holder.cmt.text = comment

        loadUserData(model,holder)

        holder.itemView.setOnClickListener {
            if (mAuth.currentUser != null && mAuth.uid == uid){
                deleteCmt(model, holder)
            }
        }
    }

    private fun deleteCmt(model: Comment, holder: HolderComment) {
        val builder = AlertDialog.Builder(c)
            builder.setTitle("Delete Comment")
                .setMessage("Are you sure about that")
                .setPositiveButton("DELETE") {d, e ->
                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(model.bookId).child("Comments").child(model.id)
                        .removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(c, "Deleted Comment", Toast.LENGTH_SHORT).show()
                        }
                }

                .setNegativeButton("CANCEL") {d, e ->
                    d.dismiss()
                }
                .show()
    }

    private fun loadUserData(model: Comment, holder: HolderComment) {
            val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").value
                    val proImage = snapshot.child("profileImage").value
                    holder.name.text = name.toString()
                    Glide.with(c).load(proImage).placeholder(R.drawable.ic_baseline_person_24)
                        .into(holder.avatar)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun getItemCount(): Int {
        return List.size
    }
}