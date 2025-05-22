package com.example.magiclines.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.magiclines.adapters.CategoryAdapter
import com.example.magiclines.adapters.LevelPlayerAdapter2
import com.example.magiclines.data.SettingDataStore
import com.example.magiclines.databinding.FragmentCollectionBinding
import com.example.magiclines.models.Level
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class CollectionFragment : Fragment(), LevelPlayerAdapter2.FilterListener {

    private lateinit var binding: FragmentCollectionBinding
    private var categoryAdapter: CategoryAdapter? = null
    private val category = listOf<String>("All", "Animal", "Anime", "Kawaii")
    private var dataStore: SettingDataStore? = null
    private var levels = mutableListOf<Level>()
    private var currentCategory = 0

    private var levelAdapter: LevelPlayerAdapter2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        categoryAdapter = CategoryAdapter(requireContext(),currentCategory, category){category, position ->
            categoryAdapter!!.setCurrentCategory(position)
            if (position == 0) {
                levelAdapter!!.setOriginalItems()
                onResume()
            }else levelAdapter!!.filter?.filter(category)
        }

        levelAdapter = LevelPlayerAdapter2(requireContext(), this){position ->
            val action = CollectionFragmentDirections.actionCollectionFragmentToShowCollectionFragment(levels[position])
            findNavController().navigate(action)
        }

        dataStore = SettingDataStore(requireContext())

    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            levels.clear()
            val data = dataStore!!.levelsFlow.first()
            for (i in data){
                if (i.isComplete) levels.add(i)
            }
        }

        if (levelAdapter!!.getItemsFiltered().isEmpty()){
            binding.llNoCollection.visibility = View.VISIBLE
        }else{
            binding.llNoCollection.visibility = View.GONE
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    override fun onFilterApplied(filteredList: List<Level>) {
        if (filteredList.isEmpty()){
            binding.llNoCollection.visibility = View.VISIBLE
        }else{
            binding.llNoCollection.visibility = View.GONE
        }
    }

}