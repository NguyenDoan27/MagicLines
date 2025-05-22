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
        Audio("Pure love", R.raw.pure_love),
        Audio("Piano", R.raw.josefpres__piano),
        Audio("Under_sky", R.raw.under_the_sky),
        Audio("happy_chill", R.raw.happy_chill),
        Audio("beat_loop", R.raw.beat_loop_in_a_maj)
    )

    private var levels = arrayListOf<Level>(
        Level(1, R.string.orange_juice, R.drawable.test_600, false,"Object"),
        Level(2, R.string.orange_juice, R.drawable.frame_6, false,"Action"),
        Level(3, R.string.orange_juice, R.drawable.frame_7, false,"Symbol"),
        Level(4, R.string.orange_juice, R.drawable.frame_8, false,"Animal"),
        Level(5, R.string.orange_juice, R.drawable.frame_10, false,""),
        Level(6, R.string.orange_juice, R.drawable.vit_test, false,""),
        Level(7, R.string.orange_juice, R.drawable.test_700, false,""),
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
            preferences[PreferenceKey.IS_SOUND] ?: true
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

    val isFirst: Flow<Boolean> = context.dataStore.data.map {
        preferences -> preferences[PreferenceKey.IS_FIRST] ?: true
    }

    suspend fun setIsFirst(isFirst: Boolean){
        context.dataStore.edit { preferences ->
            preferences[PreferenceKey.IS_FIRST] = isFirst
        }
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
    val IS_FIRST = booleanPreferencesKey("is_first")
}