package com.example.magiclines.models

import java.io.Serializable

class Level: Serializable {
    private var numLevel: Int? = null
    private var resourceId: Int? = null
    private var isComplete: Boolean? = null

    constructor(numLevel: Int, resourceId: Int, isComplete: Boolean){
        this.numLevel = numLevel
        this.resourceId = resourceId
        this.isComplete = isComplete
    }

    fun getNumLevel(): Int? {
        return numLevel
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
}