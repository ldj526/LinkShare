package com.example.linkshare.link

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.launch

class LinkViewModel : ViewModel() {
    private val linkRepo = LinkRepo()

    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> = _saveStatus

    private val _linkData = MutableLiveData<Link>()
    val linkData: LiveData<Link?> = _linkData

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String?> = _imageUrl

    private val _deleteStatus = MutableLiveData<Boolean>()
    val deleteStatus: LiveData<Boolean> = _deleteStatus

    // 내가 작성하고 공유받은 메모들 가져오기
    fun getUserWrittenAndSharedData(uid: String): LiveData<MutableList<Link>> {
        val mediatorLiveData = MediatorLiveData<MutableList<Link>>()
        val linkData = linkRepo.getEqualUidListData(uid)
        val sharedLinkData = linkRepo.getSharedListData(uid)

        mediatorLiveData.addSource(linkData) {
            mediatorLiveData.value = combineDataList(linkData.value, sharedLinkData.value)
        }

        mediatorLiveData.addSource(sharedLinkData) {
            mediatorLiveData.value = combineDataList(linkData.value, sharedLinkData.value)
        }

        return mediatorLiveData
    }

    // LiveData 합치기
    private fun combineDataList(
        linkList: MutableList<Link>?,
        boardList: MutableList<Link>?
    ): MutableList<Link> {
        val combinedList = mutableListOf<Link>()
        linkList?.let { combinedList.addAll(it) }
        boardList?.let { combinedList.addAll(it) }
        combinedList.sortByDescending { it.time }
        return combinedList
    }

    // 메모, 게시글에서 볼 데이터를 받아오는 기능
    fun getPostData(key: String) {
        viewModelScope.launch {
            val linkData = linkRepo.getLinkDataByKey(key)
            _linkData.postValue(linkData!!)
        }
    }

    // 이미지 url 받아오는 기능
    fun getImageUrl(key: String) {
        viewModelScope.launch {
            _imageUrl.postValue(linkRepo.getImageUrl(key))
        }
    }

    // 메모 저장하기
    fun saveLink(link: Link, imageData: ByteArray?, isEditMode: Boolean) {
        viewModelScope.launch {
            val result = linkRepo.saveLink(link, imageData, isEditMode)
            _saveStatus.value = result
        }
    }

    // 메모 삭제
    fun deleteMemo(category: DatabaseReference, key: String) {
        viewModelScope.launch {
            val result = linkRepo.deleteLink(category, key)
            _deleteStatus.value = result
        }
    }
}