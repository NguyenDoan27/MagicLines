package com.example.magiclines.models

class Audio {
    private  var name : String? = null
    private  var rawResourceId: Int? = null

    constructor(name: String, rawResourceId: Int){
        this.name = name
        this.rawResourceId = rawResourceId
    }

     fun getName(): String? {
        return name
    }

    fun getRawResourceId(): Int? {
        return rawResourceId
    }
}