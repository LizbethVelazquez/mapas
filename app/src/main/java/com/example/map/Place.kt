package com.example.map

import com.google.gson.annotations.SerializedName

data class Place(
     @SerializedName("nombre") val name: String,
     @SerializedName("latitud") val lat: Double,
     @SerializedName("longitud") val lng: Double,
)
