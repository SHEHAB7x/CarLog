package com.example.carlog.ui.home

import android.bluetooth.BluetoothSocket
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import kotlin.math.sqrt

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private val connectViewModel: ConnectViewModel by viewModels()

    private val gravity = FloatArray(3)
    private val linearAcceleration = FloatArray(3)
    private val alpha = 0.8f
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
            getData(bluetoothSocket)
            //acceleration()
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
            viewModel.getSpeed(bluetoothSocket)
            viewModel.getRPM(bluetoothSocket)
        }
    }
    private fun acceleration() {
        val sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // Isolate the force of gravity with the low-pass filter.
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

                // Remove the gravity contribution with the high-pass filter.
                linearAcceleration[0] = event.values[0] - gravity[0]
                linearAcceleration[1] = event.values[1] - gravity[1]
                (event.values[2] - gravity[2]).also { linearAcceleration[2] = it }

                // Calculate the magnitude of acceleration
                val accelerationMagnitude = sqrt(
                    (
                            linearAcceleration[0]
                                    * linearAcceleration[0]
                                    + linearAcceleration[1]
                                    * linearAcceleration[1]
                                    + linearAcceleration[2]
                                    * linearAcceleration[2]).toDouble()
                )

                // Display the magnitude of acceleration
                val accMagnitudeStr = "Acceleration Magnitude: $accelerationMagnitude"
                Toast.makeText(requireContext(), accMagnitudeStr, Toast.LENGTH_SHORT).show()
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                // Optional: Handle accuracy changes if needed
            }
        }
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
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
                speedRating(speedValues)
            }
        }
    }

    private fun speedRating(speedValues: List<Int>) {
        // Find the highest speed in the list
        val maxSpeed = speedValues.max()
        // Calculate the percentage increase
        val percentageIncrease = ((maxSpeed - 20) / 20.0) * 100
        // Subtract the percentage increase from 100
        val rateOfSpeed = 100 - percentageIncrease
        binding.speed.text = rateOfSpeed.toString()
        if(rateOfSpeed < 50){
            binding.speed.background = ContextCompat.getDrawable(requireContext(), R.drawable.red_circle)
        }else if(rateOfSpeed > 85){
            binding.speed.background = ContextCompat.getDrawable(requireContext(), R.drawable.green_circle)
        }else{
            binding.speed.background = ContextCompat.getDrawable(requireContext(), R.drawable.blue_circle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}