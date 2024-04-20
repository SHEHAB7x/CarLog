package com.example.carlog.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.example.carlog.R
import com.example.carlog.data.ModelUser
import com.example.carlog.databinding.FragmentLoginBinding
import com.example.carlog.network.ResponseState
import com.example.carlog.utils.MySharedPreferences
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding : FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        onClicks()
        observers()
        return binding.root
    }

    private fun observers() {
        loginViewModel.loginLiveData.observe(viewLifecycleOwner){ state ->
            when (state) {
                is ResponseState.Success -> navigateToHome(state.data)
                is ResponseState.Error -> showError(state.message)
                ResponseState.Loading -> showLoadingIndicator()
            }
        }
    }

    private fun showLoadingIndicator() {
        binding.loading.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        binding.loading.visibility = View.GONE
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToHome(user: ModelUser) {
        binding.loading.visibility = View.GONE
        cacheUserData(user)
        Navigation.findNavController(binding.root).navigate(R.id.action_loginFragment_to_connectFragment)
    }

    private fun cacheUserData(user: ModelUser) {
        MySharedPreferences.setUserEmail(user.email)
    }

    private fun onClicks() {
        binding.btnLogin.setOnClickListener{
            validate()
        }
    }

    private fun validate() {
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        if (email.isEmpty())
            Toast.makeText(requireContext(),"Username require!", Toast.LENGTH_SHORT).show()
        else if (password.isEmpty())
            Toast.makeText(requireContext(),"Password require!", Toast.LENGTH_SHORT).show()
        else {
            loginViewModel.loginUser(email, password)
        }
    }

}