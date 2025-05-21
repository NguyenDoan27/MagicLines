package com.example.magiclines.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.models.Audio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SoundService: Service() {
    private var musics1 = listOf<Audio>()
    private var musicPosition = -1
    private var mediaPlayer: MediaPlayer? = null
    private var dataStore: SettingDataStore? = null
    private var soundState: Boolean = true
    override fun onCreate() {
        super.onCreate()
        Log.e("TAG", "onCreate: service ", )
        dataStore = SettingDataStore(applicationContext)
        runBlocking {
            val (position, state, musics) = withContext(Dispatchers.IO) {
                Triple(
                    dataStore!!.readMusicPosition().first(),
                    dataStore!!.readStateSound().first(),
                    dataStore!!.musics.first()
                )
            }
            musicPosition = position
            soundState = state
            musics1 = musics
        }
        try {
            if (musicPosition != -1){
                mediaPlayer = MediaPlayer.create(this, musics1[musicPosition].rawResourceId).apply {
                    isLooping = true
                }
            }else{
                Log.e("TAG", "onCreate1: position $musicPosition", )
            }

        }catch (e: Exception){
            Log.e("TAG", "onCreate2: ${e.stackTrace}", )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (soundState){
            Log.e("TAG", "onStartCommand: start", )
            mediaPlayer?.start()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
    override fun onBind(p0: Intent?): IBinder? {
       return null
    }
}