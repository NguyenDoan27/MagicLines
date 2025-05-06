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
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
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
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class PlayingActivity : BaseActivity() {

    private lateinit var binding: ActivityPlayingBinding
    private var dialog: BottomSheetDialog? = null
    private var dialogBinding: EffectBottomDialogBinding? = null
    private var currentColorPosition = 0
    private var currentMusicPosition = 0
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying: Boolean = true
    private lateinit var dataStore: SettingDataStore
    private var customview: View? = null
    private var position: Int = -1
    private var myList: ArrayList<Level>? = null
    private var musics: List<Audio>? = null

    val backgrounds = listOf<Color>(
        Color("Orchid", "#DA70D6"),
        Color("Peach cream", "#FFEBD0")
    )

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlayingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        myList = intent.getSerializableExtra("levels") as ArrayList<Level>
        position = intent.getIntExtra("position", -1)
        dataStore = SettingDataStore(this@PlayingActivity)
        if(myList!!.isNotEmpty() && position != -1){
            customview = PlayingView(this@PlayingActivity, myList!![position])
            binding.frPlaying.addView(customview)
            binding.imgRePlay.setOnClickListener {
                if (customview != null) {
                    (customview as PlayingView).setTouchEnabled(true)
                    (customview as PlayingView).scramblePaths()
                }
            }
        }else{
            Toast.makeText(this@PlayingActivity, "NOT LEVEL IN HERE", Toast.LENGTH_SHORT).show()
        }

        binding.imgEffect.setOnClickListener {
            showEffectDialog()
        }

        binding.imgBack.setOnClickListener {
            lifecycleScope.launch {
                dataStore.saveLevel(myList!!)
                finish()
            }
        }

        runBlocking {
            musics = dataStore.musics.first()
        }

            lifecycleScope.launch {
                combine(
                    dataStore.readMusicPosition(),
                    dataStore.readBackgroundPosition(),
                    dataStore.readStateSound()
                ) { musicPos, bgPos, soundState ->
                    Triple(musicPos, bgPos, soundState)
                }.collect { (musicPos, bgPos, soundState) ->
                    currentMusicPosition = musicPos
                    currentColorPosition = bgPos
                    isPlaying = soundState

                    initColorBackground(backgrounds.getOrNull(bgPos)?.getCodeColor())
                }
            }

        binding.imgNextLevel.setOnClickListener {
            position += 1
            binding.frPlaying.removeView(customview)
            customview = PlayingView(this@PlayingActivity, myList!![position])
            binding.frPlaying.addView(customview)
        }

        binding.imgDownLoad.setOnClickListener {
            if(allPermissionsGranted()){
                val drawable = resources.getDrawable(myList!![position].getResourceId()!!)
                val bitmap = drawableToBitmap(drawable)
                saveBitmapToGallery(bitmap)
            }else{
                requestPermissions()
            }
        }

        binding.imgShare.setOnClickListener {
            val drawable = resources.getDrawable(myList!![position].getResourceId()!!)
            shareDrawable(drawable)
        }
    }

    private fun initColorBackground(color: String?) {
        color?.let {
            val colorInt = color.toColorInt()
            val strokeColor = ContextCompat.getColor(baseContext, R.color.light_blue)

            binding.frPlaying.setGradientBackground(colorInt)
            binding.imgEffect.setGradientBackground(colorInt, 2, strokeColor, 50f)
            binding.imgRePlay.setGradientBackground(colorInt, 2, strokeColor, 50f)
            binding.imgNextLevel.setGradientBackground(colorInt, 2, strokeColor, 50f)
            binding.imgBack.setGradientBackground(colorInt, 2, strokeColor, 50f)
            binding.imgDownLoad.setGradientBackground(colorInt, 2, strokeColor, 50f)
            binding.imgShare.setGradientBackground(colorInt, 2, strokeColor, 50f)
        }
    }

    private fun showEffectDialog() {
        if (isFinishing || isDestroyed) return

        dialog?.dismiss()
        dialogBinding = null

        try {

            dialog = BottomSheetDialog(this@PlayingActivity).apply {
                dialogBinding = EffectBottomDialogBinding.inflate(layoutInflater)
                setContentView(dialogBinding?.root ?: return@apply)

                val musicAdapter = MusicAdapter(this@PlayingActivity, musics!!, currentMusicPosition, object: IOnClick {
                    override fun onclick(position: Int) {
                        lifecycleScope.launch { dataStore.saveMusicPosition(position) }
                        val intent = Intent(this@PlayingActivity, SoundService::class.java)
                        stopService(intent)
                        startService(intent)
                    }
                })

                val backgroundAdapter = BackgroundAdapter(backgrounds, currentColorPosition, object: IOnClick {
                    override fun onclick(position: Int) {
                        currentColorPosition = position
                        initColorBackground(backgrounds[position].getCodeColor())
                        lifecycleScope.launch {
                            dataStore.saveBackgroundPosition(position)
                        }
                    }
                })

                dialogBinding?.rcvMusicList?.apply {
                    layoutManager = LinearLayoutManager(
                        this@PlayingActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    adapter = musicAdapter
                }

                dialogBinding?.rcvBackgroundList?.apply {
                    layoutManager = LinearLayoutManager(
                        this@PlayingActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    adapter = backgroundAdapter
                }

                dialogBinding?.swMusic!!.isChecked = isPlaying

                dialogBinding?.swMusic?.setOnCheckedChangeListener { _, isChecked ->
                    lifecycleScope.launch {
                        dataStore.saveStateSound(isChecked)
                        val intent = Intent(this@PlayingActivity, SoundService::class.java)
                        if (isChecked) {
                            startService(intent)
                        } else {
                            stopService(intent)
                        }
                    }
                }

                setOnDismissListener {
                    dialogBinding = null
                }

                show()
            }
        } catch (e: WindowManager.BadTokenException) {
            Log.e("DialogError", "Cannot show dialog: ${e.message}")
        } catch (e: Exception) {
            Log.e("DialogError", "Error showing dialog: ${e.message}")
        }
    }

    override fun onDestroy() {
        dialog?.dismiss()
        dialog = null
        dialogBinding = null
        super.onDestroy()
    }

    fun View.setGradientBackground(
        color: Int,
        strokeWidth: Int = 0,
        strokeColor: Int = android.graphics.Color.TRANSPARENT,
        cornerRadius: Float = 0f
    ) {
        val gradientDrawable = GradientDrawable().apply {
            setColor(color)
            if (strokeWidth > 0) setStroke(strokeWidth, strokeColor)
            if (cornerRadius > 0) this.cornerRadius = cornerRadius
        }
        background = gradientDrawable
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.let {
            isPlaying = it.isPlaying
            it.release()
        }
        mediaPlayer = null
    }

    companion object {
        private const val TAG = "MagicLines"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
    { permissions ->
        var permissionGranted = true
        permissions.entries.forEach {
            if (it.key in REQUIRED_PERMISSIONS && !it.value)
                permissionGranted = false
        }
        if (!permissionGranted) {
            Toast.makeText(baseContext, "Permission request denied", Toast.LENGTH_SHORT).show()
        } else {
            val drawable = resources.getDrawable(myList!![position].getResourceId()!!)
            val bitmap = drawableToBitmap(drawable)
            saveBitmapToGallery(bitmap)
        }
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    fun saveBitmapToGallery(bitmap: Bitmap) {
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MagicLines")
            }
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        uri?.let { imageUri ->
            contentResolver.openOutputStream(imageUri).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream!!)
                Toast.makeText(this, "Saved to Gallery", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareDrawable(drawable: Drawable) {
        val bitmap = drawableToBitmap(drawable)

        val cachePath = File(cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "shared_image.png")

        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }

        val contentUri: Uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "image/png"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share image"))
    }
}
