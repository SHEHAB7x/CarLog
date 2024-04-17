package com.example.carlog.ui.home

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.carlog.R
import com.example.carlog.databinding.FragmentHomeBinding
import com.example.carlog.network.ResponseState
import com.example.carlog.ui.connect.ConnectViewModel
import com.example.carlog.utils.App
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private val connectViewModel: ConnectViewModel by viewModels()
    var values = arrayOfNulls<Int>(10000)
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
                    binding.speedValue.text = state.data.toString()
                    binding.speedRawData.text = "Success"

                    binding.speedValue.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.green
                        )
                    )
                }

                is ResponseState.Error -> {
                    binding.speedValue.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red
                        )
                    )
                    binding.speedValue.text = "Error"
                    binding.speedRawData.text = state.message
                }

                else -> {
                    binding.speedValue.text = "Else"
                    binding.speedRawData.text = "Else"
                }
            }
        }

        viewModel.liveRPM.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResponseState.Success -> {
                    binding.rpmValue.text = state.data.toString()
                    binding.rpmRawData.text = "Success"

                    binding.rpmValue.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.green
                        )
                    )
                }

                is ResponseState.Error -> {
                    binding.rpmValue.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red
                        )
                    )
                    binding.rpmValue.text = "Error"
                    binding.rpmRawData.text = state.message
                }

                else -> {
                    binding.rpmValue.text = "Else"
                    binding.rpmRawData.text = "Else"
                }
            }
        }

    }

    private fun initializeSocket() {
        val myApp = activity?.application as App
        val bluetoothSocket = myApp.bluetoothSocket
        if (bluetoothSocket != null) {
            Toast.makeText(requireContext(), "Socket is connected", Toast.LENGTH_SHORT).show()
            getData(bluetoothSocket)
        } else {
            Toast.makeText(requireContext(), "Noo Socket", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getData(bluetoothSocket: BluetoothSocket?) {
        Toast.makeText(requireContext(), "Get Data", Toast.LENGTH_SHORT).show()
        if(bluetoothSocket == null){
            Toast.makeText(requireContext(), "Bluetooth socket is null!", Toast.LENGTH_SHORT).show()
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
        binding.btnEnd.setOnClickListener {
            val speedValues = viewModel.speedValues
            displaySpeedValues(speedValues)
        }
    }
    private fun displaySpeedValues(speedValues: List<Int>) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            for (speedValue in speedValues) {
                binding.allValues.text = speedValue.toString()
                delay(500)
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}