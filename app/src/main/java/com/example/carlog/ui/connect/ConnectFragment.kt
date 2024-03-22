package com.example.carlog.ui.connect

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.carlog.R
import com.example.carlog.adapters.AdapterRecyclerDevices
import com.example.carlog.databinding.FragmentConnectBinding
import com.example.carlog.network.ResponseState
import com.example.carlog.utils.App
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

@AndroidEntryPoint
class ConnectFragment : Fragment() {
    private var _binding : FragmentConnectBinding? = null
    private val binding get() = _binding!!
    private val viewModel : ConnectViewModel by viewModels()
    private val adapterRecyclerDevices = AdapterRecyclerDevices()
    private val bluetoothAdapter by lazy {
        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkBluetoothPermissions()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectBinding.inflate(inflater,container,false)
        onClicks()
        observers()
        return binding.root
    }

    private fun observers() {

        viewModel.apply {
            devices.observe(viewLifecycleOwner){
                when(it){
                    is ResponseState.Success ->{
                        Toast.makeText(requireContext(),"Success " + it.data.size.toString(),Toast.LENGTH_SHORT).show()
                        binding.loading.visibility = View.GONE
                        adapterRecyclerDevices.list = it.data
                        binding.recyclerDevices.adapter = adapterRecyclerDevices
                    }
                    is ResponseState.Error -> {
                        binding.loading.visibility = View.GONE
                        Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                    }
                    ResponseState.Loading -> binding.loading.visibility = View.VISIBLE
                }
            }

            connectionStateLiveData.observe(viewLifecycleOwner){ socket ->
                when(socket){
                    is ResponseState.Success -> {
                        (activity?.application as App).bluetoothSocket = socket.data
                        binding.loading.visibility = View.GONE
                        Toast.makeText(requireContext(),"You're connected",Toast.LENGTH_SHORT).show()
                        Navigation.findNavController(binding.root).navigate(R.id.action_connectFragment_to_homeFragment)
                    }
                    is ResponseState.Error ->{
                        binding.loading.visibility = View.GONE
                        Toast.makeText(requireContext(),socket.message,Toast.LENGTH_SHORT).show()
                    }
                    ResponseState.Loading -> binding.loading.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun onClicks(){
        adapterRecyclerDevices.listener = object : AdapterRecyclerDevices.OnItemClickListener{
            override fun onItemClicked(bluetoothDevice: BluetoothDevice) {
                viewModel.connectToDevice(bluetoothDevice)
            }
        }

        binding.textDevices.setOnClickListener{
            Navigation.findNavController(binding.root).navigate(R.id.action_connectFragment_to_homeFragment)
        }
    }


    private fun checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.BLUETOOTH_CONNECT
                ) -> {
                    viewModel.initializeBluetooth(bluetoothAdapter!!)
                }
                else -> {
                    requestBluetoothConnectPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
                }
            }
        }else{
            viewModel.initializeBluetooth(bluetoothAdapter!!)
        }
    }
    private val requestBluetoothConnectPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                checkBluetoothPermissions()
            } else {
                Toast.makeText(requireContext(),"isGranted.toString()",Toast.LENGTH_SHORT).show()
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}