package com.example.nautilusapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class LogFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_log, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.log_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
         val noObsText = view.findViewById<TextView>(R.id.text_no_obs)
        val data = getFishData()
        if(data.isNullOrEmpty()){
            noObsText.visibility = View.VISIBLE
        }else {
            noObsText.visibility = View.GONE
            recyclerView.adapter = FishLogAdapter(data)
        }

        // Setup search icon click
        val searchIcon = view.findViewById<ImageView>(R.id.icon_search)
        searchIcon.setOnClickListener {
            Toast.makeText(requireContext(), "Search clicked!", Toast.LENGTH_SHORT).show()
        }
    }

    fun getFishData(): List<FishLogData> {
        val dbHelper = DatabaseHelper(requireContext())
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT scientific_name, geolocalisation, picture FROM Observation AS o " +
                    "JOIN Species AS s ON s.aphia_id = o.aphiaId " +
                    "JOIN is_in_observation AS i ON i.idObs = o._id " +
                    "JOIN Picture AS p ON p._id = i.idPicture",
            null
        )

        val res = mutableListOf<FishLogData>()

        while (cursor.moveToNext()) {
            val scientificName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Species.COLUMN_NAME_COL1))
            val localization = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Observation.COLUMN_NAME_COL1))
            val imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Picture.COLUMN_NAME_COL1))

            Log.d("GetFishData", "Scientific name: $scientificName")
            Log.d("GetFishData", "Localization: $localization")
            Log.d("GetFishData", "Image path: $imagePath")

            val picBitmap = if (!imagePath.isNullOrEmpty() && File(imagePath).exists()) {
                BitmapFactory.decodeFile(imagePath)
            } else {
                Log.w("GetFishData", "Image file not found at path: $imagePath")
                Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888) // fallback
            }

            try {
                res.add(FishLogData(localization, picBitmap, scientificName))
            } catch (e: NullPointerException) {
                Log.e("GetFishData", "Error: one or more values are null", e)
            }
        }

        cursor.close()
        return res
    }


}
