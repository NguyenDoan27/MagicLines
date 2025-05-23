package com.example.magiclines.ui.playing

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.magiclines.base.BaseViewModel
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.models.Audio
import com.example.magiclines.models.Level
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PlayingViewModel(private val dataStore: SettingDataStore): BaseViewModel() {
    private val _isFist = MutableLiveData<Boolean>()
    val firstTimes: LiveData<Boolean> get() = _isFist

    private val _levels = MutableLiveData<List<Level>>()
    val levels: LiveData<List<Level>> get() = _levels

    private val _musicPos = MutableLiveData<Int>()
    val musicPos: LiveData<Int> get() = _musicPos

    private val _bgPos = MutableLiveData<Int>()
    val bgPos: LiveData<Int> get() = _bgPos

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    fun setIsFirst(isFirst: Boolean){
        _isFist.value = isFirst
        viewModelScope.launch {
            dataStore.setIsFirst(isFirst)
        }
    }

    fun setBackgroundPosition(pos: Int){
        _bgPos.value = pos
        viewModelScope.launch {
            dataStore.saveBackgroundPosition(pos)
        }
    }
    private val _music = MutableLiveData<List<Audio>>()
    val music: LiveData<List<Audio>> get() = _music

    fun setMusicPosition(pos: Int){
        _musicPos.value = pos
        viewModelScope.launch {
            dataStore.saveMusicPosition(pos)
        }
    }

    fun setIsPlaying(isPlaying: Boolean){
        _isPlaying.value = isPlaying
        viewModelScope.launch {
            dataStore.saveStateSound(isPlaying)
        }
    }

    fun setLevels(originLevels: List<Level>, playingLevels: List<Level>, pos: Int, star: Int) {
        viewModelScope.launch {
            dataStore.apply {
                for (i in originLevels) {
                    if (i.resourceId == playingLevels[pos].resourceId){
                        i.isComplete = true
                        i.star = star
                        break
                    }
                }
                saveLevel(originLevels)
            }
        }
    }

    fun getInitData(){
        viewModelScope.launch{
            combine(
                dataStore.isFirst,
                dataStore.levelsFlow,
                dataStore.readMusicPosition(),
                dataStore.readBackgroundPosition(),
                dataStore.readStateSound(),
            ) { isFirst, levels, musicPos, bgPos, isPlaying ->
                Pen(isFirst, levels, musicPos, bgPos, isPlaying)
            }.collect { (isFist, levels, musicPos, bgPos, isPlaying) ->
                _isFist.value = isFist
                _levels.value = levels
                _musicPos.value = musicPos
                _bgPos.value = bgPos
                _isPlaying.value = isPlaying
            }
        }
    }

    fun getMusics(){
        viewModelScope.launch {
            val musics = dataStore.musics.first()
            _music.value = musics
        }
    }


    private data class Pen<out A, out B, out C, out D, out E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )

}