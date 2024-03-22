package com.example.carlog.ui.chats

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.carlog.R
import com.example.carlog.databinding.FragmentChatsBinding
import com.example.carlog.databinding.FragmentProfileBinding


class ChatsFragment : Fragment() {
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        onClicks()
        return binding.root
    }

    private fun onClicks() {
        binding.btnHome.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_chatsFragment_to_homeFragment)
        }
        binding.btnProfile.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_chatsFragment_to_profileFragment)
        }
    }

}