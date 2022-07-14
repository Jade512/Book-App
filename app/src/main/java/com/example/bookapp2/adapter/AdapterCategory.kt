package com.example.bookapp2.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp2.filter.FilterCategory
import com.example.bookapp2.R
import com.example.bookapp2.model.Category
import com.example.bookapp2.ui.activities.AdminActivity
import com.example.bookapp2.ui.fragments.PdfListAdminFragment
import com.google.firebase.database.FirebaseDatabase

class AdapterCategory : RecyclerView.Adapter<AdapterCategory.HolderCategory>, Filterable {

    private val context : Context
    var categoryList : ArrayList<Category>
    private var filterList : ArrayList<Category>
    private var filter : FilterCategory? = null

    constructor(context: Context, categoryList: ArrayList<Category>)  {
        this.context = context
        this.categoryList = categoryList
        this.filterList = categoryList
    }



    inner class HolderCategory(itemView: View) : RecyclerView.ViewHolder(itemView){
        var categoryTv : TextView = itemView.findViewById(R.id.categoryTv)
        var deleteIb : ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        return HolderCategory(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_rv_category,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        val model = categoryList[position]
        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        holder.categoryTv.text = category
        holder.deleteIb.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are you sure about this?")
                .setPositiveButton("Yes"){a, d ->
                    deleteCategory(model, holder)
                }
                .setNegativeButton("No"){a, d->
                    a.dismiss()
                }
                .show()
        }
        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("categoryId",id)
            bundle.putString("category",category)
            val activity = it.context as AppCompatActivity
            val pdfListAdminFragment = PdfListAdminFragment()
            pdfListAdminFragment.arguments = bundle
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view_tag_admin,pdfListAdminFragment,PdfListAdminFragment::class.java.simpleName)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun deleteCategory(model: Category, holder: HolderCategory) {
        val id = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to delete due to ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun getFilter(): Filter {
        if (filter == null){
            FilterCategory(filterList,this).also {
                filter = it
            }
        }
        return filter as FilterCategory
    }

}