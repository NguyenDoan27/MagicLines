package com.example.magiclines.ui.main

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.magiclines.base.BaseActivity
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.data.SoundManager
import com.example.magiclines.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main) // Sử dụng Dispatchers.Main
    private lateinit var soundManager: SoundManager

    private val dataStore by lazy { SettingDataStore(applicationContext) }
    private var currentLanguage: String? = null

    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate

    override fun initViewBinding() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataStore = SettingDataStore(applicationContext)
        scope.launch {

            dataStore.readStateSound()
                .flowOn(Dispatchers.IO)
                .collect { isSound ->
                    soundManager = SoundManager(applicationContext, isSound)
                    ProcessLifecycleOwner.Companion.get().lifecycle.addObserver(soundManager)
                }
        }

        lifecycleScope.launch {
            val initialLanguage = dataStore.language.first()
            updateLocale(initialLanguage)
        }
    }


    override fun onStart() {
        super.onStart()
        observeLanguageChanges()
    }


    private fun observeLanguageChanges() {
        lifecycleScope.launch {
            dataStore.language
                .distinctUntilChanged()
                .collect { newLanguage ->
                    if (newLanguage != currentLanguage) {
                        currentLanguage = newLanguage
                        updateLocale(newLanguage)

                        onLanguageChanged(newLanguage)
                    }
                }
        }
    }

    protected open fun onLanguageChanged(newLanguage: String) {

    }

    private fun updateLocale(languageCode: String) {
        val config = resources.configuration
        val locale = try {
            Locale(languageCode)
        } catch (e: IllegalArgumentException) {
            Locale.getDefault()
        }

        Locale.setDefault(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLocales(LocaleList(locale))
        } else {
            config.locale = locale
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            applicationContext.createConfigurationContext(config)
        }

        resources.updateConfiguration(config, resources.displayMetrics)

        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            recreate()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)

        lifecycleScope.launch {
            val language = dataStore.language.first()
            updateLocale(language)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}