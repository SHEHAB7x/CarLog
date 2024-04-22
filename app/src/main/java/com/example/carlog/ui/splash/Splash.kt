package com.example.carlog.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.carlog.R
import com.example.carlog.databinding.FragmentSplashBinding
import com.example.carlog.utils.MySharedPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Splash : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        navigateToNextScreenWithDelay()
        return binding.root
    }

    private fun navigateToNextScreenWithDelay() {
        lifecycleScope.launch {
            delay(2000)
            when(MySharedPreferences.getUserId()){
                0 -> Navigation.findNavController(binding.root).navigate(R.id.action_splash_to_loginFragment)
                else -> Navigation.findNavController(binding.root).navigate(R.id.action_splash_to_connectFragment)
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}