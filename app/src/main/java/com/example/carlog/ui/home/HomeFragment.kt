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
            acceleration()
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

                val rate = viewModel.getRate()
                setRate(rate)

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
    private fun setRate(rate: Double) {
        binding.speed.text = rate.toString()
        if (rate < 50) {
            binding.speed.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.red_circle)
        } else if (rate > 85) {
            binding.speed.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.green_circle)
        } else {
            binding.speed.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.blue_circle)
        }
    }
        private fun acceleration() {
        val sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

                linearAcceleration[0] = event.values[0] - gravity[0]
                linearAcceleration[1] = event.values[1] - gravity[1]
                (event.values[2] - gravity[2]).also { linearAcceleration[2] = it }
                val accelerationMagnitude = sqrt(
                    (
                            linearAcceleration[0]
                                    * linearAcceleration[0]
                                    + linearAcceleration[1]
                                    * linearAcceleration[1]
                                    + linearAcceleration[2]
                                    * linearAcceleration[2]).toDouble()
                )
                binding.acceleration.text = accelerationMagnitude.toString()[0].toString()
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                // Optional: Handle accuracy changes if needed
            }
        }
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}