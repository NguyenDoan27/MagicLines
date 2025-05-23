package com.example.magiclines.ui.selectionLevel

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.magiclines.R
import com.example.magiclines.common.adapter.CategoryAdapter
import com.example.magiclines.common.adapter.LevelPlayerAdapter2
import com.example.magiclines.base.BaseFragment
import com.example.magiclines.data.EnergyWorker
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.databinding.AddEnergyDialogBinding
import com.example.magiclines.databinding.FragmentSelectionLevelBinding
import com.example.magiclines.models.Level
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class SelectLevelFragment : BaseFragment<FragmentSelectionLevelBinding, SelectLevelViewModel>(), LevelPlayerAdapter2.FilterListener {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSelectionLevelBinding
        get() = FragmentSelectionLevelBinding::inflate



//    private lateinit var viewModel: EnergyViewModel

    private var levelAdapter: LevelPlayerAdapter2? = null
    private var energyDialogBinding: AddEnergyDialogBinding? = null
    private var dialog: Dialog? = null
    private var levels: ArrayList<Level> = arrayListOf()
    private var energy: Int = 0

    private var categoryAdapter: CategoryAdapter? = null
    private var category = emptyList<String>()
    private var currentCategory: Int = 0
    private val viewModel: SelectLevelViewModel by lazy { SelectLevelViewModel(SettingDataStore(requireContext())) }
    private var isLoading = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = resources.getStringArray(R.array.category_name).toList()
        setupAdapters()
        setupCategoryAdapter()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        initViewModel()


        if (isLoading){
            Log.e("TAG", "onViewCreated: load", )
            viewModel.getDataFiltered(levelAdapter!!, currentCategory, "")
            isLoading = false
        }

        viewModel.filteredLevels.observe(viewLifecycleOwner) { filteredLevels ->
            levelAdapter!!.submitList(filteredLevels)
        }

        viewModel.currentCategory.observe(viewLifecycleOwner) { categoryIndex ->
            categoryAdapter!!.setCurrentCategory(categoryIndex)
        }
//        scheduleEnergyWork()
    }

    override fun onResume() {
        super.onResume()
        Log.e("TAG", "onResume: $currentCategory", )
        isLoading = true
        viewModel.getDataFiltered(levelAdapter!!, currentCategory, if (currentCategory == 0) "" else category[currentCategory])
    }

    override fun initViewBinding() {
        binding.apply {
//            tvPower.setOnClickListener {
//                showEnergyDialog()
//            }
            imgback.setOnClickListener {
                findNavController().popBackStack()
            }


        }

        binding.rcvLevelPlayer.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = levelAdapter
        }

        binding.rcvCategory.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }


    }

//    private fun initViewModel() {
//        viewModel = ViewModelProvider(this)[EnergyViewModel::class.java]
//        viewModel.energy.observe(viewLifecycleOwner) { energy ->
//            updateEnergyView(energy)
//        }
//    }


    private fun setupAdapters() {

        levelAdapter = LevelPlayerAdapter2(requireContext(), this) { position ->
            handleLevelClick(position)
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

//    fun scheduleEnergyWork() {
//        val initialWorkRequest = OneTimeWorkRequestBuilder<EnergyWorker>()
//            .setInitialDelay(1, TimeUnit.MINUTES)
//            .build()
//
//        WorkManager.getInstance(requireContext()).enqueueUniqueWork(
//            "EnergyWork",
//            ExistingWorkPolicy.KEEP,
//            initialWorkRequest
//        )
//    }

//    private fun updateEnergyView(energy: Int) {
//        binding.tvPower.text = energy.toString()
//    }

//    private fun showEnergyDialog() {
//        if (!isAdded || isDetached) return
//
//        try {
//            dialog?.dismiss()
//            energyDialogBinding = AddEnergyDialogBinding.inflate(layoutInflater)
//
//            dialog = Dialog(requireContext()).apply {
//                setContentView(energyDialogBinding?.root ?: return@apply)
//                window?.apply {
//                    setLayout(550, 400)
//                    setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
//                }
//
//                setupEnergyDialogButtons()
//                setOnDismissListener { energyDialogBinding = null }
//                show()
//            }
//        } catch (e: Exception) {
//            Log.e("MainActivity", "Error showing energy dialog", e)
//        }
//    }

//    private fun setupEnergyDialogButtons() {
//        lifecycleScope.launch {
//            val savedDate = dataStore.date.first()
//            val currentDate = Date()
//
//            energyDialogBinding?.btnGetEnergy?.isEnabled =
//                !(savedDate != null && isSameDay(savedDate, currentDate))
//        }
//
//        energyDialogBinding?.apply {
//            btnGetEnergy.setOnClickListener {
//                lifecycleScope.launch {
//                    dataStore.saveEnergy(energy + 1)
//                    dataStore.saveDate(Date())
//                }
//                btnGetEnergy.isEnabled = false
//            }
//            btnLoadAds.setOnClickListener {
//                Toast.makeText(requireContext(), "Load Ads", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    fun  navigationToPlaying(position: Int) {
        val action = SelectLevelFragmentDirections.actionSelectLevelFragmentToPlayingFragment(
            position,
            viewModel.filteredLevels.value.toTypedArray()
        )
        findNavController().navigate(action)
    }

    fun setupCategoryAdapter() {
        categoryAdapter =
            CategoryAdapter(requireContext(), currentCategory, category.toList()) { category, position ->
                currentCategory = position
                categoryAdapter!!.setCurrentCategory(position)
                viewModel.setCurrentCategory(position)
                if (position == 0) {
                    levelAdapter!!.filter?.filter("")
                } else {
                    levelAdapter!!.filter?.filter(category)
                }
            }
    }


    override fun onFilterApplied(filteredList: List<Level>) {
        viewModel.setFilteredLevels(filteredList)
    }
}