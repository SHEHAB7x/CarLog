package com.example.carlog.ui.home

import androidx.lifecycle.ViewModel
import com.example.carlog.repo.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repo: Repo) : ViewModel() {

}