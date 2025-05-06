package com.example.magiclines.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.magiclines.data.SettingDataStore

class EnergyViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = SettingDataStore(application)

    val energy: LiveData<Int> = liveData {
        preferences.energy.collect { emit(it) }
    }
}