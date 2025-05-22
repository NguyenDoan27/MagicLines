package com.example.magiclines.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.magiclines.base.BaseFragment
import com.example.magiclines.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHomeBinding
        get() = FragmentHomeBinding::inflate

    override fun initViewBinding() {
        binding.imgSetting.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToSettingFragment()
            findNavController().navigate(action)
        }

        binding.layoutGraft.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToSelectionLevelFragment()
            findNavController().navigate(action)
        }

        binding.layoutGallery.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToCollectionFragment()
            findNavController().navigate(action)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


}