package com.example.carlog.ui.chats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.example.carlog.R
import com.example.carlog.adapters.AdapterRecyclerMessages
import com.example.carlog.data.ModelMessages
import com.example.carlog.databinding.FragmentChatsBinding
import com.example.carlog.network.ResponseState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatsFragment : Fragment() {
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
    private val chatsViewModel: ChatsViewModel by viewModels()
    private val adapterRecyclerMessages = AdapterRecyclerMessages()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        onClicks()
        getData()
        observers()
        return binding.root
    }

    private fun observers() {
        chatsViewModel.chatsLiveData.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ResponseState.Success -> setData(response)
                is ResponseState.Error -> showToast(response.message)
                ResponseState.Loading -> showLoadingIndicator()
            }
        }
    }

    private fun setData(messagesSuccess: ResponseState.Success<ModelMessages>) {
        binding.loading.visibility = View.GONE
        adapterRecyclerMessages.modelMessages = messagesSuccess.data
        binding.recyclerMessages.adapter = adapterRecyclerMessages
        adapterRecyclerMessages.notifyDataSetChanged()
    }


    private fun showToast(message: String) {
        binding.loading.visibility = View.GONE
        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
        Log.e("TAG", "showToast: $message")
    }


    private fun getData() {
        chatsViewModel.getMessages()
    }

    private fun showLoadingIndicator() {
        binding.loading.visibility = View.VISIBLE
    }

    private fun onClicks() {
        binding.btnHome.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_chatsFragment_to_homeFragment)
        }
        binding.btnProfile.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_chatsFragment_to_profileFragment)
        }
    }

}