package com.example.linkshare.setting

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChangePasswordViewModel(private val settingRepository: SettingRepository) : ViewModel() {
    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    private val _updatePasswordResult = MutableLiveData<Result<Boolean>>()
    val updatePasswordResult: LiveData<Result<Boolean>> get() = _updatePasswordResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val delayTime = 500L

    // 사용자 가져오기
    fun fetchCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = settingRepository.getCurrentUser()
        }
    }

    // 비밀번호 업데이트
    fun updatePassword(email: String, currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _loading.value = true
            }
            val result = settingRepository.reauthenticateUser(email, currentPassword)
            result.fold(
                onSuccess = {
                    val updateResult = settingRepository.updatePassword(newPassword)
                    _updatePasswordResult.value = updateResult
                },
                onFailure = {
                    _updatePasswordResult.value = Result.failure(it)
                    // You can log the exception or show an error message to the user
                    Log.e("SettingViewModel", "Reauthentication failed", it)
                }
            )
            job.cancel()
            _loading.value = false
        }
    }
}