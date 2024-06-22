package com.example.linkshare.auth

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository): ViewModel() {

    private val _loginResult = MutableLiveData<Result<FirebaseUser?>>()
    val loginResult: LiveData<Result<FirebaseUser?>> get() = _loginResult

    private val _nicknameExists = MutableLiveData<Boolean>()
    val nicknameExists: LiveData<Boolean> get() = _nicknameExists

    private val _isEmailDuplicated = MutableLiveData<Result<Boolean>>()
    val isEmailDuplicated: LiveData<Result<Boolean>> get() = _isEmailDuplicated

    private val _isNicknameDuplicated = MutableLiveData<Result<Boolean>>()
    val isNicknameDuplicated: LiveData<Result<Boolean>> get() = _isNicknameDuplicated

    private val _signUpResult = MutableLiveData<Result<Unit>>()
    val signUpResult: LiveData<Result<Unit>> get() = _signUpResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val delayTime = 500L

    fun signInWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _loading.value = true
            }
            val result = repository.signInWithEmailAndPassword(email, password)
            job.cancel()
            _loading.value = false
            _loginResult.postValue(result)
            result.getOrNull()?.uid?.let { checkNickname(it) }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _loading.value = true
            }
            val result = repository.signInWithGoogle(idToken)
            job.cancel()
            _loading.value = false
            _loginResult.postValue(result)
            result.getOrNull()?.uid?.let { checkNickname(it) }
        }
    }

    fun signInWithKakao(activity: Activity, token: String) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _loading.value = true
            }
            val result = repository.signInWithKakao(activity, token)
            job.cancel()
            _loading.value = false
            _loginResult.postValue(result)
            result.getOrNull()?.uid?.let { checkNickname(it) }
        }
    }

    fun checkNickname(uid: String) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _loading.value = true
            }
            val exists = repository.checkNickname(uid)
            job.cancel()
            _loading.value = false
            _nicknameExists.postValue(exists)
        }
    }

    fun checkEmailDuplication(email: String) {
        viewModelScope.launch {
            _isEmailDuplicated.value = repository.checkEmailDuplication(email)
        }
    }

    fun checkNicknameDuplication(nickname: String) {
        viewModelScope.launch {
            _isNicknameDuplicated.value = repository.checkNicknameDuplication(nickname)
        }
    }

    fun signUpUser(email: String, password: String, nickname: String) {
        viewModelScope.launch {
            _signUpResult.value = repository.signUpUser(email, password, nickname)
        }
    }
}