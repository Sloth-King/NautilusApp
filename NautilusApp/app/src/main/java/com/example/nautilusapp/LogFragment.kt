package com.example.nautilusapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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
        //recyclerView.adapter = FishLogAdapter(fakedata()) //TODO : make fake data one day (or real data idc)

        // Setup search icon click
        val searchIcon = view.findViewById<ImageView>(R.id.icon_search)
        searchIcon.setOnClickListener {
            Toast.makeText(requireContext(), "Search clicked!", Toast.LENGTH_SHORT).show()
        }
    }

}
