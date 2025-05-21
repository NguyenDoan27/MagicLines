package com.example.magiclines.fragments

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.magiclines.databinding.FragmentHomeBinding
import androidx.core.graphics.toColorInt
import androidx.navigation.fragment.findNavController


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.layoutGraft.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToSelectionLevelFragment()
            findNavController().navigate(action)
        }

        binding.layoutGallery.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToCollectionFragment()
            findNavController().navigate(action)
        }
        binding.imgSetting.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToSettingFragment()
            findNavController().navigate(action)
        }
    }

}