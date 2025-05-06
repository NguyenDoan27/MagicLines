package com.example.magiclines.models

class Color {
    private var name: String? = null
    private var codeColor: String? = null

    constructor(name: String, codeColor: String){
        this.name = name
        this.codeColor = codeColor
    }
    fun getName(): String? {
        return name
    }

    fun getCodeColor(): String? {
        return codeColor
    }
}