package com.example.firebasestorage

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class imageAdapter(private var uri: List<String>): RecyclerView.Adapter<imageAdapter.imageViewHolder>() {

    inner class imageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        var imageViewHolder: ImageView = itemView.findViewById(R.id.imageViewHolder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): imageViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.image_holder,parent,false)
        return imageViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return uri.size
    }

    override fun onBindViewHolder(holder: imageViewHolder, position: Int) {
        holder.apply {
            Glide.with(itemView).load(uri[position]).into(imageViewHolder)
            imageViewHolder.setOnLongClickListener {
                Toast.makeText(itemView.context,"You have clicked on image ${position}",Toast.LENGTH_SHORT).show()
                val alertDialog = AlertDialog.Builder(itemView.context)
                    .setTitle("Delete Image")
                    .setMessage("Are you sure want to delete the message")
                    .setIcon(R.drawable.delete_icon)
                    .setPositiveButton("Yes,Delete"){ _,_->
                        //Delete a particular file
                        Firebase.storage.reference.child("images/image${position}").delete().addOnSuccessListener {
                            Toast.makeText(itemView.context,"Deleted Successfully",Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener{
                            Toast.makeText(itemView.context,"Failed to delete",Toast.LENGTH_SHORT).show()
                        }

                    }.setNegativeButton("Cancel"){_,_->
                        Toast.makeText(itemView.context,"Cancelled",Toast.LENGTH_SHORT).show()
                    }
                alertDialog.show()
                true
            }
        }

    }


}