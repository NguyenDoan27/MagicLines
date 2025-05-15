package com.example.magiclines

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.magiclines.databinding.ActivitySettingBinding
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.magiclines.adapters.MusicAdapter
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.databinding.SelectLanguageDialogBinding
import com.example.magiclines.interfaces.IOnClick
import com.example.magiclines.models.Audio
import com.example.magiclines.services.SoundService
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SettingActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingBinding
    private var dialog: Dialog? = null
    private lateinit var dataStore: SettingDataStore
    private var musicAdapter: MusicAdapter? = null

    private var currentLanguageCode: String = "en"
    private var currentMusicPosition: Int = 0
    private var isSoundEnabled: Boolean = true
    private var sounds: List<Audio> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        initDataStore()
        setupRecyclerView()
        setupListeners()
        observeDataChanges()
    }

    override fun onDestroy() {
        dialog?.dismiss()
        super.onDestroy()
    }

    private fun setupView() {
        enableEdgeToEdge()
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initDataStore() {
        dataStore = SettingDataStore(applicationContext)
    }

    private fun setupRecyclerView() {
        binding.rcvSounds.apply {
            layoutManager = LinearLayoutManager(
                this@SettingActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    private fun setupListeners() {
        binding.apply {
            swMusic.setOnCheckedChangeListener { _, isChecked ->
                handleSoundToggle(isChecked)
            }
            layoutContact.setOnClickListener { openContactEmail() }
            layoutRate.setOnClickListener { showRateAppToast() }
            layoutShare.setOnClickListener { shareApp() }
            layoutLanguages.setOnClickListener { showLanguageDialog() }
            imgBack.setOnClickListener { finish() }
        }
    }

    private fun observeDataChanges() {
        lifecycleScope.launch {
            combine(
                dataStore.musics,
                dataStore.readMusicPosition(),
                dataStore.readStateSound(),
                dataStore.language
            ) { musics, position, state, language ->
                Quad(musics, position, state, language)
            }.collect { (musics, pos, state, language) ->
                sounds = musics
                currentMusicPosition = pos
                isSoundEnabled = state
                currentLanguageCode = language

                updateMusicAdapter()
                binding.swMusic.setOnCheckedChangeListener(null)
                binding.swMusic.isChecked = isSoundEnabled
                binding.swMusic.setOnCheckedChangeListener { _, isChecked ->
                    handleSoundToggle(isChecked)
                }
            }
        }
    }

    private fun updateMusicAdapter() {
        musicAdapter = MusicAdapter(
            applicationContext,
            sounds,
            currentMusicPosition,
            object : IOnClick {
                override fun onclick(position: Int) {
                    lifecycleScope.launch {
                        dataStore.saveMusicPosition(position)
                        with(Intent(this@SettingActivity, SoundService::class.java)) {
                            if (isSoundEnabled){
                                stopService(this)
                                startService(this)
                            }
                        }
                    }
                }
            }
        )
        binding.rcvSounds.adapter = musicAdapter
    }

    private fun handleSoundToggle(isEnabled: Boolean) {
        lifecycleScope.launch {
            dataStore.saveStateSound(isEnabled)
            with(Intent(this@SettingActivity, SoundService::class.java)) {
                if (isEnabled) startService(this) else stopService(this)
            }
        }
    }


    private fun openContactEmail() {
        val emailUri = "mailto:doannguyen22702@gmail.com" +
                "?subject=" + Uri.encode("Report my app")

        Intent(Intent.ACTION_SENDTO).apply {
            data = emailUri.toUri()
        }.takeIf { it.resolveActivity(packageManager) != null }?.let {
            startActivity(it)
        } ?: showToast("No app support to send mail")
    }

    private fun showRateAppToast() {
        Toast.makeText(this, "Rated 5 star", Toast.LENGTH_SHORT).show()
    }

    private fun shareApp() {
        val textToShare = """
            I would like invite you to download this app
            
            https://play.google.com/store/apps/details?id=com.draw.drawing.glow.dot.art
        """.trimIndent()

        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textToShare)
        }.let {
            startActivity(Intent.createChooser(it, "Share"))
        }
    }

    private fun showLanguageDialog() {
        if (isFinishing || isDestroyed) return

        try {
            dialog?.dismiss()
            val dialogBinding = SelectLanguageDialogBinding.inflate(layoutInflater)

            dialog = Dialog(this).apply {
                setContentView(dialogBinding.root)
                window?.apply {
                    setLayout(550, LayoutParams.WRAP_CONTENT)
                    setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                }

                updateLanguageSelection(dialogBinding)
                setupLanguageListeners(dialogBinding)
                show()
            }
        } catch (e: Exception) {
            Log.e("SettingActivity", "Error showing language dialog", e)
        }
    }

    private fun updateLanguageSelection(binding: SelectLanguageDialogBinding) {
        when (currentLanguageCode) {
            "vi" -> {
                binding.layoutVietnamese.isEnabled = false
                binding.layoutEnglish.isEnabled = true
            }
            "en" -> {
                binding.layoutEnglish.isEnabled = false
                binding.layoutVietnamese.isEnabled = true
            }
        }
    }

    private fun setupLanguageListeners(binding: SelectLanguageDialogBinding) {
        binding.apply {
            layoutVietnamese.setOnClickListener {
                handleLanguageSelection("vi")
                layoutEnglish.isEnabled = true
                layoutVietnamese.isEnabled = false
            }

            layoutEnglish.setOnClickListener {
                handleLanguageSelection("en")
                layoutEnglish.isEnabled = false
                layoutVietnamese.isEnabled = true
            }
        }
    }

    private fun handleLanguageSelection(languageCode: String) {
        lifecycleScope.launch {
            dataStore.saveLanguage(languageCode)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private data class Quad<out A, out B, out C, out D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}