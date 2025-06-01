package com.example.nautilusapp

import android.content.ContentValues
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
import com.example.nautilusapp.DatabaseContract.Species
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

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
                        val jsonArray = JSONArray(body)
                        if (jsonArray.length() > 0) {
                            val speciesObject = jsonArray.getJSONObject(0)
                            val aphiaID = speciesObject.getInt("AphiaID")
                            val scientificName = speciesObject.getString("scientificname")
                            val classificationUrl = "https://www.marinespecies.org/rest/AphiaClassificationByAphiaID/$aphiaID"
                            val environmentUrl = "https://www.marinespecies.org/rest/AphiaRecordByAphiaID/$aphiaID"

                            val classificationResponse = withContext(Dispatchers.IO) {
                                client.newCall(Request.Builder().url(classificationUrl).build()).execute()
                            }

                            val environmentResponse = withContext(Dispatchers.IO) {
                                client.newCall(Request.Builder().url(environmentUrl).build()).execute()
                            }

                            // Get class info
                            // I'm not a biologist so i just took everything ngl
                            val classInfo = StringBuilder()
                            if (classificationResponse.isSuccessful) {
                                val classBody = classificationResponse.body?.string()
                                if (!classBody.isNullOrEmpty() && classBody != "null") {
                                    val classification = JSONObject(classBody)
                                    classInfo.append("Kingdom: ${classification.optJSONObject("kingdom")?.optString("scientificname")}, ")
                                    classInfo.append("Phylum: ${classification.optJSONObject("phylum")?.optString("scientificname")}, ")
                                    classInfo.append("Class: ${classification.optJSONObject("class")?.optString("scientificname")}, ")
                                    classInfo.append("Order: ${classification.optJSONObject("order")?.optString("scientificname")}, ")
                                    classInfo.append("Family: ${classification.optJSONObject("family")?.optString("scientificname")}, ")
                                    classInfo.append("Genus: ${classification.optJSONObject("genus")?.optString("scientificname")}")
                                }
                            }

                            // Get environment info
                            // !! Not always present data, so idk if that breaks anything tbh
                            var environmentInfo = ""
                            if (environmentResponse.isSuccessful) {
                                val envBody = environmentResponse.body?.string()
                                if (!envBody.isNullOrEmpty() && envBody != "null") {
                                    val env = JSONObject(envBody)
                                    val environments = mutableListOf<String>()
                                    if (env.optBoolean("isMarine")) environments.add("Marine")
                                    if (env.optBoolean("isBrackish")) environments.add("Brackish")
                                    if (env.optBoolean("isFreshwater")) environments.add("Freshwater")
                                    if (env.optBoolean("isTerrestrial")) environments.add("Terrestrial")
                                    environmentInfo = environments.joinToString(", ")
                                }
                            }

                            val speciesValues = ContentValues().apply {
                                put(Species.Aphia_Id, aphiaID)
                                put(Species.COLUMN_NAME_COL1, scientificName)
                                put(Species.COLUMN_NAME_COL2, classInfo.toString()) // classification string
                                put(Species.COLUMN_NAME_COL3, environmentInfo)       // environment info
                            }

                            db.insert(Species.TABLE_NAME, null, speciesValues)

                            errorText.visibility = View.GONE
                            Toast.makeText(requireContext(), "Valid species saved!", Toast.LENGTH_SHORT).show()
                        } else {
                            errorText.text = "Species not found. Please check the name."
                            errorText.visibility = View.VISIBLE
                        }
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

