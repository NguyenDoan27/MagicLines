package com.example.magiclines.ui.selectionLevel

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
import kotlin.math.log

class SelectLevelViewModel(private val dataStore: SettingDataStore) : BaseViewModel() {
    private val _levels = MutableLiveData<List<Level>>(emptyList())
    val levels: LiveData<List<Level>> get() = _levels

    private val _currentCategory = MutableLiveData<Int>(0)
    val currentCategory: LiveData<Int> get() = _currentCategory

    fun setCurrentCategory(categoryIndex: Int) {
        _currentCategory.value = categoryIndex
    }

    fun getDataOriginal() {
        viewModelScope.launch {
            val data = dataStore.levelsFlow.first()
            Log.e("TAG", "getDataOriginal: ${data.size}", )
            _levels.value = data
        }
    }
}