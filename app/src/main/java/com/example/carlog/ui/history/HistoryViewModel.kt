package com.example.carlog.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carlog.data.ModelAllTrips
import com.example.carlog.network.ResponseState
import com.example.carlog.repo.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(private val repo : Repo) : ViewModel() {
    private val _tripsListLiveData = MutableLiveData<ResponseState<ModelAllTrips>>()
    val tripsListLiveData : LiveData<ResponseState<ModelAllTrips>> get() =  _tripsListLiveData
    fun getTrips(){
        viewModelScope.launch {
            _tripsListLiveData.value = ResponseState.Loading
            try {
                _tripsListLiveData.postValue(repo.getAllTrips())
            }catch (e : Exception){
                _tripsListLiveData.value = ResponseState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}