package com.example.magiclines

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.magiclines.adapters.LevelPlayerAdapter
import com.example.magiclines.data.EnergyWorker
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.databinding.ActivityMainBinding
import com.example.magiclines.databinding.AddEnergyDialogBinding
import com.example.magiclines.interfaces.IOnClick
import com.example.magiclines.models.Level
import com.example.magiclines.viewModels.EnergyViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit
import androidx.core.graphics.drawable.toDrawable
import java.util.Calendar

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dataStore: SettingDataStore
    private lateinit var viewModel: EnergyViewModel

    private var levelAdapter: LevelPlayerAdapter? = null
    private var energyDialogBinding: AddEnergyDialogBinding? = null
    private var dialog: Dialog? = null
    private var levels: ArrayList<Level> = arrayListOf()
    private var energy: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        initViewModel()
        initDataStore()
        setupAdapters()
        setupListeners()
        observeData()
        scheduleEnergyWork()
    }

    private fun setupView() {
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[EnergyViewModel::class.java]
        viewModel.energy.observe(this) { energy ->
            updateEnergyView(energy)
        }
    }

    private fun initDataStore() {
        dataStore = SettingDataStore(this)
        lifecycleScope.launch {
            dataStore.initData()
        }
    }

    private fun setupAdapters() {
        levelAdapter = LevelPlayerAdapter(this, levels, object : IOnClick {
            override fun onclick(position: Int) {
                handleLevelClick(position)
            }
        })

        binding.rcvLevelPlayer.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = levelAdapter
        }
    }

    private fun setupListeners() {
        binding.apply {
            imgSetting.setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingActivity::class.java))
            }
            tvPower.setOnClickListener {
                showEnergyDialog()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeData() {
        lifecycleScope.launch {
            combine(dataStore.energy, dataStore.levelsFlow) { energy, levels ->
                Pair(energy, levels)
            }.collect { (currentEnergy, currentLevels) ->
                energy = currentEnergy
                levels.clear()
                levels.addAll(currentLevels)
                levelAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun handleLevelClick(position: Int) {
        lifecycleScope.launch {
            val newEnergy = energy - 1
            if (newEnergy < 5) {
                scheduleEnergyWork()
            }

            if (newEnergy < 0) {
                showEnergyDialog()
            }else{
                dataStore.saveEnergy(newEnergy)

                Intent(this@MainActivity, PlayingActivity::class.java).apply {
                    putExtra("levels", levels)
                    putExtra("position", position)
                    startActivity(this)
                }
            }

        }
    }

    fun scheduleEnergyWork() {
        val initialWorkRequest = OneTimeWorkRequestBuilder<EnergyWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "EnergyWork",
            ExistingWorkPolicy.KEEP,
            initialWorkRequest
        )
    }

    private fun updateEnergyView(energy: Int) {
        binding.tvPower.text = energy.toString()
    }

    private fun showEnergyDialog() {
        if (isFinishing || isDestroyed) return

        try {
            dialog?.dismiss()
            energyDialogBinding = AddEnergyDialogBinding.inflate(layoutInflater)

            dialog = Dialog(this).apply {
                setContentView(energyDialogBinding?.root ?: return@apply)
                window?.apply {
                    setLayout(550, 400)
                    setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                }

                setupEnergyDialogButtons()
                setOnDismissListener { energyDialogBinding = null }
                show()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error showing energy dialog", e)
        }
    }

    private fun setupEnergyDialogButtons() {
        lifecycleScope.launch {
            val savedDate = dataStore.date.first()
            val currentDate = Date()

            energyDialogBinding?.btnGetEnergy?.isEnabled =
                !(savedDate != null && isSameDay(savedDate, currentDate))
        }

        energyDialogBinding?.apply {
            btnGetEnergy.setOnClickListener {
                lifecycleScope.launch {
                    dataStore.saveEnergy(energy + 1)
                    dataStore.saveDate(Date())
                }
                btnGetEnergy.isEnabled = false
            }
            btnLoadAds.setOnClickListener {
                Toast.makeText(this@MainActivity, "Load Ads", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }
}