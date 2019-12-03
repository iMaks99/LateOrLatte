package com.example.lateorlatte

import android.location.Address

class CafeItem {

    private var imageUrl: String
    private var titleCafe: String
    private var addressCafe: String
    private var cX: Double
    private var cY: Double


    constructor(title: String, address: String, url: String, x: Double, y: Double) {
        titleCafe = title
        addressCafe = address
        imageUrl = url
        cX = x
        cY = y
    }

    fun getImage(): String {
        return imageUrl
    }

    fun getTitle(): String{
        return titleCafe
    }

    fun getAddress(): String{
        return addressCafe
    }

    fun getX(): Double{
        return cX
    }

    fun getY(): Double{
        return cY
    }
}
