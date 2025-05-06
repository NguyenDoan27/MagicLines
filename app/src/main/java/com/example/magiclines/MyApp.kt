package com.example.magiclines

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.magiclines.data.SoundManager

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(SoundManager(this))
    }
}