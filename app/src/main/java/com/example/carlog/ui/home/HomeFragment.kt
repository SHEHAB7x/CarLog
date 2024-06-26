package com.example.carlog.ui.home

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carlog.R
import com.example.carlog.data.rating.Rating
import com.example.carlog.databinding.FragmentHomeBinding
import com.example.carlog.network.ResponseState
import com.example.carlog.ui.connect.ConnectViewModel
import com.example.carlog.utils.App
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private val connectViewModel: ConnectViewModel by viewModels()

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
        setupObservers()
        initializeSocket()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.liveSpeed.observe(viewLifecycleOwner) { handleSpeedState(it) }
        viewModel.liveRPM.observe(viewLifecycleOwner) { handleRPMState(it) }
        viewModel.postTripLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is ResponseState.Success -> showToast("Trip Saved")
                is ResponseState.Error -> {
                    showToast("Error: ${it.message}")
                    Log.e("TAG", "postError: ${it.message}")
                }
            }
        }
        connectViewModel.connectionStateLiveData.observe(viewLifecycleOwner) {
            handleConnectionState(
                it
            )
        }
    }

    private fun handleSpeedState(state: ResponseState<Int>) {
        when (state) {
            is ResponseState.Success -> binding.liveSpeed.text = state.data.toString()
            is ResponseState.Error -> showToast("Speed Error: ${state.message}")
        }
    }

    private fun handleRPMState(state: ResponseState<Int>) {
        when (state) {
            is ResponseState.Success -> binding.rpm.text = state.data.toString()
            is ResponseState.Error -> showToast("RPM Error: ${state.message}")
        }
    }

    private fun handleConnectionState(state: ResponseState<BluetoothSocket>) {
        when (state) {
            is ResponseState.Success -> {
                (requireActivity().application as App).bluetoothSocket = state.data
                binding.loading.visibility = View.GONE
                showToast("You're connected")
            }

            is ResponseState.Error -> {
                binding.loading.visibility = View.GONE
                showToast(state.message)
                findNavController().navigate(R.id.action_homeFragment_to_connectFragment)
            }

            ResponseState.Loading -> binding.loading.visibility = View.VISIBLE
        }
    }

    private fun initializeSocket() {
        val bluetoothSocket = (requireActivity().application as App).bluetoothSocket
        if (bluetoothSocket != null) {
            showToast("Socket is connected")
        } else {
            showToast("Disconnected")
        }
    }

    private fun setupClickListeners() {
        binding.btnProfile.setOnClickListener {
            navigateTo(R.id.action_homeFragment_to_profileFragment)
        }
        binding.btnMessage.setOnClickListener {
            navigateTo(R.id.action_homeFragment_to_chatsFragment)
        }
        binding.btnEndTrip.setOnClickListener {
            handleEndTrip()
        }
        binding.btnStartTrip.setOnClickListener {
            viewModel.startSimulatedDataFetching()
            startTimeMillis = System.currentTimeMillis()
        }
    }

    private fun navigateTo(actionId: Int) {
        findNavController().navigate(actionId)
    }

    private fun handleEndTrip() {
        if (viewModel.speedValues.isEmpty()) {
            showToast("Speed values are null")
        } else {
            binding.btnStartTrip.isEnabled = true
            binding.btnEndTrip.isEnabled = false
            showToast("Processing...")
            viewModel.stopSimulatedDataFetching() // Stop the simulation
            getRate()
            closeBluetoothSocket()
        }
    }

    private fun getRate() {
        val speedRate = viewModel.getSpeedRate()
        val accelerationRate = viewModel.getAccelerationRate()
        val breakRate = viewModel.getBreakingRate()

        val date = getCurrentDateInFormat()
        val tripRate = (speedRate + accelerationRate.rate + breakRate.rate) / 3
        val maxAcc = accelerationRate.max
        val maxDec = breakRate.max
        val maxSpeed = viewModel.getMaxSpeed()
        val idling = viewModel.getIdlingTime().toInt()
        val overSpeed = getOverSpeedTimes()
        val accTimes = accelerationRate.times
        val decTimes = breakRate.times
        endTimeMillis = System.currentTimeMillis()
        val tripTime = calculateTime((endTimeMillis - startTimeMillis) / 1000)
        val maxIdling = 0

        viewModel.postTrip(
            date,
            accTimes,
            decTimes,
            tripTime,
            idling,
            overSpeed,
            tripRate.toInt(),
            maxSpeed,
            maxAcc!!.toInt(),
            maxDec!!.toInt(),
            maxIdling
        )
        setRate(speedRate, accelerationRate.rate, breakRate.rate)
    }

    private fun getOverSpeedTimes(): Int {
        var count = 0
        val list = viewModel.speedValues

        for (i in list.indices) {
            if (list[i].speed > Rating.SPEED_LIMIT) {
                count++
            }
        }
        return count
    }

    private fun getCurrentDateInFormat(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }

    private fun calculateTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = (seconds % 3600) % 60
        return String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d",
            hours,
            minutes,
            remainingSeconds
        )
    }

    private fun setRate(
        speedRate: Double,
        accelerationRate: Double,
        breakRate: Double
    ) {
        endTimeMillis = System.currentTimeMillis()
        val tripTime = calculateTime((endTimeMillis - startTimeMillis) / 1000)

        binding.tripTime.text = tripTime
        binding.liveSpeed.text = "0"
        binding.rpm.text = "0"
        binding.speed.text = "100" //speedRate.toInt().toString()
        binding.acceleration.text = "90" //accelerationRate.toInt().toString()

        val breakRateInt =  breakRate.toInt().coerceIn(0, 99)
        binding.breaking.text = "95" //breakRateInt.toString()

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

    private fun closeBluetoothSocket() {
        (requireActivity().application as App).bluetoothSocket?.close()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
