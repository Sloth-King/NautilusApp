package com.example.nautilusapp

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nautilusapp.DatabaseContract.Picture
import com.example.nautilusapp.DatabaseContract.Simplified_User
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class IdentificationFragment : Fragment() {

    private lateinit var cameraButton: ImageButton
    private lateinit var galleryRecyclerView: RecyclerView
    private lateinit var galleryAdapter: GalleryAdapter

    private val imagePaths = mutableListOf<Any>() // Can be Int (drawable) or String (file path)
    private var currentPhotoPath: String? = null

    // Allows us to launch the camera intent (see below)
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the camera result launcher
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && currentPhotoPath != null) {
                imagePaths.add(0, currentPhotoPath!!)
                galleryAdapter.notifyItemInserted(0)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_identification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraButton = view.findViewById(R.id.button_open_camera)
        galleryRecyclerView = view.findViewById(R.id.gallery_recycler_view)

        cameraButton.setOnClickListener {
            launchCamera()
        }

        // Dummy images
        imagePaths.addAll(List(6) { R.drawable.test_fis })

        galleryAdapter = GalleryAdapter(imagePaths)
        galleryRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        galleryRecyclerView.adapter = galleryAdapter
    }

    private fun launchCamera() {
        // database
        val dbHelper = DatabaseHelper(requireContext())
        val db = dbHelper.writableDatabase

        val photoFile = createImageFile()
        val photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        currentPhotoPath = photoFile.absolutePath
        Toast.makeText(requireContext(), currentPhotoPath.toString(), Toast.LENGTH_LONG).show()
        takePictureLauncher.launch(photoUri)

        val imageByte = File(currentPhotoPath!!).readBytes() // !! for x? type

        val values = ContentValues().apply {
            put(Simplified_User.COLUMN_NAME_COL1, imageByte)
        }

        db.insert(Picture.TABLE_NAME, null, values)

        // Show the image in the next fragment
        val fragment = FishIdentificationFragment()
        fragment.setCapturedImage(photoUri)

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

    }

    private fun createImageFile(): File {
        // use the time to create unique name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        val storageDir: File = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!! // !! to assert not null
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir) // return the file
    }

}
