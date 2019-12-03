package com.example.lateorlatte.dto

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import java.util.*

data class Meeting(
    var address: String? = null,
    var location: GeoPoint? = null,
    var creatorId: String? = null,
    var creator: String? = null,
    var date: Date? = null,
    var time: Date? = null,
    var participant: ArrayList<String>? = null
) : Parcelable {

    @SuppressLint("SimpleDateFormat")
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        GeoPoint(parcel.readDouble(), parcel.readDouble()),
        parcel.readString(),
        parcel.readString(),
        parcel.readSerializable() as Date?,
        parcel.readSerializable() as Date?,
        parcel.createStringArrayList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(address)
        parcel.writeDouble(location!!.latitude)
        parcel.writeDouble(location!!.longitude)
        parcel.writeString(creatorId)
        parcel.writeString(creator)
        parcel.writeSerializable(date)
        parcel.writeSerializable(time)
        parcel.writeStringList(participant!!)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Meeting> {
        override fun createFromParcel(parcel: Parcel): Meeting {
            return Meeting(parcel)
        }

        override fun newArray(size: Int): Array<Meeting?> {
            return arrayOfNulls(size)
        }
    }

}