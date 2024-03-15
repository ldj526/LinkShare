package com.example.linkshare.memo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DatabaseReference
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

    private val _deleteStatus = MutableLiveData<Boolean>()
    val deleteStatus: LiveData<Boolean> = _deleteStatus

    private val _shareStatus = MutableLiveData<Boolean>()
    val shareStatus: LiveData<Boolean> = _shareStatus

    // 내가 작성하고 공유받은 메모들 가져오기
    fun getUserWrittenAndSharedData(uid: String) {
        viewModelScope.launch {
            val memoData = memoRepo.getMemoData(uid)
            val boardData = memoRepo.getBoardData(uid)
            _userWrittenAndSharedData.postValue((memoData + boardData).toMutableList())
            Log.d("memoData", "ViewModel에서 getUserWirtten ~ :$_userWrittenAndSharedData")
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

    // 메모, 게시글에서 볼 데이터를 받아오는 기능
    fun getPostData(key: String) {
        viewModelScope.launch {
            val memoData = memoRepo.getMemoDataByKey(key)
            _memoData.postValue(memoData!!)
        }
    }

    // 이미지 url 받아오는 기능
    fun getImageUrl(key: String) {
        viewModelScope.launch {
            _imageUrl.postValue(memoRepo.getImageUrl(key))
        }
    }

    // 메모 저장하기
    fun saveMemo(memo: Memo, imageData: ByteArray?, category: DatabaseReference, isEditMode: Boolean) {
        viewModelScope.launch {
            val result = memoRepo.saveMemo(memo, imageData, category, isEditMode)
            _saveStatus.value = result
        }
    }

    // 메모 공유하기
    fun shareMemo(memo: Memo, imageData: ByteArray?, category: DatabaseReference, isEditMode: Boolean) {
        viewModelScope.launch {
            val result = memoRepo.saveMemo(memo, imageData, category, isEditMode)
            _shareStatus.value = result
        }
    }

    // 메모 삭제
    fun deleteMemo(category: DatabaseReference, key: String) {
        viewModelScope.launch {
            val result = memoRepo.deleteMemo(category, key)
            _deleteStatus.value = result
        }
    }
}