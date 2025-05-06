package com.example.magiclines.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import com.example.magiclines.R
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.models.Audio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SoundService: Service() {
    private val musics = arrayListOf<Audio>(
        Audio("Memories", R.raw.our_memories),
        Audio("Pure love", R.raw.pure_love)
    )
    private var musicPosition = -1
    private var mediaPlayer: MediaPlayer? = null
    private var dataStore: SettingDataStore? = null
    private var soundState: Boolean = true
    override fun onCreate() {
        super.onCreate()
        dataStore = SettingDataStore(applicationContext)
        runBlocking {
            val (position, state) = withContext(Dispatchers.IO) {
                Pair(
                    dataStore!!.readMusicPosition().first(),
                    dataStore!!.readStateSound().first()
                )
            }
            musicPosition = position
            soundState = state
        }
        try {
            if (musicPosition != -1){
                mediaPlayer = MediaPlayer.create(this, musics[musicPosition].getRawResourceId() ?: return).apply {
                    isLooping = true
                }
            }else{
                Log.e("TAG", "onCreate: position $musicPosition", )
            }

        }catch (e: Exception){
            Log.e("TAG", "onCreate: ${e.message}", )
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