package com.example.magiclines.ui.collection

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.magiclines.common.adapter.CategoryAdapter
import com.example.magiclines.common.adapter.LevelPlayerAdapter2
import com.example.magiclines.base.BaseFragment
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.databinding.FragmentCollectionBinding
import com.example.magiclines.models.Level
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CollectionFragment : BaseFragment<FragmentCollectionBinding, CollectionViewModel>(),
    LevelPlayerAdapter2.FilterListener {

    private var categoryAdapter: CategoryAdapter? = null
    private val category = listOf("All", "Anime", "Animal", "Kawaii")
    private var dataStore: SettingDataStore? = null
    private var levels = mutableListOf<Level>()
    private var currentCategory = 0
    private var levelAdapter: LevelPlayerAdapter2? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCollectionBinding
        get() = FragmentCollectionBinding::inflate

    override fun initViewBinding() {
        binding.imgBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.rcvCategory.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
        binding.rcvLevelPlayer.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = levelAdapter
            levelAdapter!!.setItems(levels)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        categoryAdapter = CategoryAdapter(requireContext(), currentCategory, category) { category, position ->
            categoryAdapter!!.setCurrentCategory(position)
            if (position == 0) {
                levelAdapter!!.setOriginalItems()
                levelAdapter!!.filter?.filter("")
            } else {
                levelAdapter!!.filter?.filter(category)
            }
        }

        levelAdapter = LevelPlayerAdapter2(requireContext(), this) { position ->
            val action = CollectionFragmentDirections.actionCollectionFragmentToShowCollectionFragment(
                levelAdapter!!.getItemsFiltered()[position])
            findNavController().navigate(action)
        }

        dataStore = SettingDataStore(requireContext())
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            levels.clear()
            val data = dataStore!!.levelsFlow.first()
            for (i in data) {
                if (i.isComplete) levels.add(i)
            }
            levelAdapter?.setItems(levels)
            updateNoCollectionVisibility()
        }
        categoryAdapter?.setCurrentCategory(currentCategory)
    }

    override fun onFilterApplied(filteredList: List<Level>) {
        updateNoCollectionVisibility()
    }

    private fun updateNoCollectionVisibility() {
        if (levelAdapter!!.getItemsFiltered().isEmpty()) {
            binding.llNoCollection.visibility = View.VISIBLE
        } else {
            binding.llNoCollection.visibility = View.GONE
        }
    }
}