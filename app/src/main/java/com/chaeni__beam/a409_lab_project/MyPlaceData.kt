package com.chaeni__beam.a409_lab_project

import android.os.Parcelable
import android.widget.ImageView
import kotlinx.android.parcel.Parcelize
import ted.gun0912.clustering.clustering.TedClusterItem
import ted.gun0912.clustering.geometry.TedLatLng
import java.io.Serializable

@Parcelize
data class MyPlaceData(
    val locationName : String = "null",
    val category: String = "null",
    val categoryPos : Int = 0,
    val address : String = "null",
    val latitude : String = "null",
    val longitude : String = "null",
    val memo : String = "null",
    val dateCreated : String = "null",
    val markerColor : String = "null",
    val markerColorTag : String = "null",
    val imageNum : Int = 0,
    val image1 : String = "null",
    val image2 : String = "null",
    val image3 : String = "null",
    val image4 : String = "null",
    val image5 : String = "null"
) : Parcelable
