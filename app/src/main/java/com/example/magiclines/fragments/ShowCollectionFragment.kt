package com.example.magiclines.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.databinding.DeleteArtDialogBinding
import com.example.magiclines.databinding.FragmentShowCollectionBinding
import com.example.magiclines.databinding.SelectLanguageDialogBinding
import com.example.magiclines.models.Level
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.getValue


class ShowCollectionFragment : Fragment() {

    private lateinit var binding: FragmentShowCollectionBinding
    private var level: Level? = null
    private val args: ShowCollectionFragmentArgs by navArgs()
    private val dataStore: SettingDataStore by lazy { SettingDataStore(requireContext()) }
    private var levels: List<Level> = listOf()
    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        level = args.level
        lifecycleScope.launch {
            levels = dataStore.levelsFlow.first()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShowCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivShowCollection.setImageResource(level!!.resourceId)
        binding.btnShare.setOnClickListener { handleShare() }
        binding.btnDownload.setOnClickListener { handleDownload() }
        binding.ivBack.setOnClickListener { findNavController().popBackStack() }
        binding.ivDelete.setOnClickListener { handleDelete() }
    }

    private fun handleDelete() { showDeleteDialog() }

    private fun showDeleteDialog() {
        if (isDetached || !isAdded) return
        try {
            dialog?.dismiss()
            val dialogBinding = DeleteArtDialogBinding.inflate(layoutInflater)

            dialog = Dialog(requireContext()).apply {
                setContentView(dialogBinding.root)
                window?.apply {
                    setLayout(550, LayoutParams.WRAP_CONTENT)
                    setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                }

                dialogBinding.btnKeep.setOnClickListener {
                    dialog?.dismiss()
                }
                dialogBinding.btnDelete.setOnClickListener {
                    for (i in levels){
                        if (i.numLevel == level!!.numLevel){
                            i.isComplete = false
                        }
                    }
                    lifecycleScope.launch {
                        dataStore.saveLevel(levels)
                    }
                    dialog!!.dismiss()
                    findNavController().popBackStack()
                }
                show()
            }
        }catch (_: Exception){
            Log.e("SettingActivity", "Error showing language dialog", )
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
    private fun handleShare() {
        shareDrawable(resources.getDrawable(level!!.resourceId))
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

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { handlePermissionResult(it) }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun saveLevelImage() {
        level!!.resourceId.let { resId ->
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

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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

    companion object {
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

}