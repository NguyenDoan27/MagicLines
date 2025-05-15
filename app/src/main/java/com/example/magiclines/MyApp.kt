package com.example.magiclines

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.data.SoundManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class MyApp : Application() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main) // Sử dụng Dispatchers.Main
    private lateinit var soundManager: SoundManager

    override fun onCreate() {
        super.onCreate()
        val dataStore = SettingDataStore(applicationContext)

        scope.launch {

            dataStore.readStateSound()
                .flowOn(Dispatchers.IO)
                .collect { isSound ->
                    soundManager = SoundManager(applicationContext, isSound)
                    ProcessLifecycleOwner.get().lifecycle.addObserver(soundManager)
                }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        scope.cancel()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(soundManager)
    }
}