package com.example.linkshare.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class NicknameViewModelFactory(private val repository: NicknameRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NicknameViewModel::class.java)) {
            return NicknameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}