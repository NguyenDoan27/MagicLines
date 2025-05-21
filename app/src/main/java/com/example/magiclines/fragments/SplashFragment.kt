package com.example.magiclines.fragments


import android.animation.ObjectAnimator
import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.magiclines.R
import com.example.magiclines.databinding.FragmentSplashBinding
import androidx.core.graphics.drawable.toDrawable
import com.example.magiclines.data.BlurTransformation


class SplashFragment : Fragment() {

    private lateinit var binding: FragmentSplashBinding
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(Looper.getMainLooper())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.max = 1000
        ObjectAnimator.ofInt(binding.progressBar, "progress", 1000)
            .setDuration(1500)
            .start()

        handler.postDelayed({
            val navController = findNavController()
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.splashFragment, true)
                .build()

            val action = SplashFragmentDirections.actionSplashFragmentToHomeFragment()

            navController.navigate(action, navOptions)
        }, 1500)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }

}