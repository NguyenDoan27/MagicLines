package com.example.magiclines.ui.splash

import androidx.datastore.dataStore
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.magiclines.base.BaseViewModel
import com.example.magiclines.data.SettingDataStore
import kotlinx.coroutines.launch

class SplashViewModel(private val dataStore: SettingDataStore): BaseViewModel() {
    fun initData(){
        viewModelScope.launch {
            dataStore.initData()
        }
    }
}