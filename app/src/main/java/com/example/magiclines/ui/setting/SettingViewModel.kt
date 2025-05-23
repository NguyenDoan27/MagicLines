package com.example.magiclines.ui.setting

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.magiclines.base.BaseViewModel
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.models.Audio
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class SettingViewModel(private val dataStore: SettingDataStore): BaseViewModel() {
    private val _isSoundEnabled = MutableLiveData<Boolean>()
    val isSoundEnabled: LiveData<Boolean> get() = _isSoundEnabled

    private val _musics = MutableLiveData<List<Audio>>()
    val musics: LiveData<List<Audio>> get() = _musics

    private val _currentMusicPosition = MutableLiveData<Int>()
    val currentMusicPosition: LiveData<Int> get() = _currentMusicPosition

    private val _currentLanguageCode = MutableLiveData<String>()
    val currentLanguageCode: LiveData<String> get() = _currentLanguageCode

    fun setMusicPosition(position: Int) {
        _currentMusicPosition.value = position
        viewModelScope.launch { dataStore.saveMusicPosition(position) }
    }

    fun setIsSoundEnabled(isSoundEnabled: Boolean) {
        _isSoundEnabled.value = isSoundEnabled
        viewModelScope.launch { dataStore.saveStateSound(isSoundEnabled) }
    }

    fun setLanguage(language: String) {
        _currentLanguageCode.value = language
        viewModelScope.launch { dataStore.saveLanguage(language) }
    }
    fun getData(){
        viewModelScope.launch {
            combine(
                dataStore.musics,
                dataStore.readMusicPosition(),
                dataStore.readStateSound(),
                dataStore.language
            ) { musics, position, state, language ->
                Quad(musics, position, state, language)
            }.distinctUntilChanged()
                .collect { (musics, pos, state, language) ->
                    _musics.value = musics
                    _currentMusicPosition.value = pos
                    _isSoundEnabled.value = state
                    _currentLanguageCode.value = language
                }
        }
    }

    private data class Quad<out A, out B, out C, out D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}