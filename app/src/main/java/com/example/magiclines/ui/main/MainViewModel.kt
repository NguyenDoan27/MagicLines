package com.example.magiclines.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.magiclines.base.BaseViewModel
import com.example.magiclines.data.SettingDataStore
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(private val dataStore: SettingDataStore): BaseViewModel() {
    private val _language = MutableLiveData<String>("vi")
    val language : LiveData<String> get() = _language

    private val _isSound = MutableLiveData<Boolean>(false)
    val isSound : LiveData<Boolean> get() = _isSound

    fun setLanguage(language: String) {
        _language.value = language
        viewModelScope.launch {
            dataStore.saveLanguage(language)
        }
    }

    fun getData(){
        viewModelScope.launch {
            combine(
                dataStore.language,
                dataStore.readStateSound()
            ) {language, state -> Pair(language, state) }
                .distinctUntilChanged()
                .collect { (language, state) ->
                _language.value = language
                _isSound.value = state
            }
        }
    }
}