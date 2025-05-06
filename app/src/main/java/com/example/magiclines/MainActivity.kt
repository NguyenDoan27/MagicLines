package com.example.magiclines

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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
import kotlin.collections.arrayListOf
import androidx.core.graphics.drawable.toDrawable
import java.util.Calendar

class MainActivity :  BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private var levelAdapter: LevelPlayerAdapter? = null
    private lateinit var dataStore: SettingDataStore
    private var energyDialogBinding: AddEnergyDialogBinding? = null
    private var dialog: Dialog? = null
    private var levels = arrayListOf<Level>(
        Level(1, R.drawable.heart, false),
        Level(2, R.drawable.circle_bolt, false),
        Level(3, R.drawable.guitar, false),
        Level(4, R.drawable.donus, false)
    )
    private var energy: Int? = null

    private lateinit var viewModel: EnergyViewModel
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[EnergyViewModel::class.java]
        viewModel.energy.observe(this) { energy ->
            updateEnergyView(energy)
        }
        dataStore = SettingDataStore(this@MainActivity)

        binding.imgSetting.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingActivity::class.java)
            startActivity(intent)
        }

        scheduleEnergyWork()

        levelAdapter = LevelPlayerAdapter(this@MainActivity, levels, object : IOnClick {
            override fun onclick(position: Int) {
                lifecycleScope.launch {
                    val newEnergy = energy!! - 1
                    if (newEnergy < 5){
                        scheduleEnergyWork()
                    }
                    dataStore.saveEnergy(newEnergy)
                }
                val intent = Intent(this@MainActivity, PlayingActivity::class.java)
                intent.putExtra("levels", levels)
                intent.putExtra("position", position)
                startActivity(intent)
            }
        })

        binding.rcvLevelPlayer.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2, LinearLayoutManager.VERTICAL, false)
            adapter = levelAdapter
        }

        binding.tvPower.setOnClickListener {
            showEnergyDialog()
        }

        lifecycleScope.launch {
            combine (
                dataStore.energy,
                dataStore.levelsFlow
            ) {
                value, levels ->
                Pair(value,levels)
            }.collect { (value, newLevel) ->
                energy = value
                if (newLevel.size < levels.size){

                }else{
                    levels.clear()
                    levels.addAll(newLevel)
                    levelAdapter!!.notifyDataSetChanged()
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

     fun updateEnergyView(energy: Int) {
        binding.tvPower.text = "$energy"
    }

    private fun showEnergyDialog(){
        if (isFinishing || isDestroyed) return

        dialog?.dismiss()
        energyDialogBinding = null

        try {
            dialog = Dialog(this@MainActivity).apply {
                energyDialogBinding = AddEnergyDialogBinding.inflate(layoutInflater)
                setContentView(energyDialogBinding?.root ?: return@apply)
                val currentDate = Date()
                window?.setLayout(
                    550,  // or specific width in pixels
                    400   // or specific height
                )
                window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
               lifecycleScope.launch {
                   val savedDate = dataStore.date.first()
                   val currentDate = Date()

                   energyDialogBinding!!.btnGetEnergy.isEnabled =
                       !(savedDate != null && isSameDay(savedDate, currentDate))
               }
                energyDialogBinding!!.btnGetEnergy.setOnClickListener {
                    lifecycleScope.launch {
                        dataStore.saveEnergy(energy!! + 1)
                        dataStore.saveDate(currentDate)
                    }
                    energyDialogBinding!!.btnGetEnergy.isEnabled = false
                }
                energyDialogBinding!!.btnLoadAds.setOnClickListener {
                    Toast.makeText(this@MainActivity, "Load Ads", Toast.LENGTH_SHORT).show()
                }
                setOnDismissListener {
                    energyDialogBinding = null
                }
                show()
            }
        }catch (_:Exception){

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