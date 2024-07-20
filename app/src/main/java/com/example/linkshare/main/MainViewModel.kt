package com.example.linkshare.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkshare.link.Link
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(private val mainViewRepository: MainViewRepository) : ViewModel() {
    private val _topViewLinks = MutableLiveData<Result<MutableList<Link>>>()
    val topViewLinks: LiveData<Result<MutableList<Link>>> = _topViewLinks

    private val _topShareLinks = MutableLiveData<Result<MutableList<Link>>>()
    val topShareLinks: LiveData<Result<MutableList<Link>>> = _topShareLinks

    private val _viewLoading = MutableLiveData<Boolean>()
    val viewLoading: LiveData<Boolean> = _viewLoading

    private val _shareLoading = MutableLiveData<Boolean>()
    val shareLoading: LiveData<Boolean> = _shareLoading

    private val delayTime = 500L

    fun getTopViewLinks(timeRange: MainViewRepository.TimeRange) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _viewLoading.value = true
            }
            val result = mainViewRepository.getTopLinks(timeRange, MainViewRepository.SortBy.VIEWS)
            job.cancel()
            _viewLoading.value = false
            _topViewLinks.postValue(result)
        }
    }

    fun getTopShareLinks(timeRange: MainViewRepository.TimeRange) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _shareLoading.value = true
            }
            val result = mainViewRepository.getTopLinks(timeRange, MainViewRepository.SortBy.SHARES)
            job.cancel()
            _shareLoading.value = false
            _topShareLinks.postValue(result)
        }
    }
}