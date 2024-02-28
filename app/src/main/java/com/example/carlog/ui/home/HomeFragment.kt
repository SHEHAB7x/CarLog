package com.example.carlog.ui.home

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.example.carlog.R
import com.example.carlog.databinding.FragmentHomeBinding
import com.example.carlog.network.ResponseState
import com.example.carlog.ui.connect.ConnectViewModel
import com.example.carlog.utils.App
import com.example.carlog.utils.MyBluetoothManager
import com.example.carlog.utils.MyBluetoothSocket
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeSocket()
    }

    private fun observers() {
        viewModel.liveSpeed.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResponseState.Success -> {
                    binding.speed.text = state.data.toString()
                }

                is ResponseState.Error -> {
                    binding.speed.text = state.message
                }
                ResponseState.Loading -> binding.loading.visibility = View.VISIBLE
                else -> binding.loading.visibility = View.GONE
            }
        }
    }

    private fun initializeSocket(){
        val myApp = activity?.application as App
        val bluetoothSocket = myApp.bluetoothSocket

        if (bluetoothSocket != null) {
            viewModel.getSpeed(bluetoothSocket)
            observers()
            onClicks()
        } else {
            Navigation.findNavController(binding.root).navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }

    private fun onClicks() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}