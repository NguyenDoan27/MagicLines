package com.example.magiclines.ui.selectionLevel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.magiclines.R
import com.example.magiclines.common.adapter.CategoryAdapter
import com.example.magiclines.common.adapter.LevelPlayerAdapter2
import com.example.magiclines.base.BaseFragment
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.databinding.FragmentSelectionLevelBinding
import com.example.magiclines.models.Level

class SelectLevelFragment : BaseFragment<FragmentSelectionLevelBinding, SelectLevelViewModel>(), LevelPlayerAdapter2.FilterListener {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSelectionLevelBinding
        get() = FragmentSelectionLevelBinding::inflate

    private var levelAdapter: LevelPlayerAdapter2? = null

    private var categoryAdapter: CategoryAdapter? = null
    private var category = emptyList<String>()
    private var currentCategory: Int = 0
    private val viewModel: SelectLevelViewModel by lazy { SelectLevelViewModel(SettingDataStore(requireContext())) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = resources.getStringArray(R.array.category_name).toList()
        setupAdapters()
        setupCategoryAdapter()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getDataOriginal()

        viewModel.levels.observe(viewLifecycleOwner) { filteredLevels ->
            levelAdapter?.setItems(filteredLevels)
        }

        viewModel.currentCategory.observe(viewLifecycleOwner) { categoryIndex ->
            categoryAdapter!!.setCurrentCategory(categoryIndex)
            levelAdapter?.filter?.filter(if (categoryIndex == 0) "" else category[categoryIndex])
        }
    }

    override fun initViewBinding() {
        binding.apply {
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


    private fun setupAdapters() {

        levelAdapter = LevelPlayerAdapter2(requireContext(), this) { position ->
            handleLevelClick(position)
        }

    }


    private fun handleLevelClick(position: Int) {
        navigationToPlaying(position)
    }


    fun  navigationToPlaying(position: Int) {
        val action = SelectLevelFragmentDirections.actionSelectLevelFragmentToPlayingFragment(
            position,
            levelAdapter?.getItemsFiltered()!!.toTypedArray()
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

    }
}