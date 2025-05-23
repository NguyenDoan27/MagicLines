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
        Level(1, R.string.orange_juice, R.drawable.anime_and_cartoon_03, false, "Anime"),
        Level(2, R.string.orange_juice, R.drawable.animal_10, false, "Animal"),
        Level(3, R.string.orange_juice, R.drawable.cute_kawaii_07, false, "Kawaii"),
        Level(4, R.string.orange_juice, R.drawable.anime_and_cartoon_01, false, "Anime"),
        Level(5, R.string.orange_juice, R.drawable.animal_02, false, "Animal"),
        Level(6, R.string.orange_juice, R.drawable.anime_and_cartoon_06, false, "Anime"),
        Level(7, R.string.orange_juice, R.drawable.cute_kawaii_05, false, "Kawaii"),
        Level(8, R.string.orange_juice, R.drawable.animal_03, false, "Animal"),
        Level(9, R.string.orange_juice, R.drawable.anime_and_cartoon_05, false, "Anime"),
        Level(10, R.string.orange_juice, R.drawable.cute_kawaii_10, false, "Kawaii"),
        Level(11, R.string.orange_juice, R.drawable.anime_and_cartoon_02, false, "Anime"),
        Level(12, R.string.orange_juice, R.drawable.animal_01, false, "Animal"),
        Level(13, R.string.orange_juice, R.drawable.cute_kawaii_01, false, "Kawaii"),
        Level(14, R.string.orange_juice, R.drawable.animal_04, false, "Animal"),
        Level(15, R.string.orange_juice, R.drawable.anime_and_cartoon_04, false, "Anime"),
        Level(16, R.string.orange_juice, R.drawable.animal_06, false, "Animal"),
        Level(17, R.string.orange_juice, R.drawable.cute_kawaii_02, false, "Kawaii"),
        Level(18, R.string.orange_juice, R.drawable.animal_05, false, "Animal"),
        Level(19, R.string.orange_juice, R.drawable.cute_kawaii_03, false, "Kawaii"),
        Level(20, R.string.orange_juice, R.drawable.animal_09, false, "Animal"),
        Level(21, R.string.orange_juice, R.drawable.cute_kawaii_06, false, "Kawaii"),
        Level(22, R.string.orange_juice, R.drawable.animal_08, false, "Animal"),
        Level(23, R.string.orange_juice, R.drawable.cute_kawaii_04, false, "Kawaii"),
        Level(24, R.string.orange_juice, R.drawable.cute_kawaii_08, false, "Kawaii"),
        Level(25, R.string.orange_juice, R.drawable.animal_07, false, "Animal"),
        Level(26, R.string.orange_juice, R.drawable.cute_kawaii_09, false, "Kawaii"),
        Level(27, R.string.orange_juice, R.drawable.animal_10, false, "Animal"),
        Level(28, R.string.orange_juice, R.drawable.animal_01, false, "Animal"),
        Level(29, R.string.orange_juice, R.drawable.anime_and_cartoon_09, false, "Anime"),
        Level(30, R.string.orange_juice, R.drawable.anime_and_cartoon_10, false, "Anime")
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