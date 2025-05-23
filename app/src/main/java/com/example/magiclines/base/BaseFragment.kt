package com.example.magiclines.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<binding: ViewBinding, viewModel: BaseViewModel> : Fragment(){
    protected abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> binding
    private var _binding: binding? = null
    val binding get() = _binding!!

    protected abstract fun initViewBinding()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewBinding()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}