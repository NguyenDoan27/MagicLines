package com.example.magiclines.ui.collection

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.magiclines.base.BaseViewModel
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.models.Level
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CollectionViewModel(private val dataStore: SettingDataStore): BaseViewModel() {
    private val _filteredLevels = MutableLiveData<List<Level>>(emptyList())
    val filteredLevels: LiveData<List<Level>> get() = _filteredLevels

    private val _currentCategory = MutableLiveData<Int>(0)
    val currentCategory: LiveData<Int> get() = _currentCategory


    fun setCurrentCategory(categoryIndex: Int) {
        _currentCategory.value = categoryIndex
    }

    fun getDataFiltered() {
        viewModelScope.launch {
            val data = dataStore.levelsFlow.first()
            val completedLevels = data.filter { it.isComplete }
            Log.e("TAG", "setFilteredLevels: ${completedLevels.size}", )
            _filteredLevels.value = completedLevels

        }
    }
}