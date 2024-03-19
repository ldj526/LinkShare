package com.example.linkshare.board

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkshare.link.Link
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.ShareResult
import com.google.firebase.database.DatabaseReference
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

    // Repository에서 반환된 LiveData를 직접 사용
    val userWrittenData: LiveData<MutableList<Link>>
    val allUserWrittenData: LiveData<MutableList<Link>>

    init {
        val uid = FBAuth.getUid()
        userWrittenData = boardRepo.getEqualUidListData(uid)
        allUserWrittenData = boardRepo.getAllLinkListData()
    }

    // 메모 공유하기
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