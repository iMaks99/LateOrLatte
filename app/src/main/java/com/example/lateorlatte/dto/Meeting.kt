package com.example.lateorlatte.dto

import android.location.Location
import java.util.*
import kotlin.collections.ArrayList

data class Meeting(
    val locationName: String? = null,
    val locationCoor: Location? = null,
    val creator: String? = null,
    val date: Date? = null,
    val participant: ArrayList<User>? = null
)