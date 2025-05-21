package com.example.magiclines.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.magiclines.R
import com.example.magiclines.adapters.BackgroundAdapter2
import com.example.magiclines.adapters.MusicAdapter2
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.databinding.AddEnergyDialogBinding
import com.example.magiclines.databinding.EffectBottomDialogBinding
import com.example.magiclines.databinding.ExitGraftDialogBinding
import com.example.magiclines.databinding.FragmentPlayingBinding
import com.example.magiclines.models.Audio
import com.example.magiclines.models.Color
import com.example.magiclines.models.Level
import com.example.magiclines.services.SoundService
import com.example.magiclines.views.PlayingView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit


class PlayingFragment : Fragment(), PlayingView.OnProcessingCompleteListener {

    companion object {
        private const val TAG = "PlayingActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    private lateinit var binding: FragmentPlayingBinding
    private var dialog: BottomSheetDialog? = null
    private var dialogBinding: EffectBottomDialogBinding? = null
    private var currentColorPosition = 0
    private var currentMusicPosition = 0
    private var isPlaying = true
    private lateinit var dataStore: SettingDataStore
    private var customView: PlayingView? = null
    private var position = -1
    private var levels: List<Level> = emptyList()
    private var musics: List<Audio> = emptyList()
    private var tts : TextToSpeech? = null
    private var musicAdapter: MusicAdapter2? = null
    private var backgroundAdapter: BackgroundAdapter2? = null
    private val args: PlayingFragmentArgs by navArgs()
    private var originLevels: List<Level> = emptyList()
    private var isComplete = false
    private var exitDialogBinding : ExitGraftDialogBinding? = null
    private var exitDialog: Dialog? = null

    private val backgrounds = listOf(
        Color("Green", "#29724E"),
        Color("Peach cream", "#2C2972"),
        Color("Olive", "#677229"),
        Color("Maroon", "#72294C"),
        Color("Teal Green", "#297266"),
        Color("Elf Green", "#295672"),
        Color("SandyBrown", "#442972"),
    )

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { handlePermissionResult(it) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlayingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        setupListeners()
        observeDataStore()
        handleTextToSpeech()
    }

    override fun onComplete() {
        handleLevelCompletion()
    }

    private fun initData() {
        dataStore = SettingDataStore(requireContext())
        lifecycleScope.launch {
            originLevels = dataStore.levelsFlow.first()
        }

        levels = args.levels.toList()
        position = args.position

        if (levels.isNotEmpty() && position != -1) {
            setupCustomView()
        } else {
            showToast("No level available")
        }
    }

    private fun setupCustomView() {
        customView = PlayingView(requireContext(), levels[position]).apply {
            setOnProcessingCompleteListener(this@PlayingFragment)
            binding.frPlaying.addView(this)
            setToolbarVisibility(false)
        }
    }

    private fun setupListeners() {
        binding.apply {
            imgEffect.setOnClickListener { showEffectDialog() }
            imgBack.setOnClickListener {

                if (isComplete) findNavController().popBackStack()
                else showExitDialog()
            }
            imgRePlay.setOnClickListener { handleReplay() }
            imgNextLevel.setOnClickListener { loadNextLevel() }
            imgDownLoad.setOnClickListener { handleDownload() }
            imgShare.setOnClickListener { shareCurrentLevel() }
        }
    }

    private fun observeDataStore() {
        lifecycleScope.launch {
            combine(
                dataStore.readMusicPosition(),
                dataStore.readBackgroundPosition(),
                dataStore.readStateSound()
            ) { musicPos, bgPos, soundState -> Triple(musicPos, bgPos, soundState) }
                .collect { (musicPos, bgPos, soundState) ->
                    currentMusicPosition = musicPos
                    currentColorPosition = bgPos
                    isPlaying = soundState
                    updateBackgroundColor(backgrounds.getOrNull(bgPos)?.codeColor)
                }
        }

        lifecycleScope.launch {
            musics = dataStore.musics.first()
        }
    }

    private fun handleReplay() {
        customView?.let {
            lifecycleScope.launch {
                dataStore.saveEnergy(dataStore.energy.first() - 1)
            }
            it.setTouchEnabled(true)
            it.scramblePaths()
            isComplete = false
            setToolbarVisibility(false)
        }
    }

    private fun loadNextLevel() {
        position++
        if (position == levels.size ) {
            Toast.makeText(requireContext(), "No more level", Toast.LENGTH_SHORT).show()
            return
        }else{
            binding.frPlaying.removeView(customView)
            customView = PlayingView(requireContext(), levels[position]).apply {
                setOnProcessingCompleteListener(this@PlayingFragment)
                binding.frPlaying.addView(this)
                setToolbarVisibility(false)
            }
        }

    }

    private fun handleDownload() {
        if (allPermissionsGranted()) {
            saveLevelImage()
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun shareCurrentLevel() {
        levels.getOrNull(position)?.resourceId?.let { resId ->
            shareDrawable(resources.getDrawable(resId))
        }
    }

    private fun updateBackgroundColor(color: String?) {
        color?.let {
            val colorInt = color.toColorInt()
            val strokeColor = ContextCompat.getColor(requireContext(), R.color.light_blue)

            binding.frPlaying.setGradientBackground(colorInt)
            listOf(
                binding.imgEffect,
                binding.imgRePlay,
                binding.imgNextLevel,
                binding.imgBack,
                binding.imgDownLoad,
                binding.imgShare
            ).forEach { view ->
                view.setGradientBackground(colorInt, 2, strokeColor, 50f)
            }
        }
    }

    private fun showEffectDialog() {
        if (!isAdded || isDetached) return

        try {
            dialog?.dismiss()
            dialogBinding = EffectBottomDialogBinding.inflate(layoutInflater)

            dialog = BottomSheetDialog(requireContext()).apply {
                setContentView(dialogBinding?.root ?: return@apply)
                setupMusicAdapter()
                setupBackgroundAdapter()
                setupSoundSwitch()
                setOnDismissListener { dialogBinding = null }
                show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing dialog: ${e.message}")
        }
    }

    private fun setupMusicAdapter() {
        musicAdapter = MusicAdapter2(
            requireContext(),
            currentMusicPosition){position ->
            lifecycleScope.launch {
                dataStore.saveMusicPosition(position)
                with(Intent(requireContext(), SoundService::class.java)) {
                    if (isPlaying) {
                        requireContext().stopService(this)
                        requireContext().startService(this)
                    }
                }
            }
        }
        dialogBinding?.rcvMusicList?.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = musicAdapter
            musicAdapter!!.submitList(musics)
        }
    }

    private fun setupBackgroundAdapter() {
        backgroundAdapter = BackgroundAdapter2(requireContext(),currentColorPosition){position ->
            currentColorPosition = position
            updateBackgroundColor(backgrounds[position].codeColor)
            lifecycleScope.launch {
                dataStore.saveBackgroundPosition(position)
            }
        }
        dialogBinding?.rcvBackgroundList?.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = backgroundAdapter
            backgroundAdapter!!.submitList(backgrounds)
        }
    }

    private fun setupSoundSwitch() {
        dialogBinding?.swMusic?.apply {
            isChecked = isPlaying
            setOnCheckedChangeListener { _, isChecked ->
                lifecycleScope.launch {
                    dataStore.saveStateSound(isChecked)
                    with(Intent(requireContext(), SoundService::class.java)) {
                        if (isChecked) requireContext().startService(this) else requireContext().stopService(this)
                    }
                }
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        val allGranted = permissions.entries.all { it.value }
        if (!allGranted) {
            showToast("Permission request denied")
        } else {
            saveLevelImage()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun saveLevelImage() {
        levels.getOrNull(position)?.resourceId?.let { resId ->
            val bitmap = drawableToBitmap(resources.getDrawable(resId))
            saveBitmapToGallery(bitmap)
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight).apply {
                val canvas = Canvas(this)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            }
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MagicLines")
            }
        }

        requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
            requireContext().contentResolver.openOutputStream(uri)?.use {
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) {
                    showToast("Saved to Gallery")
                }
            }
        } ?: showToast("Failed to save image")
    }

    private fun shareDrawable(drawable: Drawable) {
        val bitmap = drawableToBitmap(drawable)
        val cacheFile = File(requireContext().cacheDir, "images/shared_image.png").apply {
            parentFile?.mkdirs()
            FileOutputStream(this).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }

        val contentUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            cacheFile
        )

        Intent.createChooser(
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            "Share image"
        ).let { startActivity(it) }
    }

    private fun handleLevelCompletion() {
        lifecycleScope.launch {
            dataStore.apply {
                for (i in originLevels){
                    if (i.resourceId == levels[position].resourceId){
                        i.isComplete = true
                        break
                    }
                }
                saveLevel(originLevels)
            }
        }
        val handle = Handler(Looper.getMainLooper())
        val action = Runnable{
            val party = Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                position = Position.Relative(0.5, 0.3)
            )

            binding.konfettiView.start(party)
        }
        binding.tvName.text = getString(levels[position].resourceName)
        handle.postDelayed(action, 1000)
        setToolbarVisibility(true)
        isComplete = true
    }

    private fun setToolbarVisibility(visible: Boolean) {
        binding.apply {
            imgRePlay.visibility = visible.toVisibility()
            imgShare.visibility = visible.toVisibility()
            imgNextLevel.visibility = visible.toVisibility()
            imgDownLoad.visibility = visible.toVisibility()
            imgLoudspeaker.visibility = visible.toVisibility()
            tvName.visibility = visible.toVisibility()
        }
    }

    private fun Boolean.toVisibility() = if (this) View.VISIBLE else View.GONE

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun handleTextToSpeech(){
        tts = TextToSpeech(requireContext(), object: TextToSpeech.OnInitListener {
            override fun onInit(p0: Int) {
                lifecycleScope.launch {
                    dataStore.language.collect { language ->
                        when(language){
                            "vi" -> tts!!.language = Locale("vi", "VN")
                            "en" -> tts!!.language = Locale("en", "US")
                            else -> tts!!.language = Locale("vi", "VN")
                        }
                    }
                }
            }
        })

        binding.imgLoudspeaker.setOnClickListener {
            tts!!.speak(binding.tvName.text.toString(), TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }
    private fun showExitDialog() {
        if (!isAdded || isDetached) return

        try {
            exitDialog?.dismiss()
            exitDialogBinding = ExitGraftDialogBinding.inflate(layoutInflater)

            exitDialog = Dialog(requireContext()).apply {
                setContentView(exitDialogBinding?.root ?: return@apply)
                window?.apply {
                    setLayout(550, LayoutParams.WRAP_CONTENT)
                    setBackgroundDrawable(android.graphics.Color.TRANSPARENT.toDrawable())
                }

                exitDialogBinding!!.btnEit.setOnClickListener {
                    findNavController().popBackStack()
                    exitDialog!!.dismiss()
                }
                exitDialogBinding!!.btnCancel.setOnClickListener {
                    exitDialog!!.dismiss()
                }
                show()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error showing energy dialog", e)
        }
    }
    private fun View.setGradientBackground(
        color: Int,
        strokeWidth: Int = 0,
        strokeColor: Int = android.graphics.Color.TRANSPARENT,
        cornerRadius: Float = 0f
    ) {
        background = GradientDrawable().apply {
            setColor(color)
            if (strokeWidth > 0) setStroke(strokeWidth, strokeColor)
            if (cornerRadius > 0) this.cornerRadius = cornerRadius
        }
    }
}

