package com.example.linkshare.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SettingViewModelFactory(private val settingRepository: SettingRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SettingViewModel::class.java) -> {
                SettingViewModel(settingRepository) as T
            }
            modelClass.isAssignableFrom(NicknameViewModel::class.java) -> {
                NicknameViewModel(settingRepository) as T
            }
            modelClass.isAssignableFrom(ChangePasswordViewModel::class.java) -> {
                ChangePasswordViewModel(settingRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
