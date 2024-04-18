package com.example.carlog.ui.home

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.example.carlog.R
import com.example.carlog.databinding.FragmentHomeBinding
import com.example.carlog.network.ResponseState
import com.example.carlog.ui.connect.ConnectViewModel
import com.example.carlog.utils.App
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private val connectViewModel: ConnectViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClicks()
        initializeSocket()
        observers()
    }

    private fun observers() {
        connectViewModel.connectionStateLiveData.observe(viewLifecycleOwner) { socket ->
            when (socket) {
                is ResponseState.Success -> {
                    (activity?.application as App).bluetoothSocket = socket.data
                    binding.loading.visibility = View.GONE
                    Toast.makeText(requireContext(), "You're connected", Toast.LENGTH_SHORT).show()
                }

                is ResponseState.Error -> {
                    binding.loading.visibility = View.GONE
                    Toast.makeText(requireContext(), socket.message, Toast.LENGTH_SHORT).show()
                    Navigation.findNavController(binding.root)
                        .navigate(R.id.action_homeFragment_to_connectFragment)
                }

                ResponseState.Loading -> binding.loading.visibility = View.VISIBLE
            }
        }

        viewModel.liveSpeed.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResponseState.Success -> {
                    binding.liveSpeed.text = state.data.toString()
                }
                is ResponseState.Error -> {
                    Toast.makeText(requireContext(), "Speed Error: ${state.message}", Toast.LENGTH_SHORT).show()
                }

            }
        }

        viewModel.liveRPM.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResponseState.Success -> {
                    binding.rpm.text = state.data.toString()
                }
                is ResponseState.Error -> {
                    Toast.makeText(requireContext(), "Rpm Error: ${state.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initializeSocket() {
        val myApp = activity?.application as App
        val bluetoothSocket = myApp.bluetoothSocket
        if (bluetoothSocket != null) {
            Toast.makeText(requireContext(), "Socket is connected", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Noo Socket", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getData(bluetoothSocket: BluetoothSocket?) {
        Toast.makeText(requireContext(), "Get Data", Toast.LENGTH_SHORT).show()
        if(bluetoothSocket == null){
            Toast.makeText(requireContext(), "Failed to get data, Reconnect", Toast.LENGTH_SHORT).show()
            Navigation.findNavController(binding.root).navigate(R.id.action_homeFragment_to_connectFragment)
        }else{
            viewModel.getSpeed(bluetoothSocket)
            viewModel.getRPM(bluetoothSocket)
        }
    }
    private fun onClicks() {
        binding.btnProfile.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_profileFragment)
        }
        binding.btnMessage.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_chatsFragment)
        }
        binding.btnEndTrip.setOnClickListener {
            val speedValues = viewModel.speedValues
            if(speedValues.isEmpty()){
                Toast.makeText(requireContext(),"Speed values are null",Toast.LENGTH_SHORT).show()
            }else{
                // Processing speed values and then display the result of rating
                Toast.makeText(requireContext(),"Processing...",Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnStartTrip.setOnClickListener {
            val myApp = activity?.application as App
            val bluetoothSocket = myApp.bluetoothSocket
            if (bluetoothSocket != null) {
                Toast.makeText(requireContext(), "The trip is Start", Toast.LENGTH_SHORT).show()
                getData(bluetoothSocket)
            } else {
                Toast.makeText(requireContext(), "Null Socket please Reconnect", Toast.LENGTH_SHORT).show()
                Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_connectFragment)
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}