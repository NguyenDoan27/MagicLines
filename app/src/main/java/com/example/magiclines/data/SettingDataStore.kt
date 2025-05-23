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
        Level(1,  R.drawable.people_portraits_7, false, R.string.people),
        Level(2,  R.drawable.flowers_nature_7, false, R.string.flowers),
        Level(3,  R.drawable.mandala10, false, R.string.mandala),
        Level(4,  R.drawable.anime_and_cartoon_03, false, R.string.anime),
        Level(5,  R.drawable.people_portraits_10, false, R.string.people),
        Level(6,  R.drawable.animal_10, false, R.string.animal),
        Level(7,  R.drawable.fantasy_magic_4, false, R.string.fantasy),
        Level(8,  R.drawable.cute_kawaii_07, false, R.string.kawaii),
        Level(9,  R.drawable.anime_and_cartoon_01, false, R.string.anime),
        Level(10,  R.drawable.mandala8, false, R.string.mandala),
        Level(11,  R.drawable.people_portraits_5, false, R.string.people),
        Level(12,  R.drawable.flowers_nature_10, false, R.string.flowers),
        Level(13,  R.drawable.animal_02, false, R.string.animal),
        Level(14,  R.drawable.anime_and_cartoon_06, false, R.string.anime),
        Level(15,  R.drawable.cute_kawaii_05, false, R.string.kawaii),
        Level(16,  R.drawable.animal_03, false, R.string.animal),
        Level(17,  R.drawable.flowers_nature_1, false, R.string.flowers),
        Level(18,  R.drawable.anime_and_cartoon_05, false, R.string.anime),
        Level(19,  R.drawable.cute_kawaii_10, false, R.string.kawaii),
        Level(20,  R.drawable.anime_and_cartoon_02, false, R.string.anime),
        Level(21,  R.drawable.fantasy_magic_2, false, R.string.fantasy),
        Level(22,  R.drawable.animal_01, false, R.string.animal),
        Level(23,  R.drawable.cute_kawaii_01, false, R.string.kawaii),
        Level(24,  R.drawable.animal_04, false, R.string.animal),
        Level(25,  R.drawable.anime_and_cartoon_04, false, R.string.anime),
        Level(26,  R.drawable.animal_06, false, R.string.animal),
        Level(27,  R.drawable.cute_kawaii_02, false, R.string.kawaii),
        Level(28,  R.drawable.fantasy_magic_3, false, R.string.fantasy),
        Level(29,  R.drawable.flowers_nature_4, false, R.string.flowers),
        Level(30,  R.drawable.fantasy_magic_9, false, R.string.fantasy),
        Level(31,  R.drawable.flowers_nature_3, false, R.string.flowers),
        Level(32,  R.drawable.people_portraits_8, false, R.string.people),
        Level(33,  R.drawable.mandala4, false, R.string.mandala),
        Level(34,  R.drawable.people_portraits_9, false, R.string.people),
        Level(35,  R.drawable.flowers_nature_2, false, R.string.flowers),
        Level(36,  R.drawable.mandala6, false, R.string.mandala),
        Level(37,  R.drawable.flowers_nature_8, false, R.string.flowers),
        Level(38,  R.drawable.fantasy_magic_6, false, R.string.fantasy),
        Level(39,  R.drawable.people_portraits_1, false, R.string.people),
        Level(40,  R.drawable.people_portraits_4, false, R.string.people),
        Level(41,  R.drawable.fantasy_magic_8, false, R.string.fantasy),
        Level(42,  R.drawable.mandala1, false, R.string.mandala),
        Level(43,  R.drawable.flowers_nature_5, false, R.string.flowers),
        Level(44,  R.drawable.animal_05, false, R.string.animal),
        Level(45,  R.drawable.cute_kawaii_03, false, R.string.kawaii),
        Level(46,  R.drawable.animal_09, false, R.string.animal),
        Level(47,  R.drawable.cute_kawaii_06, false, R.string.kawaii),
        Level(48,  R.drawable.animal_08, false, R.string.animal),
        Level(49,  R.drawable.cute_kawaii_04, false, R.string.kawaii),
        Level(50,  R.drawable.mandala3, false, R.string.mandala),
        Level(51,  R.drawable.cute_kawaii_08, false, R.string.kawaii),
        Level(52,  R.drawable.fantasy_magic_5, false, R.string.fantasy),
        Level(53,  R.drawable.animal_07, false, R.string.animal),
        Level(54,  R.drawable.cute_kawaii_09, false, R.string.kawaii),
        Level(55,  R.drawable.anime_and_cartoon_08, false, R.string.anime),
        Level(56,  R.drawable.mandala5, false, R.string.mandala),
        Level(57,  R.drawable.anime_and_cartoon_07, false, R.string.anime),
        Level(58,  R.drawable.mandala7, false, R.string.mandala),
        Level(59,  R.drawable.people_portraits_6, false, R.string.people),
        Level(60,  R.drawable.fantasy_magic_10, false, R.string.fantasy),
        Level(61,  R.drawable.mandala2, false, R.string.mandala),
        Level(62,  R.drawable.people_portraits_2, false, R.string.people),
        Level(63,  R.drawable.anime_and_cartoon_09, false, R.string.anime),
        Level(64,  R.drawable.mandala9, false, R.string.mandala),
        Level(65,  R.drawable.flowers_nature_9, false, R.string.flowers),
        Level(66,  R.drawable.flowers_nature_6, false, R.string.flowers),
        Level(67,  R.drawable.fantasy_magic_7, false, R.string.fantasy),
        Level(68,  R.drawable.anime_and_cartoon_10, false, R.string.anime),
        Level(69,  R.drawable.people_portraits_3, false, R.string.people),
        Level(70,  R.drawable.fantasy_magic_1, false, R.string.fantasy),
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