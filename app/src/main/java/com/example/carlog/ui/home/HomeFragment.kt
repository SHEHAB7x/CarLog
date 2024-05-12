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

    private val gravity = FloatArray(3)
    private val linearAcceleration = FloatArray(3)
    private val alpha = 0.8f
    private var startTimeMillis: Long = 0L
    private var endTimeMillis: Long = 0L

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
    }
    private fun initializeSocket() {
        val myApp = activity?.application as App
        val bluetoothSocket = myApp.bluetoothSocket
        if (bluetoothSocket != null) {
            Toast.makeText(requireContext(), "Socket is connected", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Disconnected", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getData(bluetoothSocket: BluetoothSocket?) {
        Toast.makeText(requireContext(), "Get Data", Toast.LENGTH_SHORT).show()
        if(bluetoothSocket == null){
            Toast.makeText(requireContext(), "Failed to get data, Reconnect", Toast.LENGTH_SHORT).show()
            Navigation.findNavController(binding.root).navigate(R.id.action_homeFragment_to_connectFragment)
        }else{
            //viewModel.getSpeed(bluetoothSocket)
            //viewModel.getRPM(bluetoothSocket)
            viewModel.getData(bluetoothSocket)
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
                binding.btnStartTrip.isEnabled = true
                binding.btnEndTrip.isEnabled = false

                Toast.makeText(requireContext(),"Processing...",Toast.LENGTH_SHORT).show()
                endTimeMillis = System.currentTimeMillis()
                val milliSeconds = endTimeMillis - startTimeMillis

                val seconds = milliSeconds / 1000
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val remainingSeconds = (seconds % 3600) % 60

                val tripTime = "$hours : $minutes : $remainingSeconds"
                binding.tripTime.text = tripTime

                getRate()

                val myApp = activity?.application as App
                myApp.bluetoothSocket?.close()
            }
        }

        binding.btnStartTrip.setOnClickListener {
            val myApp = activity?.application as App
            val bluetoothSocket = myApp.bluetoothSocket

            if (bluetoothSocket != null) {
                binding.btnStartTrip.isEnabled = false
                binding.btnEndTrip.isEnabled = true
                Toast.makeText(requireContext(), "The trip is Start", Toast.LENGTH_SHORT).show()
                startTimeMillis = System.currentTimeMillis()
                getData(bluetoothSocket)
            } else {
                Toast.makeText(requireContext(), "Null Socket please Reconnect", Toast.LENGTH_SHORT).show()
                Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_connectFragment)
            }
        }
    }

    private fun getRate() {
        val speedRate = viewModel.getSpeedRate()
        val accelerationRate = viewModel.getAccelerationRate()
        val breakRate = viewModel.getBreakingRate()
        setRate(speedRate, accelerationRate, breakRate)
    }

    private fun setRate(speedRate: Double, accelerationRate: Double, breakRate: Double) {
        binding.speed.text = speedRate.toString()
        binding.acceleration.text = accelerationRate.toString()
        binding.breaking.text = breakRate.toString()

        setRateBackground(binding.speed, speedRate)
        setRateBackground(binding.acceleration, accelerationRate)
        setRateBackground(binding.breaking, breakRate)
    }

    private fun setRateBackground(view: View, rate: Double) {
        val backgroundResId = when {
            rate < 50 -> R.drawable.red_circle
            rate > 85 -> R.drawable.green_circle
            else -> R.drawable.blue_circle
        }
        view.background = ContextCompat.getDrawable(requireContext(), backgroundResId)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}