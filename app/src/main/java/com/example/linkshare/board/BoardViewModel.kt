package com.example.linkshare.board

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkshare.link.Link
import com.example.linkshare.util.ShareResult
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BoardViewModel(private val boardRepository: BoardRepository): ViewModel() {

    private val _shareStatus = MutableLiveData<ShareResult>()
    val shareStatus: LiveData<ShareResult> = _shareStatus

    private val _shareCount = MutableLiveData<Int>()
    val shareCount: LiveData<Int> = _shareCount

    private val _imageUrl = MutableLiveData<String?>()
    val imageUrl: LiveData<String?> = _imageUrl

    private val _linkData = MutableLiveData<Link?>()
    val linkData: LiveData<Link?> = _linkData

    private val _deleteStatus = MutableLiveData<Boolean>()
    val deleteStatus: LiveData<Boolean> = _deleteStatus

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _imageLoading = MutableLiveData<Boolean>()
    val imageLoading: LiveData<Boolean> = _imageLoading

    private val _searchResult = MutableLiveData<Result<MutableList<Link>>>()
    val searchResult: LiveData<Result<MutableList<Link>>> get() = _searchResult

    private val _categoryResult = MutableLiveData<Result<MutableList<Link>>>()
    val categoryResult: LiveData<Result<MutableList<Link>>> get() = _categoryResult

    private val delayTime = 500L

    // 검색된 링크 가져오기
    fun getSearchedLinks(searchText: String, searchOption: String) {
        viewModelScope.launch {
            val job = launch {// 빠르게 처리되면 progressBar 안나타나게 하기 위함
                delay(delayTime)
                _loading.value = true
            }
            val result = boardRepository.searchLinks(searchText, searchOption)
            _searchResult.value = result
            job.cancel()
            _loading.value = false
        }
    }

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

    // 링크 공유하기
    fun shareLink(key: String, imageData: ByteArray?) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _loading.value = true
            }
            val result = boardRepository.shareLink(key, imageData)
            result.onSuccess { newShareCount ->
                _shareCount.value = newShareCount
                _shareStatus.value = ShareResult.SUCCESS
            }.onFailure {
                _shareStatus.value = ShareResult.FAILURE
            }
            job.cancel()
            _loading.value =false
        }
    }

    // 메모, 게시글에서 볼 데이터를 받아오는 기능
    fun getPostData(key: String) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _loading.value = true
            }
            val result = boardRepository.getLinkDataByKey(key)
            result.onSuccess { linkData ->
                _linkData.postValue(linkData)
            }.onFailure {
                _linkData.postValue(null)
            }
            job.cancel()
            _loading.value = false
        }
    }

    // 이미지 url 받아오는 기능
    fun getImageUrl(key: String) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _imageLoading.value = true
            }
            val result = boardRepository.getImageUrl(key)
            result.onSuccess { url ->
                _imageUrl.postValue(url)
            }.onFailure {
                _imageUrl.postValue(null)
            }
            job.cancel()
            _imageLoading.value = false
        }
    }

    // 링크 삭제
    fun deleteLink(ref: DatabaseReference, key: String) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _loading.value = true
            }
            val result = boardRepository.deleteLink(ref, key)
            result.onSuccess {
                _deleteStatus.value = true
            }.onFailure {
                _deleteStatus.value = false
            }
            job.cancel()
            _loading.value = false
        }
    }
}