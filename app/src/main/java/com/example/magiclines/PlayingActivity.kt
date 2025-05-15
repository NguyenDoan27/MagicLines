package com.example.magiclines

import android.Manifest
import android.annotation.SuppressLint
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
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.magiclines.adapters.BackgroundAdapter
import com.example.magiclines.adapters.MusicAdapter
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.databinding.ActivityPlayingBinding
import com.example.magiclines.databinding.EffectBottomDialogBinding
import com.example.magiclines.interfaces.IOnClick
import com.example.magiclines.models.Audio
import com.example.magiclines.models.Color
import com.example.magiclines.models.Level
import com.example.magiclines.views.PlayingView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import androidx.core.graphics.createBitmap
import com.example.magiclines.services.SoundService
import kotlinx.coroutines.flow.first
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class PlayingActivity : BaseActivity(), PlayingView.OnProcessingCompleteListener {

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

    private lateinit var binding: ActivityPlayingBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        initData()
        setupListeners()
        observeDataStore()
        handleTextToSpeech()
    }

    override fun onDestroy() {
        dialog?.dismiss()
        super.onDestroy()
    }

    override fun onComplete() {
        handleLevelCompletion()
    }

    private fun setupView() {
        enableEdgeToEdge()
        binding = ActivityPlayingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initData() {
        dataStore = SettingDataStore(this)
        levels = intent.getSerializableExtra("levels") as? ArrayList<Level> ?: arrayListOf()
        position = intent.getIntExtra("position", -1)

        if (levels.isNotEmpty() && position != -1) {
            setupCustomView()
        } else {
            showToast("No level available")
        }
    }

    private fun setupCustomView() {
        customView = PlayingView(this, levels[position]).apply {
            setOnProcessingCompleteListener(this@PlayingActivity)
            binding.frPlaying.addView(this)
            setToolbarVisibility(false)
        }
    }

    private fun setupListeners() {
        binding.apply {
            imgEffect.setOnClickListener { showEffectDialog() }
            imgBack.setOnClickListener { finish() }
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
                    updateBackgroundColor(backgrounds.getOrNull(bgPos)?.getCodeColor())
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
            setToolbarVisibility(false)
        }
    }

    private fun loadNextLevel() {
        position++
        binding.frPlaying.removeView(customView)
        customView = PlayingView(this, levels[position]).apply {
            setOnProcessingCompleteListener(this@PlayingActivity)
            binding.frPlaying.addView(this)
            setToolbarVisibility(false)
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
        levels.getOrNull(position)?.getResourceId()?.let { resId ->
            shareDrawable(resources.getDrawable(resId))
        }
    }

    private fun updateBackgroundColor(color: String?) {
        color?.let {
            val colorInt = color.toColorInt()
            val strokeColor = ContextCompat.getColor(this, R.color.light_blue)

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
        if (isFinishing || isDestroyed) return

        try {
            dialog?.dismiss()
            dialogBinding = EffectBottomDialogBinding.inflate(layoutInflater)

            dialog = BottomSheetDialog(this).apply {
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
        dialogBinding?.rcvMusicList?.apply {
            layoutManager = LinearLayoutManager(this@PlayingActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = MusicAdapter(
                this@PlayingActivity,
                musics,
                currentMusicPosition,
                object : IOnClick {
                    override fun onclick(position: Int) {
                        lifecycleScope.launch { dataStore.saveMusicPosition(position) }
                        with(Intent(this@PlayingActivity, SoundService::class.java)) {
                            if (isPlaying){
                                stopService(this)
                                startService(this)
                            }
                        }
                    }
                }
            )
        }
    }

    private fun setupBackgroundAdapter() {
        dialogBinding?.rcvBackgroundList?.apply {
            layoutManager = LinearLayoutManager(this@PlayingActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = BackgroundAdapter(
                backgrounds,
                currentColorPosition,
                object : IOnClick {
                    override fun onclick(position: Int) {
                        currentColorPosition = position
                        updateBackgroundColor(backgrounds[position].getCodeColor())
                        lifecycleScope.launch {
                            dataStore.saveBackgroundPosition(position)
                        }
                    }
                }
            )
        }
    }

    private fun setupSoundSwitch() {
        dialogBinding?.swMusic?.apply {
            isChecked = isPlaying
            setOnCheckedChangeListener { _, isChecked ->
                lifecycleScope.launch {
                    dataStore.saveStateSound(isChecked)
                    with(Intent(this@PlayingActivity, SoundService::class.java)) {
                        if (isChecked) startService(this) else stopService(this)
                    }
                }
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
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
        levels.getOrNull(position)?.getResourceId()?.let { resId ->
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

        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
            contentResolver.openOutputStream(uri)?.use {
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) {
                    showToast("Saved to Gallery")
                }
            }
        } ?: showToast("Failed to save image")
    }

    private fun shareDrawable(drawable: Drawable) {
        val bitmap = drawableToBitmap(drawable)
        val cacheFile = File(cacheDir, "images/shared_image.png").apply {
            parentFile?.mkdirs()
            FileOutputStream(this).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }

        val contentUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
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
                saveLevel(levels)
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
        binding.tvName.text = getString(levels[position].getResourceName()!!)
        handle.postDelayed(action, 1000)
        setToolbarVisibility(true)
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
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun handleTextToSpeech(){
         tts = TextToSpeech(this, object: TextToSpeech.OnInitListener {
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
}


    fun View.setGradientBackground(
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



