package com.example.nautilusapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class FishDetailsFragment : Fragment() {

    companion object {
        const val ARG_FISH = "fish_arg"

        fun newInstance(fish: FishLogData): FishDetailsFragment {
            val fragment = FishDetailsFragment()
            val args = Bundle()
            args.putParcelable(ARG_FISH, fish) // Make sure FishLogData is Parcelable
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var fish: FishLogData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            fish = it.getParcelable(ARG_FISH)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_fish_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Fill UI with fish data
        view.findViewById<TextView>(R.id.fish_scientific_name).text = fish.scientificName
        view.findViewById<ImageView>(R.id.fish_image).setImageBitmap(fish.imageResId)

        view.findViewById<ImageView>(R.id.button_close).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        view.findViewById<Button>(R.id.button_add_note).setOnClickListener {
            // navigate to Add Note Fragment (you'll define later)
        }
    }
}
