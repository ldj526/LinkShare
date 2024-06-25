package com.example.linkshare.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingViewModel(private val settingRepository: SettingRepository): ViewModel() {

    private val _userNicknameResult = MutableLiveData<Result<String?>>()
    val userNicknameResult: LiveData<Result<String?>> get() = _userNicknameResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _emailLoading = MutableLiveData<Boolean>()
    val emailLoading: LiveData<Boolean> get() = _emailLoading

    private val _loginMethodLoading = MutableLiveData<Boolean>()
    val loginMethodLoading: LiveData<Boolean> get() = _loginMethodLoading

    private val _deleteResult = MutableLiveData<Result<Unit>>()
    val deleteResult: LiveData<Result<Unit>> get() = _deleteResult

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    private val _authMethod = MutableLiveData<String>()
    val authMethod: LiveData<String> get() = _authMethod

    private val _profileEmail = MutableLiveData<Result<String?>>()
    val profileEmail: LiveData<Result<String?>> get() = _profileEmail

    private val delayTime = 500L

    // Nickname 가져오기
    fun fetchUserNickname(userId: String) {
        viewModelScope.launch {
            val job = launch {// 빠르게 처리되면 progressBar 안나타나게 하기 위함
                delay(delayTime)
                _loading.value = true
            }
            val result = settingRepository.getUserNickname(userId)
            job.cancel()
            _loading.value = false
            _userNicknameResult.value = result
        }
    }

    // 로그인 제공자와 이메일 가져오기
    fun fetchUserDetails() {
        viewModelScope.launch {
            val methodJob = launch {
                delay(delayTime)
                _loginMethodLoading.value = true
            }

            val emailJob = launch {
                delay(delayTime)
                _emailLoading.value = true
            }

            val provider = settingRepository.getLoginProvider()
            methodJob.cancel()
            _loginMethodLoading.value = false
            _authMethod.value = provider

            val emailResult = settingRepository.fetchUserEmail(provider)
            emailJob.cancel()
            _emailLoading.value = false
            _profileEmail.value = emailResult
        }
    }

    // 사용자의 계정 삭제
    fun deleteUserAccount() {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _loading.value = true
            }
            val result = settingRepository.deleteUserAccount()
            job.cancel()
            _loading.value = false
            _deleteResult.value = result
        }
    }
}