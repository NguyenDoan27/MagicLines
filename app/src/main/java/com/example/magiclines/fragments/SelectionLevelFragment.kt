package com.example.magiclines.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.magiclines.adapters.CategoryAdapter
import com.example.magiclines.adapters.LevelPlayerAdapter2
import com.example.magiclines.data.EnergyWorker
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.databinding.AddEnergyDialogBinding
import com.example.magiclines.databinding.FragmentSelectionLevelBinding
import com.example.magiclines.models.Level
import com.example.magiclines.viewModels.EnergyViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit


class SelectionLevelFragment : Fragment(), LevelPlayerAdapter2.FilterListener {

    private lateinit var binding: FragmentSelectionLevelBinding
    private lateinit var dataStore: SettingDataStore
//    private lateinit var viewModel: EnergyViewModel

    private var levelAdapter: LevelPlayerAdapter2? = null
    private var energyDialogBinding: AddEnergyDialogBinding? = null
    private var dialog: Dialog? = null
    private var levels: ArrayList<Level> = arrayListOf()
    private var energy: Int = 0

    private var categoryAdapter: CategoryAdapter? = null
    private val category = listOf<String>("All", "Action", "Animal", "Symbol")
    private var currentCategory: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDataStore()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectionLevelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        initViewModel()
        setupAdapters()
        setupCategoryAdapter()
        setupListeners()
        observeData()
        scheduleEnergyWork()
    }

//    private fun initViewModel() {
//        viewModel = ViewModelProvider(this)[EnergyViewModel::class.java]
//        viewModel.energy.observe(viewLifecycleOwner) { energy ->
//            updateEnergyView(energy)
//        }
//    }

    private fun initDataStore() {
        dataStore = SettingDataStore(requireContext())
        lifecycleScope.launch {
            dataStore.initData()
        }
    }

    private fun setupAdapters() {

        levelAdapter = LevelPlayerAdapter2(requireContext(), this){position ->
            handleLevelClick(position)
        }

        binding.rcvLevelPlayer.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = levelAdapter
            levelAdapter!!.setItems(levels)
        }
    }

    private fun setupListeners() {
        binding.apply {
//            tvPower.setOnClickListener {
//                showEnergyDialog()
//            }
            imgSetting.setOnClickListener {
                findNavController().popBackStack()
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
        navigationToPlaying(position)
//        lifecycleScope.launch {
//            val newEnergy = energy - 1
//            if (newEnergy < 5) {
//                scheduleEnergyWork()
//            }
//
//            if (newEnergy < 0) {
//                showEnergyDialog()
//            }else{
//                dataStore.saveEnergy(newEnergy)
//
//            }
//
//        }
    }

    fun scheduleEnergyWork() {
        val initialWorkRequest = OneTimeWorkRequestBuilder<EnergyWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniqueWork(
            "EnergyWork",
            ExistingWorkPolicy.KEEP,
            initialWorkRequest
        )
    }

//    private fun updateEnergyView(energy: Int) {
//        binding.tvPower.text = energy.toString()
//    }

    private fun showEnergyDialog() {
        if (!isAdded || isDetached) return

        try {
            dialog?.dismiss()
            energyDialogBinding = AddEnergyDialogBinding.inflate(layoutInflater)

            dialog = Dialog(requireContext()).apply {
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
                Toast.makeText(requireContext(), "Load Ads", Toast.LENGTH_SHORT).show()
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

    fun  navigationToPlaying(position: Int) {
        val lvs = levelAdapter!!.getItemsFiltered().toTypedArray()
        val action = SelectionLevelFragmentDirections.actionSelectLevelFragmentToPlayingFragment(
            position,
            lvs
        )
        findNavController().navigate(action)
    }

    fun setupCategoryAdapter() {
        categoryAdapter = CategoryAdapter(requireContext(),currentCategory, category){category, position ->
            categoryAdapter!!.setCurrentCategory(position)
            if (position == 0) levelAdapter!!.setOriginalItems() else levelAdapter!!.filter?.filter(category)
        }
        binding.rcvCategory.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
    }

    override fun onFilterApplied(filteredList: List<Level>) {

    }


}