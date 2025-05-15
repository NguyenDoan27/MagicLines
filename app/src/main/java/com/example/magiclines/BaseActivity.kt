package com.example.magiclines

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.example.magiclines.data.SettingDataStore
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first

import kotlinx.coroutines.launch
import java.util.Locale

abstract class BaseActivity : AppCompatActivity() {
    private val dataStore by lazy { SettingDataStore(applicationContext) }
    private var currentLanguage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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