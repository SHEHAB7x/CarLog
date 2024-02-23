package com.example.carlog.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.carlog.R
import com.example.carlog.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding : FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        onClicks()
        return binding.root
    }

    private fun onClicks() {
        binding.btnLogin.setOnClickListener{
            validate()
        }
    }

    private fun validate() {
        val email = binding.email.text
        val password = binding.password.text
        Navigation.findNavController(binding.root).navigate(R.id.action_loginFragment_to_connectFragment)
    }

}