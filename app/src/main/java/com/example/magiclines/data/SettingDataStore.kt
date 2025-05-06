package com.example.magiclines.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.magiclines.R
import com.example.magiclines.models.Audio
import com.example.magiclines.models.Level
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Date

private const val DEVICE_CONFIG = "config"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DEVICE_CONFIG)

class SettingDataStore(private val context: Context) {
    private val gson = Gson()
    private val sounds = arrayListOf<Audio>(
        Audio("Memories", R.raw.our_memories),
        Audio("Pure love", R.raw.pure_love)
    )

    private var levels = arrayListOf<Level>(
        Level(1, R.drawable.heart, false),
        Level(2, R.drawable.circle_bolt, false),
        Level(3, R.drawable.guitar, false),
        Level(4, R.drawable.donus, false)
    )

    suspend fun initData(){
        context.dataStore.edit{ preferences ->
            preferences[PreferenceKey.SOUNDS] = gson.toJson(sounds)
        }

        context.dataStore.edit { preferences ->
            if (!preferences.contains(PreferenceKey.PROGRESS_KEY)) {
                preferences[PreferenceKey.PROGRESS_KEY] = gson.toJson(levels)
            }else{
                val data = levelsFlow.first()
                if (data.size < levels.size ){
                    preferences[PreferenceKey.PROGRESS_KEY] = gson.toJson(levels)
                }
            }
        }
    }

    val musics: Flow<List<Audio>> =  context.dataStore.data.map{ preferences ->
        val jsonString = preferences[PreferenceKey.SOUNDS] ?: return@map emptyList()
        val type = object : TypeToken<List<Audio>>() {}.type
        gson.fromJson(jsonString, type) ?: emptyList()
    }

    suspend fun saveMusicPosition(position: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKey.DEFAULT_MUSIC_POSITION] = position
        }
    }

     fun readMusicPosition(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferenceKey.DEFAULT_MUSIC_POSITION] ?: 0
        }
    }
    suspend fun saveBackgroundPosition(position: Int){
        context.dataStore.edit { preferences ->
            preferences[PreferenceKey.DEFAULT_BACKGROUND_POSITION] = position
        }
    }

     fun readBackgroundPosition(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferenceKey.DEFAULT_BACKGROUND_POSITION] ?: 0
        }
    }

    suspend fun saveStateSound(isSound: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKey.IS_SOUND] = isSound
        }
    }

     fun readStateSound(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferenceKey.IS_SOUND] != false
        }
    }
    suspend fun saveLevel(levels: List<Level>){
        context.dataStore.edit{ preferences ->
            preferences[PreferenceKey.PROGRESS_KEY] = gson.toJson(levels)
        }
    }

    val levelsFlow: Flow<List<Level>> = context.dataStore.data.map{ preferences ->
        val jsonString = preferences[PreferenceKey.PROGRESS_KEY] ?: return@map emptyList()
        val type = object : TypeToken<List<Level>>() {}.type
        gson.fromJson(jsonString, type) ?: emptyList()
    }

     suspend fun saveEnergy(energy: Int){
        context.dataStore.edit { preferences ->
            preferences[PreferenceKey.ENERGY] = energy
        }
    }

    val energy: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKey.ENERGY] ?: 5
    }

    suspend fun saveDate(date: Date) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKey.DATE_GET_ENERGY] = date.time
        }
    }

    val date: Flow<Date?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferenceKey.DATE_GET_ENERGY]?.let { Date(it) }
        }


    suspend fun saveLanguage(language: String){
        context.dataStore.edit { preferences ->
            preferences[PreferenceKey.LANGUAGE] = language
        }
    }

    val language: Flow<String> = context.dataStore.data.map {
        preferences -> preferences[PreferenceKey.LANGUAGE] ?: "vi"
    }
}

private object PreferenceKey {
    val IS_SOUND = booleanPreferencesKey("is_sound")
    val DEFAULT_BACKGROUND_POSITION = intPreferencesKey("background_position")
    val DEFAULT_MUSIC_POSITION = intPreferencesKey("music_position")
    val PROGRESS_KEY = stringPreferencesKey("level_key")
    val ENERGY = intPreferencesKey("energy")
    val DATE_GET_ENERGY = longPreferencesKey("date_get_energy")
    val LANGUAGE = stringPreferencesKey("language")
    val SOUNDS = stringPreferencesKey("sounds")
}