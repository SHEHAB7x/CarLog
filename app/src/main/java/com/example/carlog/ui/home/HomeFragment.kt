package com.example.carlog.ui.home

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
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

    private val connectViewModel : ConnectViewModel by viewModels()


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
        observers()
        initializeSocket()
    }

    private fun observers() {
        connectViewModel.connectionStateLiveData.observe(viewLifecycleOwner){ socket ->
            when(socket){
                is ResponseState.Success -> {
                    (activity?.application as App).bluetoothSocket = socket.data
                    binding.loading.visibility = View.GONE
                    Toast.makeText(requireContext(),"You're connected",Toast.LENGTH_SHORT).show()
                }
                is ResponseState.Error ->{
                    binding.loading.visibility = View.GONE
                    Toast.makeText(requireContext(),socket.message,Toast.LENGTH_SHORT).show()
                    Navigation.findNavController(binding.root).navigate(R.id.action_homeFragment_to_connectFragment)
                }
                ResponseState.Loading -> binding.loading.visibility = View.VISIBLE
            }
        }

        viewModel.liveSpeed.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResponseState.Success -> {
                    binding.speed.text = state.data.value
                    binding.rawData.text = state.data.unit
                    binding.rawData.visibility = View.VISIBLE
                    binding.socketStatus.setTextColor(ContextCompat.getColor(requireContext(),R.color.green))
                    binding.socketStatus.visibility = View.VISIBLE
                    binding.socketStatus.text = getString(R.string.success)
                    binding.idlingWord.text = state.data.rawResponse.toString()
                }
                is ResponseState.Error -> {
                    binding.socketStatus.visibility = View.VISIBLE
                    binding.socketStatus.text = state.message
                }
                ResponseState.Loading -> binding.loading.visibility = View.VISIBLE
                else -> binding.loading.visibility = View.GONE
            }
        }

        viewModel.liveRPM.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResponseState.Success -> {
                    binding.RPM.text = state.data.value
                }
                is ResponseState.Error -> {
                    binding.socketStatus.visibility = View.VISIBLE
                    binding.socketStatus.text = state.message
                }
                else -> binding.loading.visibility = View.GONE
            }
        }

    }

    private fun initializeSocket() {
        val myApp = activity?.application as App
        val bluetoothSocket = myApp.bluetoothSocket
        if (bluetoothSocket != null) {
            Toast.makeText(requireContext(),"Socket is connected",Toast.LENGTH_SHORT).show()
            binding.socketStatus.visibility = View.GONE
            getData(bluetoothSocket)
        } else {
            Toast.makeText(requireContext(),"Noo Socket",Toast.LENGTH_SHORT).show()
            binding.socketStatus.visibility = View.VISIBLE
        }
    }

    private fun getData(bluetoothSocket: BluetoothSocket?) {
        Toast.makeText(requireContext(),"Get Data",Toast.LENGTH_SHORT).show()
        viewModel.getSpeed(bluetoothSocket!!)
        viewModel.getRPM(bluetoothSocket)
    }

    private fun onClicks() {
        binding.btnProfile.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_profileFragment)
        }
        binding.btnMessage.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_chatsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}