package com.example.linkshare.board

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkshare.link.Link
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MoreBoardViewModel(private val boardRepository: BoardRepository) : ViewModel() {

    private val _categoryResult = MutableLiveData<Result<MutableList<Link>>>()
    val categoryResult: LiveData<Result<MutableList<Link>>> get() = _categoryResult

    private val _moreDataResult = MutableLiveData<Result<MutableList<Link>>>()
    val moreDataResult: LiveData<Result<MutableList<Link>>> get() = _moreDataResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _moreDataLoading = MutableLiveData<Boolean>()
    val moreDataLoading: LiveData<Boolean> = _moreDataLoading

    private val _noMoreData = MutableLiveData<Boolean>()
    val noMoreData: LiveData<Boolean> get() = _noMoreData

    private val delayTime = 500L

    // 카테고리에 맞는 LinkList 가져오기
    fun getEqualCategoryLinkList(category: String, sortOrder: Int, limit: Int) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _loading.value = true
            }
            val result = boardRepository.getEqualCategoryLinkList(category, sortOrder)
            _categoryResult.value = result.map { it.take(limit).toMutableList() }
            job.cancel()
            _loading.value = false
        }
    }

    // 더 많은 데이터 가져오기
    fun loadMoreData(category: String, sortOrder: Int, offset: Int, limit: Int) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _moreDataLoading.value = true
            }
            val result = boardRepository.getEqualCategoryLinkList(category, sortOrder)
            val newLinks = result.getOrNull()?.drop(offset)?.take(limit)?.toMutableList()
            _moreDataResult.value = result.map { it.take(limit).toMutableList() }
            _noMoreData.value = newLinks?.isEmpty()
            job.cancel()
            _moreDataLoading.value = false
        }
    }
}