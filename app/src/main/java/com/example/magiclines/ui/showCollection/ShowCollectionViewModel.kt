package com.example.magiclines.ui.showCollection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.magiclines.base.BaseViewModel
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.models.Level
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ShowCollectionViewModel(private val dataStore: SettingDataStore): BaseViewModel() {
    private val _levels = MutableLiveData<List<Level>>(emptyList())
    val levels: LiveData<List<Level>> get() = _levels

    fun getData(){
        viewModelScope.launch{
            _levels.value = dataStore.levelsFlow.first()
        }
    }

    fun saveLevel(level: List<Level>){
        viewModelScope.launch{
            dataStore.saveLevel(level)
        }
    }
}