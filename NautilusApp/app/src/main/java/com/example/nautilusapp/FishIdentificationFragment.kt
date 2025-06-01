package com.example.nautilusapp

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class FishIdentificationFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var editTextSpecies: EditText
    private lateinit var errorText: TextView
    private lateinit var submitButton: Button
    private lateinit var identifyLaterButton: Button

    private var imageUri: Uri? = null // Set this externally after photo capture

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_fish_identification, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imageView = view.findViewById(R.id.image_fish_photo)
        editTextSpecies = view.findViewById(R.id.edittext_species_name)
        errorText = view.findViewById(R.id.text_species_error)
        submitButton = view.findViewById(R.id.button_submit_species)
        identifyLaterButton = view.findViewById(R.id.button_identify_later)

        imageUri?.let { imageView.setImageURI(it) }

        submitButton.setOnClickListener {
            val speciesName = editTextSpecies.text.toString().trim()
            if (speciesName.isNotEmpty()) {
                checkSpeciesWithWoRMS(speciesName)
            }
        }

        identifyLaterButton.setOnClickListener {
            Toast.makeText(requireContext(), "Marked for later identification.", Toast.LENGTH_SHORT).show()
            // TODO: Handle storing this data for later
        }
    }

    fun setCapturedImage(uri: Uri) {
        imageUri = uri
    }

    private fun checkSpeciesWithWoRMS(name: String) {

        // Initialize database TODO : figure out where to actually put this
        val dbHelper = DatabaseHelper(requireContext())
        val db = dbHelper.writableDatabase

        val url = "https://www.marinespecies.org/rest/AphiaRecordsByName/$name?like=false&marine_only=true"

        lifecycleScope.launch {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }

                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrEmpty() && body != "null") {
                        errorText.visibility = View.GONE
                        Toast.makeText(requireContext(), "Valid species! Data fetched.", Toast.LENGTH_SHORT).show()
                        // TODO: Parse and display species info
                    } else {
                        errorText.text = "Species not found. Please check the name."
                        errorText.visibility = View.VISIBLE
                    }
                } else {
                    errorText.text = "Error fetching species. Try again."
                    errorText.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                errorText.text = "Connection error. Please try again."
                errorText.visibility = View.VISIBLE
            }
        }
    }
}

