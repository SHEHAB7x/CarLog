package com.example.carlog.ui.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.carlog.adapters.AdapterRecyclerTrips
import com.example.carlog.data.ModelAllTrips
import com.example.carlog.databinding.FragmentHistoryBinding
import com.example.carlog.network.ResponseState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val historyViewMode : HistoryViewModel by viewModels()
    private val adapterRecyclerTrips = AdapterRecyclerTrips()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        onClicks()
        getData()
        observers()
        return binding.root

    }

    private fun getData() {
        historyViewMode.getTrips()
    }

    private fun observers() {
        historyViewMode.tripsListLiveData.observe(viewLifecycleOwner){
            when (it){
                is ResponseState.Success -> setData(it)
                is ResponseState.Error -> showToast(it.message)
                ResponseState.Loading -> showLoadingIndicator()
            }
        }
    }

    private fun showToast(message: String) {
        binding.loading.visibility = View.GONE
        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
        Log.e("TAG", "showToast: $message" )
    }

    private fun setData(data: ResponseState.Success<ModelAllTrips>) {
        binding.loading.visibility = View.GONE
        adapterRecyclerTrips.list = data.data.getList
        binding.recyclerHistory.adapter = adapterRecyclerTrips
    }

    private fun showLoadingIndicator() {
        binding.loading.visibility = View.VISIBLE
    }

    private fun onClicks() {
    }
}