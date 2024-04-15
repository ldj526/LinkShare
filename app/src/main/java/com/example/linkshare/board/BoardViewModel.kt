package com.example.linkshare.board

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.linkshare.link.Link
import com.example.linkshare.util.ShareResult
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BoardViewModel: ViewModel() {

    private val boardRepo = BoardRepo()

    private val _shareStatus = MutableLiveData<ShareResult>()
    val shareStatus: LiveData<ShareResult> = _shareStatus

    private val _shareCount = MutableLiveData<Int>()
    val shareCount: LiveData<Int> = _shareCount

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String?> = _imageUrl

    private val _linkData = MutableLiveData<Link>()
    val linkData: LiveData<Link?> = _linkData

    private val _deleteStatus = MutableLiveData<Boolean>()
    val deleteStatus: LiveData<Boolean> = _deleteStatus

    // 검색된 링크 가져오기
    fun getSearchedLinks(searchText: String, searchOption: String): LiveData<MutableList<Link>> {
        return boardRepo.searchLinks(searchText, searchOption)
    }

    // 카테고리에 맞는 LinkList 가져오기
    fun getEqualCategoryLinkList(category: String, sortOrder: Int, limit: Int): LiveData<MutableList<Link>> {
        return boardRepo.getEqualCategoryLinkList(category, sortOrder).switchMap { links ->
            liveData(viewModelScope.coroutineContext + Dispatchers.Default) {
                emit(links.take(limit).toMutableList())
            }
        }
    }

    // 링크 공유하기
    fun shareLink(key: String, imageData: ByteArray?) {
        viewModelScope.launch {
            val (result, newShareCount) = boardRepo.shareLink(key, imageData)
            if (result == ShareResult.SUCCESS) {
                _shareCount.value = newShareCount
            }
            _shareStatus.value = result
        }
    }

    // 메모, 게시글에서 볼 데이터를 받아오는 기능
    fun getPostData(key: String) {
        viewModelScope.launch {
            val linkData = boardRepo.getLinkDataByKey(key)
            _linkData.postValue(linkData!!)
        }
    }

    // 이미지 url 받아오는 기능
    fun getImageUrl(key: String) {
        viewModelScope.launch {
            _imageUrl.postValue(boardRepo.getImageUrl(key))
        }
    }

    // 링크 삭제
    fun deleteLink(ref: DatabaseReference, key: String) {
        viewModelScope.launch {
            val result = boardRepo.deleteLink(ref, key)
            _deleteStatus.value = result
        }
    }
}