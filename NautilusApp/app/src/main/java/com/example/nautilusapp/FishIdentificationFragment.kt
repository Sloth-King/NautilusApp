package com.example.nautilusapp

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.nautilusapp.DatabaseContract.Species
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

import android.Manifest
import android.database.sqlite.SQLiteConstraintException
import android.media.ExifInterface
import android.util.Log
import com.example.nautilusapp.Common.me
import com.example.nautilusapp.Common.padding
import com.example.nautilusapp.Common.port
import com.example.nautilusapp.Common.servorIpAdress
import com.example.nautilusapp.DatabaseContract.Observation
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.text.Charsets.US_ASCII
import android.content.ContentResolver
import okhttp3.OkHttpClient


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
                Log.d("Maaaarche","Maaaaaaaaaaaaaaaaaaaaaaarche")
                checkSpeciesWithWoRMS(speciesName)
            }
        }

        identifyLaterButton.setOnClickListener {
            Toast.makeText(requireContext(), "Marked for later identification.", Toast.LENGTH_SHORT).show()
            // TODO: Handle storing this data for later
        }

        imageView = view.findViewById(R.id.image_fish_photo)
        val uriString = arguments?.getString("captured_uri")
        uriString?.let {
            val imageUri = Uri.parse(it)
            imageView.setImageURI(imageUri)
        }
        Log.d("ImageURI", "uri : ${imageUri.toString()}")
    }

    private fun checkSpeciesWithWoRMS(name: String) {

        // Initialize database
        val dbHelper = DatabaseHelper(requireContext())
        val db = dbHelper.writableDatabase

        val url = "https://www.marinespecies.org/rest/AphiaRecordsByName/$name?like=false&marine_only=true"

        lifecycleScope.launch {
            Log.d("Submit Observation","Do I even get in here")
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                Log.d("Submit Observation","I am here")

                if (response.isSuccessful) {
                    val body = response.body?.string()
                    Log.d("Submit Observation","AAAAAAAAAAAAAAA")
                    if (!body.isNullOrEmpty() && body != "null") {
                        val jsonArray = JSONArray(body)
                        Log.d("Submit Observation","Whereami")
                        if (jsonArray.length() > 0) {
                            val speciesObject = jsonArray.getJSONObject(0)
                            val aphiaID = speciesObject.getInt("AphiaID")
                            val scientificName = speciesObject.getString("scientificname")
                            val classificationUrl = "https://www.marinespecies.org/rest/AphiaClassificationByAphiaID/$aphiaID"
                            val environmentUrl = "https://www.marinespecies.org/rest/AphiaRecordByAphiaID/$aphiaID"

                            // Get Necessary data for species table

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
                                    val classificationMap = extractClassificationTree(classification)
                                    classInfo.append("Kingdom: ${classificationMap["Kingdom"]}, ")
                                    classInfo.append("Phylum: ${classificationMap["Phylum"]}, ")
                                    classInfo.append("Class: ${classificationMap["Class"]}, ")
                                    classInfo.append("Order: ${classificationMap["Order"]}, ")
                                    classInfo.append("Family: ${classificationMap["Family"]}, ")
                                    classInfo.append("Genus: ${classificationMap["Genus"]}, ")
                                    classInfo.append("Species: ${classificationMap["Species"]}")
                                }
                            }

                            Log.d("KingdomLog", classInfo.toString())

                            // Get environment info
                            // !! Not always present data, so idk if that breaks anything tbh
                            var environmentInfo = ""
                            if (environmentResponse.isSuccessful) {
                                val envBody = environmentResponse.body?.string()
                                if (!envBody.isNullOrEmpty() && envBody != "null") {
                                    val env = JSONObject(envBody)
                                    Log.d("JSONEnv", env.toString())
                                    val environments = mutableListOf<String>()
                                    if (env.optInt("isMarine") == 1) environments.add("Marine")
                                    if (env.optInt("isBrackish") == 1) environments.add("Brackish")
                                    if (env.optInt("isFreshwater") == 1) environments.add("Freshwater")
                                    if (env.optInt("isTerrestrial") == 1) environments.add("Terrestrial")
                                    environmentInfo = environments.joinToString(", ")
                                }
                            }

                            var geoloc: StringBuilder? = StringBuilder()
                            try {
                                geoloc = getLatLngFromImage(requireContext(), imageUri!!)

                                if (geoloc.isNullOrEmpty()) {
                                    Log.d("Ponter", "Pointer is null")
                                    geoloc = getCurrentLocation(requireContext())
                                }
                                Log.d("Geoloc",geoloc.toString())
                            }catch (e:Exception){
                                geoloc = getCurrentLocation(requireContext())
                            }

                            thread{
                                val socket = Socket(servorIpAdress, port)
                                val writer: OutputStream = socket.getOutputStream()

                                var msg = (me).toByteArray(US_ASCII)
                                var size = msg.size.toString()
                                Log.d("socket1", size)
                                var padding = padding(size, 3 - size.length)
                                Log.d("socket1", padding.toString())
                                var finalSize: ByteArray = padding.toByteArray((US_ASCII))
                                writer.write(finalSize)
                                writer.write(msg)

                                writer.write(("1").toByteArray(US_ASCII))

                                writer.write(padding(aphiaID.toString(),8 - aphiaID.toString().length).toByteArray(US_ASCII))
                                size = padding(geoloc.toString(),8-geoloc.toString().length)
                                writer.write(size.toByteArray(US_ASCII))
                                writer.write(geoloc.toString().toByteArray(US_ASCII))

                                //get picture
                                val imageBytes = requireContext().contentResolver.openInputStream(imageUri!!)?.readBytes()

                                var sizeByte = imageBytes?.size
                                var sizeOfSize = padding(sizeByte.toString(),8-sizeByte.toString().length)
                                writer.write(sizeOfSize.toByteArray(US_ASCII))
                                writer.write(sizeByte.toString().toByteArray(US_ASCII))
                                writer.write(imageBytes)

                            }

                            val speciesValues = ContentValues().apply {
                                put(Species.Aphia_Id, aphiaID)
                                put(Species.COLUMN_NAME_COL1, scientificName)
                                put(Species.COLUMN_NAME_COL2, classInfo.toString()) // classification string
                                put(Species.COLUMN_NAME_COL3, environmentInfo)       // environment info
                            }

                            try {
                                db.insertOrThrow(Species.TABLE_NAME, null, speciesValues)
                            }catch(e: SQLiteConstraintException){
                                Log.d("SpeciesInsert", "Species already registered")
                            }

                            // For observation table

                            val observationValues = ContentValues().apply {
                                put(Observation.COLUMN_NAME_COL1, geoloc.toString()) // geoloc
                                put(Observation.COLUMN_NAME_COL2, me) // userID (no clue how to get it)
                                put(Observation.COLUMN_NAME_COL3, aphiaID)
                            }

                            errorText.visibility = View.GONE
                            Toast.makeText(requireContext(), "Valid species saved!", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d("Submit Observation","No species found")
                            errorText.text = "Species not found. Please check the name."
                            errorText.visibility = View.VISIBLE
                        }
                    }
                    else{
                        Log.d("Submit Observation","Response is null")
                        errorText.text = "Body is none"
                        errorText.visibility = View.VISIBLE
                    }

                } else {
                    Log.d("Submit observation","I don't even know what's wrong")
                    errorText.text = "Error fetching species. Try again."
                    errorText.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.d("Submit observation","Ca marche paaaaaaas")
                errorText.text = "Connection error. Please try again."
                errorText.visibility = View.VISIBLE
                e.printStackTrace()
            }
        }
    }

    fun extractClassificationTree(json: JSONObject): Map<String, String> {
        val classificationMap = mutableMapOf<String, String>()

        var currentNode: JSONObject? = json

        while (currentNode != null) {
            val rank = currentNode.optString("rank")
            val name = currentNode.optString("scientificname")

            if (rank.isNotEmpty() && name.isNotEmpty()) {
                classificationMap[rank] = name
            }

            currentNode = currentNode.optJSONObject("child")
        }

        return classificationMap
    }


    // get location from the picture
    fun getLatLngFromImage(context: Context, imageUri: Uri): StringBuilder? {
        try {
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                val latLong = FloatArray(2)
                
                if (exif.getLatLong(latLong)) {
                    val res = StringBuilder()
                    res.append("Latitude : ${latLong[0]}, ")
                    res.append("Longitude : ${latLong[1]}")
                    return res
                } else {
                    return null // No location in EXIF
                }
            }
            return null // Input stream was null

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // get current location from our phone
    fun getCurrentLocation(context: Context): StringBuilder? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission first
            return null
        }

        // Note: This is a synchronous implementation for a primarily asynchronous API
        // In a real app, consider using callbacks or coroutines instead
        try {
            val locationResult = fusedLocationClient.lastLocation.result
            if (locationResult != null) {
                return StringBuilder().apply {
                    append("Latitude: ${locationResult.latitude}, ")
                    append("Longitude: ${locationResult.longitude}")
                }
            }
        } catch (e: Exception) {
            Log.e("Location", "Error getting location", e)
        }
        
        return null
    }
}

