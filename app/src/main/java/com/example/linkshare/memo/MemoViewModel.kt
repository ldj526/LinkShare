package com.example.linkshare.memo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun saveMemo(memo: Memo, imageData: ByteArray?, isEditMode: Boolean) {
        viewModelScope.launch {
            val result = memoRepo.saveMemo(memo, imageData, isEditMode)
            _saveStatus.value = result
        }
    }
}