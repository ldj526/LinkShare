package com.example.linkshare.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NicknameViewModel(private val settingRepository: SettingRepository) : ViewModel() {

    private val _updateNicknameResult = MutableLiveData<Result<Unit>>()
    val updateNicknameResult: LiveData<Result<Unit>> get() = _updateNicknameResult

    private val _userNicknameResult = MutableLiveData<Result<String?>>()
    val userNicknameResult: LiveData<Result<String?>> get() = _userNicknameResult

    private val _nicknameDuplicationResult = MutableLiveData<Result<Boolean>>()
    val nicknameDuplicationResult: LiveData<Result<Boolean>> get() = _nicknameDuplicationResult

    private val _lastUpdatedResult = MutableLiveData<Result<Long>>()
    val lastUpdatedResult: LiveData<Result<Long>> get() = _lastUpdatedResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val delayTime = 500L
    private val nicknameChangeLimitDays = 30

    init {
        _loading.value = false // ViewModel 초기화 시 로딩 상태를 false로 설정
    }

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

    // 마지막 업데이트 날짜 가져오기
    fun fetchUserLastUpdated(userId: String) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _loading.value = true
            }
            val result = settingRepository.getUserLastUpdated(userId)
            job.cancel()
            _loading.value = false
            _lastUpdatedResult.value = result
        }
    }

    // 닉네임 변경 가능한지 여부 확인
    fun checkNicknameChangeAllowed(lastUpdated: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val diffDays = (currentTime - lastUpdated) / (1000 * 60 * 60 * 24)
        return diffDays >= nicknameChangeLimitDays
    }

    // 닉네임 변경 가능한 날짜까지 남은 기간
    fun getRemainingDays(lastUpdated: Long): Long {
        val currentTime = System.currentTimeMillis()
        val diffDays = (currentTime - lastUpdated) / (1000 * 60 * 60 * 24)
        return nicknameChangeLimitDays - diffDays
    }

    // Nickname 중복 체크
    fun checkNicknameDuplication(nickname: String) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _loading.value = true
            }
            val result = settingRepository.isNicknameDuplicated(nickname)
            job.cancel()
            _loading.value = false
            _nicknameDuplicationResult.value = result
        }
    }

    // Nickname 업데이트
    fun updateNickname(userId: String, email: String, nickname: String) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _loading.value = true
            }
            val result = settingRepository.updateNickname(userId, email, nickname)
            job.cancel()
            _loading.value = false
            _updateNicknameResult.value = result
        }
    }

    // 로딩 상태 설정
    fun setLoading(isLoading: Boolean) {
        _loading.value = isLoading
    }
}