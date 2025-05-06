package com.example.magiclines

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.example.magiclines.data.SettingDataStore

import kotlinx.coroutines.launch
import java.util.Locale

abstract class BaseActivity : AppCompatActivity() {
    private var dataStore: SettingDataStore? = null
    private var currentLanguage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStore = SettingDataStore(applicationContext)
    }

    override fun onStart() {
        super.onStart()
        observeLanguageChanges()
    }

    private fun observeLanguageChanges() {
        lifecycleScope.launch {
            dataStore?.language?.collect { newLanguage ->
                if (newLanguage != currentLanguage) {
                    currentLanguage = newLanguage
                    updateLocale(newLanguage)
                }
            }
        }
    }

    private fun updateLocale(languageCode: String) {
        val config = resources.configuration
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            config.locale = locale
        }


        val newContext = createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)


        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            recreate()
        }
    }

    override fun onDestroy() {
        dataStore = null
        super.onDestroy()
    }
}