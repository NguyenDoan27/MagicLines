package com.example.magiclines.data

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.example.magiclines.services.SoundService

class SoundManager(private val context: Context, val isPlaySound: Boolean):LifecycleObserver  {
    val intent = Intent(context, SoundService::class.java)
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        if (isPlaySound){
            context.startService(intent)
        }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun pause() {
        context.stopService(intent)
    }
}