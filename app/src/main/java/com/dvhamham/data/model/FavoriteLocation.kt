package com.dvhamham.data.model

data class FavoriteLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String = "",
    val category: String = "General"
)