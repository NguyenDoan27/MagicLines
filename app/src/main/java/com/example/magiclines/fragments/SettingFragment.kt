package com.example.magiclines.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.magiclines.adapters.MusicAdapter2
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.databinding.FragmentSettingBinding
import com.example.magiclines.databinding.SelectLanguageDialogBinding
import com.example.magiclines.models.Audio
import com.example.magiclines.services.SoundService
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding
    private var dialog: Dialog? = null
    private lateinit var dataStore: SettingDataStore
    private var musicAdapter: MusicAdapter2? = null

    private var currentLanguageCode: String = "en"
    private var currentMusicPosition: Int = 0
    private var isSoundEnabled: Boolean = true
    private var sounds: List<Audio> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDataStore()
        setupRecyclerView()
        setupListeners()
        observeDataChanges()
    }


    private fun initDataStore() {
        dataStore = SettingDataStore(requireContext())
    }

    private fun setupRecyclerView() {
        binding.rcvSounds.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
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
            imgBack.setOnClickListener {
                findNavController().popBackStack()
            }
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
            }.distinctUntilChanged()
                .collect { (musics, pos, state, language) ->
                    sounds = musics
                    currentMusicPosition = pos
                    isSoundEnabled = state
                    currentLanguageCode = language
                    Log.e("TAG", "observeDataChanges: $isSoundEnabled", )
                    updateMusicAdapter()
                    binding.swMusic.isChecked = state
                }
        }
    }

    private fun updateMusicAdapter() {
        musicAdapter = MusicAdapter2(
            requireContext(),
            currentMusicPosition
        ) { position ->
            lifecycleScope.launch {
                dataStore.saveMusicPosition(position)
                with(Intent(requireContext(), SoundService::class.java)) {
                    if (isSoundEnabled) {
                        requireContext().stopService(this)
                        requireContext().startService(this)
                    }
                }
            }
        }
        binding.rcvSounds.apply {
            adapter = musicAdapter
            musicAdapter!!.submitList(sounds)
        }
    }


    private fun handleSoundToggle(isEnabled: Boolean) {
            lifecycleScope.launch {
                dataStore.saveStateSound(isEnabled)
                with(Intent(requireContext(), SoundService::class.java)) {
                    if (isEnabled) {
                        Log.e("TAG", "handleSoundToggle: start", )
                        requireContext().startService(this)
                    } else {
                        requireContext().stopService(this)
                    }
                }
            }
    }


    private fun openContactEmail() {
        val emailUri = "mailto:doannguyen22702@gmail.com" +
                "?subject=" + Uri.encode("Report my app")

        Intent(Intent.ACTION_SENDTO).apply {
            data = emailUri.toUri()
        }.takeIf { it.resolveActivity(requireActivity().packageManager) != null }?.let {
            startActivity(it)
        } ?: showToast("No app support to send mail")
    }

    private fun showRateAppToast() {
        Toast.makeText(requireContext(), "Rated 5 star", Toast.LENGTH_SHORT).show()
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
        if (!isAdded || isDetached) return

        try {
            dialog?.dismiss()
            val dialogBinding = SelectLanguageDialogBinding.inflate(layoutInflater)

            dialog = Dialog(requireContext()).apply {
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private data class Quad<out A, out B, out C, out D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )


}