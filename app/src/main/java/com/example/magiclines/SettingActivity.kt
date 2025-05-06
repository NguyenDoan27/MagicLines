package com.example.magiclines

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.magiclines.databinding.ActivitySettingBinding
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.databinding.SelectLanguageDialogBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SettingActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingBinding
    private var dialogBinding: SelectLanguageDialogBinding? = null
    private var dialog: Dialog? = null
    private var languageCode: String? = null
    private var dataStore: SettingDataStore? = null
    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        dataStore = SettingDataStore(applicationContext)
        binding.layoutContact.setOnClickListener {
            val emailUri = "mailto:doannguyen22702@gmail.com" +
                    "?subject=" + Uri.encode("Report my app")

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = emailUri.toUri()
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "no app support to send mail", Toast.LENGTH_SHORT).show()
            }
        }

        binding.layoutRate.setOnClickListener {
            Toast.makeText(this@SettingActivity, "Rated 5 star", Toast.LENGTH_SHORT).show()
        }

        binding.layoutShare.setOnClickListener {
            val textToShare = "I would like invite you to download this app\n" +
                    "\n" +
                    "https://play.google.com/store/apps/details?id=com.draw.drawing.glow.dot.art\n"

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, textToShare)
            }

            startActivity(Intent.createChooser(shareIntent, "share"))
        }

        binding.layoutLanguages.setOnClickListener {
            showLanguageDialog()
        }
    }

    private fun showLanguageDialog(){
        if(isFinishing || isDestroyed) return
        dialog?.dismiss()
        dialogBinding = null

        try {
            dialog = Dialog(this@SettingActivity).apply {
                dialogBinding = SelectLanguageDialogBinding.inflate(layoutInflater)
                setContentView(dialogBinding?.root ?: return@apply)
                window?.setLayout(
                    550,
                    LayoutParams.WRAP_CONTENT
                )
                window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                runBlocking { launch {
                    languageCode = dataStore!!.language.first()
                } }

                when(languageCode){
                    "vi" -> dialogBinding!!.layoutVietnamese.isEnabled = false
                    "en" -> dialogBinding!!.layoutEnglish.isEnabled = false
                }
                dialogBinding!!.layoutVietnamese.setOnClickListener {
                    dialogBinding!!.layoutEnglish.isEnabled = true
                    dialogBinding!!.layoutVietnamese.isEnabled = false
                    lifecycleScope.launch {
                        dataStore!!.saveLanguage("vi")
                    }
                }

                dialogBinding!!.layoutEnglish.setOnClickListener {
                    dialogBinding!!.layoutEnglish.isEnabled = false
                    dialogBinding!!.layoutVietnamese.isEnabled = true
                    lifecycleScope.launch {
                        dataStore!!.saveLanguage("en")
                    }
                }

                show()

            }
        }catch (_: Exception){

        }
    }
}