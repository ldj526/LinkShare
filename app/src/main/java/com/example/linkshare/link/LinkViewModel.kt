package com.example.linkshare.link

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LinkViewModel(private val linkRepository: LinkRepository) : ViewModel() {

    private val _saveStatus = MutableLiveData<Result<Boolean>>()
    val saveStatus: LiveData<Result<Boolean>> = _saveStatus

    private val _linkData = MutableLiveData<Result<Link?>>()
    val linkData: LiveData<Result<Link?>> = _linkData

    private val _imageUrl = MutableLiveData<String?>()
    val imageUrl: LiveData<String?> = _imageUrl

    private val _deleteStatus = MutableLiveData<Result<Boolean>>()
    val deleteStatus: LiveData<Result<Boolean>> = _deleteStatus

    private val _userLinks = MutableLiveData<Result<MutableList<Link>>>()
    val userLinks: LiveData<Result<MutableList<Link>>> = _userLinks

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _imageLoading = MutableLiveData<Boolean>()
    val imageLoading: LiveData<Boolean> = _imageLoading

    private val delayTime = 500L

    // 내가 작성하고 공유받은 메모들 가져오기
    fun getUserWrittenAndSharedData(uid: String) {
        viewModelScope.launch {
            val job = launch {// 빠르게 처리되면 progressBar 안나타나게 하기 위함
                delay(delayTime)
                _loading.value = true
            }
            val userLinksResult = linkRepository.getEqualUidListData(uid)
            val sharedLinksResult = linkRepository.getSharedListData(uid)

            val combinedList = mutableListOf<Link>()

            userLinksResult.onSuccess { userLinks ->
                combinedList.addAll(userLinks)
            }

            sharedLinksResult.onSuccess { sharedLinks ->
                combinedList.addAll(sharedLinks)
            }

            combinedList.sortByDescending { it.time }

            _userLinks.postValue(Result.success(combinedList))
            job.cancel()
            _loading.value = false
        }
    }

    // 메모, 게시글에서 볼 데이터를 받아오는 기능
    fun getPostData(key: String) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _imageLoading.value = true
            }
            val linkData = linkRepository.getLinkDataByKey(key)
            _linkData.postValue(linkData)
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
            val result = linkRepository.getImageUrl(key)
            result.onSuccess { url ->
                _imageUrl.postValue(url)
            }.onFailure {
                _imageUrl.postValue(null)
            }
            job.cancel()
            _imageLoading.value = false
        }
    }

    // 메모 저장하기
    fun saveLink(link: Link, imageData: ByteArray?, isEditMode: Boolean) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _loading.value = true
            }
            val result = linkRepository.saveLink(link, imageData, isEditMode)
            _saveStatus.value = result
            job.cancel()
            _loading.value = false
        }
    }

    // 메모 삭제
    fun deleteMemo(ref: DatabaseReference, key: String) {
        viewModelScope.launch {
            val job = launch {
                delay(delayTime)
                _loading.value = true
            }
            val result = linkRepository.deleteLink(ref, key)
            _deleteStatus.value = result
            job.cancel()
            _loading.value = false
        }
    }
}