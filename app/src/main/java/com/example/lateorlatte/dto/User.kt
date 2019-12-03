package com.example.lateorlatte.dto

import com.google.firebase.firestore.GeoPoint

data class User(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    var location: GeoPoint? = null
) : Model()