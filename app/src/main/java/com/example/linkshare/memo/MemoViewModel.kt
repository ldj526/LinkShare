package com.example.linkshare.memo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkshare.comment.Comment
import kotlinx.coroutines.launch

class MemoViewModel : ViewModel() {
    private val memoRepo = MemoRepo()
    private val _userWrittenAndSharedData = MutableLiveData<MutableList<Memo>>()
    val userWrittenAndSharedData: LiveData<MutableList<Memo>> = _userWrittenAndSharedData

    private val _userWrittenData = MutableLiveData<MutableList<Memo>>()
    val userWrittenData: LiveData<MutableList<Memo>> = _userWrittenData

    private val _allUserWrittenData = MutableLiveData<MutableList<Memo>>()
    val allUserWrittenData: LiveData<MutableList<Memo>> = _allUserWrittenData

    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> = _saveStatus

    private val _memoData = MutableLiveData<Memo>()
    val memoData: LiveData<Memo?> = _memoData

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String?> = _imageUrl

    // 내가 작성하고 공유받은 메모들 가져오기
    fun getUserWrittenAndSharedData(uid: String) {
        viewModelScope.launch {
            val memoData = memoRepo.getMemoData(uid)
            val boardData = memoRepo.getBoardData(uid)
            _userWrittenAndSharedData.postValue((memoData + boardData).toMutableList())
        }
    }

    // 내가 작성한 메모만 가져오는 것
    fun getUserWrittenData(uid: String) {
        viewModelScope.launch {
            val memoData = memoRepo.getMemoData(uid)
            _userWrittenData.postValue(memoData)
        }
    }

    // 모든 사용자들이 작성한 메모들
    fun getAllUserWrittenData() {
        viewModelScope.launch {
            val memoData = memoRepo.getAllMemoData()
            _allUserWrittenData.postValue(memoData)
        }
    }

    // 수정화면에서 데이터 가져오기
    fun getMemoDataForUpdate(key: String) {
        viewModelScope.launch {
            _memoData.postValue(memoRepo.getMemoDataForUpdate(key))
        }
    }

    // 수정화면에서 이미지 가져오기
    fun getImageUrlForUpdate(key: String) {
        viewModelScope.launch {
            _imageUrl.postValue(memoRepo.getImageUrlForUpdate(key))
        }
    }

    // 메모 저장하기
    fun saveMemo(memo: Memo, imageData: ByteArray?, isEditMode: Boolean) {
        viewModelScope.launch {
            val result = memoRepo.saveMemo(memo, imageData, isEditMode)
            _saveStatus.value = result
        }
    }
}