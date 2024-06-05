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

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _moreLoading = MutableLiveData<Boolean>()
    val moreLoading: LiveData<Boolean> = _moreLoading

    private val _isEndOfData = MutableLiveData<Boolean>()
    val isEndOfData: LiveData<Boolean> get() = _isEndOfData

    private val _initialLoadComplete = MutableLiveData<Boolean>()
    val initialLoadComplete: LiveData<Boolean> get() = _initialLoadComplete

    private val delayTime = 500L

    private var currentPage = 1
    private val pageSize = 15

    // 카테고리에 맞는 LinkList 가져오기
    fun getEqualCategoryLinkList(category: String, sortOrder: Int, initialLoad: Boolean, limit: Int = pageSize) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                if (initialLoad) {
                    _loading.value = true
                }
            }
            val result = boardRepository.getEqualCategoryLinkList(category, sortOrder)
            _categoryResult.value = result.map { it.take(limit).toMutableList() }
            _isEndOfData.value = result.getOrNull()?.size ?: 0 < pageSize
            job.cancel()
            if (initialLoad) {
                _loading.value = false
                _initialLoadComplete.value = true
            }
        }
    }

    // 다음 페이지의 LinkList 가져오기
    fun loadMoreLinkList(category: String, sortOrder: Int) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _moreLoading.value = true
            }
            val result = boardRepository.getEqualCategoryLinkList(category, sortOrder)
            result.onSuccess { links ->
                _categoryResult.value?.onSuccess { currentLinks ->
                    val newLinks = links.drop(currentPage * pageSize).take(pageSize)
                    val updatedLinks = currentLinks + newLinks
                    _categoryResult.value = Result.success(updatedLinks.toMutableList())
                    currentPage++
                    _isEndOfData.value = newLinks.size < pageSize
                }
            }.onFailure {
                _categoryResult.value = result
            }
            job.cancel()
            _moreLoading.value = false
        }
    }

    fun resetPage() {
        currentPage = 1
        _isEndOfData.value = false
        _initialLoadComplete.value = false
    }
}