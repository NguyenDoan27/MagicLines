package com.example.magiclines.ui.collection

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.magiclines.base.BaseViewModel
import com.example.magiclines.common.adapter.LevelPlayerAdapter2
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.models.Level
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CollectionViewModel(private val dataStore: SettingDataStore): BaseViewModel() {
    private val _filteredLevels = MutableLiveData<List<Level>>(emptyList())
    val filteredLevels: LiveData<List<Level>> get() = _filteredLevels

    private val _currentCategory = MutableLiveData<Int>(0)
    val currentCategory: LiveData<Int> get() = _currentCategory

    fun setFilteredLevels(levels: List<Level>) {

        _filteredLevels.value = levels
        Log.e("TAG", "setFilteredLevels: ${filteredLevels.value?.size}", )
    }

    fun setCurrentCategory(categoryIndex: Int) {
        _currentCategory.value = categoryIndex
    }

    fun getDataFiltered(levelAdapter: LevelPlayerAdapter2, position: Int, category: String) {
        viewModelScope.launch {
            if (position == 0) {
                val data = dataStore.levelsFlow.first()
                val completedLevels = data.filter { it.isComplete }
                _currentCategory.value = 0
                _filteredLevels.value = completedLevels
                levelAdapter.setItems(completedLevels)
            } else {
                val data = dataStore.levelsFlow.first()
                val completedLevels = data.filter { it.isComplete }
                _filteredLevels.value = completedLevels
                levelAdapter.setItems(completedLevels)
                levelAdapter.filter?.filter(category)
            }
        }
    }
}