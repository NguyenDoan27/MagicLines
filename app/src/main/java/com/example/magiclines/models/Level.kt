package com.example.magiclines.models

import java.io.Serializable

class Level: Serializable {
    private var numLevel: Int? = null
    private var resourceName: Int? = null
    private var resourceId: Int? = null
    private var isComplete: Boolean? = null
    private var star: Int? = null

    constructor(numLevel: Int,resourceName: Int, resourceId: Int, isComplete: Boolean){
        this.numLevel = numLevel
        this.resourceName = resourceName
        this.resourceId = resourceId
        this.isComplete = isComplete
    }

    fun getNumLevel(): Int? {
        return numLevel
    }

    fun getResourceName(): Int? {
        return resourceName
    }

    fun getResourceId(): Int? {
        return resourceId
    }

    fun getIsComplete(): Boolean? {
        return isComplete
    }
    fun setIsComplete(state: Boolean?){
        this.isComplete = state
    }
    fun setStar(star: Int?){
        this.star = star
    }

    fun getStar(): Int? {
        return star
    }
}