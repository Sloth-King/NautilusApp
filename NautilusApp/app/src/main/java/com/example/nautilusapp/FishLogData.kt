package com.example.nautilusapp

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize // Lets us skip overriding all the functions
data class FishLogData(
    val location: String,
    val imageResId: Int, //TODO : will need to adapt to camera pictures but idk how yet
    val commonName: String,
    val scientificName: String,
    val description: String
) : Parcelable // This just makes iterable in a loop if we need to
