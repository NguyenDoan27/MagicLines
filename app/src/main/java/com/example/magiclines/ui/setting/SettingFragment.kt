
package com.example.magiclines.ui.setting

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.magiclines.R
import com.example.magiclines.base.BaseFragment
import com.example.magiclines.common.adapter.MusicAdapter2
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.databinding.FragmentSettingBinding
import com.example.magiclines.databinding.SelectLanguageDialogBinding
import com.example.magiclines.models.Audio
import com.example.magiclines.services.SoundService


class SettingFragment : BaseFragment<FragmentSettingBinding, SettingViewModel>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSettingBinding
        get() = FragmentSettingBinding::inflate

    override fun initViewBinding() {
        binding.rcvSounds.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
        binding.apply {
            swMusic.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setIsSoundEnabled(isChecked)
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

    private var dialog: Dialog? = null
    //    private lateinit var dataStore: SettingDataStore
    private var musicAdapter: MusicAdapter2? = null

    private var currentLanguageCode: String = "en"
    private var currentMusicPosition: Int = 0
    private var isSoundEnabled: Boolean = true
    private var sounds: List<Audio> = emptyList()
    private val viewModel: SettingViewModel by lazy {
        SettingViewModel(SettingDataStore(requireContext()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getData()

        musicAdapter = MusicAdapter2(
            requireContext(),
            currentMusicPosition
        ) { position ->
            viewModel.setMusicPosition(position)
            with(Intent(requireContext(), SoundService::class.java)) {
                if (isSoundEnabled) {
                    requireContext().stopService(this)
                    requireContext().startService(this)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.musics.observe(viewLifecycleOwner) { musics ->
            sounds = musics
            updateMusicAdapter()
        }
        viewModel.currentMusicPosition.observe(viewLifecycleOwner) { pos ->
            currentMusicPosition = pos
            musicAdapter!!.setSelectedMusic(pos)
            binding.rcvSounds.smoothScrollToPosition(pos)
        }

        viewModel.isSoundEnabled.observe(viewLifecycleOwner) { isEnabled ->
            isSoundEnabled = isEnabled
            binding.swMusic.isChecked = isEnabled
            with(Intent(requireContext(), SoundService::class.java)) {
                if (isEnabled) {
                    requireContext().stopService(this)
                    requireContext().startService(this)
                } else {
                    requireContext().stopService(this)
                }
            }
        }

        viewModel.currentLanguageCode.observe(viewLifecycleOwner) { language ->
            currentLanguageCode = language
        }
    }

    private fun updateMusicAdapter() {
        binding.rcvSounds.apply {
            adapter = musicAdapter
            musicAdapter!!.submitList(sounds)
        }
    }


    private fun openContactEmail() {
        val emailUri = "mailto:doannguyen22702@gmail.com" +
                "?subject=" + Uri.encode("Report my app")

        Intent(Intent.ACTION_SENDTO).apply {
            data = emailUri.toUri()
        }.takeIf { it.resolveActivity(requireActivity().packageManager) != null }?.let {
            startActivity(it)
        } ?: showToast(resources.getString(R.string.no_app_support_email))
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
                    setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
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
        viewModel.setLanguage(languageCode)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
