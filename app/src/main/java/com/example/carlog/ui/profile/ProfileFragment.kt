package com.example.carlog.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.carlog.R
import com.example.carlog.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        onClicks()
        return binding.root
    }

    private fun onClicks() {
        binding.btnHome.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_profileFragment_to_homeFragment)
        }
        binding.btnMessage.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_profileFragment_to_chatsFragment)
        }
    }
}