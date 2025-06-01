package com.example.nautilusapp

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize // Lets us skip overriding all the functions
data class FishLogData(
    val location: String,
    val imageResId: Bitmap,
    val scientificName: String,
) : Parcelable // This just makes iterable in a loop if we need to
