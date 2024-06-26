package com.example.carlog.ui.profile

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.carlog.R
import com.example.carlog.databinding.FragmentProfileBinding
import com.example.carlog.utils.MySharedPreferences

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        onClicks()
        getData()
        return binding.root
    }

    private fun getData() {
        binding.name.text = MySharedPreferences.getUserName()
        binding.userAddress.text = MySharedPreferences.getUserAddress()
    }

    private fun onClicks() {
        binding.btnHome.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_profileFragment_to_homeFragment)
        }
        binding.btnMessage.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_profileFragment_to_chatsFragment)
        }
        binding.historyTextview.setOnClickListener{
            Navigation.findNavController(it).navigate(R.id.action_profileFragment_to_historyFragment)
        }
        binding.btnLogout.setOnClickListener{
            binding.loading.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                MySharedPreferences.clearUserData()
                Navigation.findNavController(it).navigate(R.id.action_profileFragment_to_loginFragment)
                binding.loading.visibility = View.GONE
            }, 2000)

        }
        binding.btnTerms.setOnClickListener{
            Navigation.findNavController(it).navigate(R.id.action_profileFragment_to_termsFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}