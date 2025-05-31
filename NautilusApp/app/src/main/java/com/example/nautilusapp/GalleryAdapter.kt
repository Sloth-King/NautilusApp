package com.example.nautilusapp

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class GalleryAdapter(private val images: List<Any>) :
    RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    inner class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.gallery_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_image, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val item = images[position]

        when (item) {
            is Int -> {
                // Drawable resource
                holder.imageView.setImageResource(item)
            }
            is String -> {
                // File path from camera photo
                val imgFile = File(item)
                if (imgFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    holder.imageView.setImageBitmap(bitmap)
                } else {
                    // Optional: placeholder or error image
                    holder.imageView.setImageResource(R.drawable.account)
                }
            }
        }
    }

    override fun getItemCount(): Int = images.size
}
