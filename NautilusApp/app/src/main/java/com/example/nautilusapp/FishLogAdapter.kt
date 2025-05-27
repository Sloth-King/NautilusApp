package com.example.nautilusapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FishLogAdapter(private val logList: List<FishLogData>) :
    RecyclerView.Adapter<FishLogAdapter.LogViewHolder>() {

    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val locationText: TextView = itemView.findViewById(R.id.text_location)
        val fishImage: ImageView = itemView.findViewById(R.id.image_fish)
        val commonNameText: TextView = itemView.findViewById(R.id.text_common_name)
        val scientificNameText: TextView = itemView.findViewById(R.id.text_scientific_name)
        val descriptionText: TextView = itemView.findViewById(R.id.text_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log_entry, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val item = logList[position]
        holder.locationText.text = item.location
        holder.fishImage.setImageResource(item.imageResId)
        holder.commonNameText.text = item.commonName
        holder.scientificNameText.text = item.scientificName
        holder.descriptionText.text = item.description
    }

    override fun getItemCount(): Int = logList.size
}
