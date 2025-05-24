package com.example.magiclines.ui.collection

import android.os.Bundle
import android.util.Log
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
import com.example.magiclines.databinding.FragmentCollectionBinding
import com.example.magiclines.models.Level
import kotlin.random.Random

class CollectionFragment : BaseFragment<FragmentCollectionBinding, CollectionViewModel>(),
    LevelPlayerAdapter2.FilterListener {

    private lateinit var categoryAdapter: CategoryAdapter
    private var category = emptyList<String>()
    private var dataStore: SettingDataStore? = null
    private lateinit var levelAdapter: LevelPlayerAdapter2
    private val viewModel: CollectionViewModel by lazy { CollectionViewModel(SettingDataStore(requireContext())) }
    private var position = 0

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
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        category = resources.getStringArray(R.array.category_name).toList()

        categoryAdapter = CategoryAdapter(requireContext(), viewModel.currentCategory.value ?: 0, category) { category, position ->
            categoryAdapter.setCurrentCategory(position)
            viewModel.setCurrentCategory(position)

            this.position = position
        }

        levelAdapter = LevelPlayerAdapter2(requireContext(), this) { position ->
            val action = CollectionFragmentDirections.actionCollectionFragmentToShowCollectionFragment(viewModel.filteredLevels.value!![position])
            findNavController().navigate(action)
        }

        dataStore = SettingDataStore(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getDataFiltered()

        viewModel.filteredLevels.observe(viewLifecycleOwner) { filteredLevels ->
            levelAdapter.setItems(filteredLevels)
            if (filteredLevels.isEmpty()){
                binding.llNoCollection.visibility = View.VISIBLE
                binding.rcvLevelPlayer.visibility = View.GONE
            }else{
                binding.llNoCollection.visibility = View.GONE
                binding.rcvLevelPlayer.visibility = View.VISIBLE
            }
        }

        viewModel.currentCategory.observe(viewLifecycleOwner) { categoryIndex ->
            categoryAdapter.setCurrentCategory(categoryIndex)
            levelAdapter.filter?.filter(if (categoryIndex == 0) "" else category[categoryIndex])
        }

    }

    override fun onResume() {
        super.onResume()
    }


    override fun onFilterApplied(filteredList: List<Level>) {
        val isEmpty = filteredList.isEmpty()
        binding.llNoCollection.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rcvLevelPlayer.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

}