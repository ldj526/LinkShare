package com.example.linkshare.memo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.ShareResult
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.launch

class MemoViewModel : ViewModel() {
    private val memoRepo = MemoRepo()

    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> = _saveStatus

    private val _memoData = MutableLiveData<Memo>()
    val memoData: LiveData<Memo?> = _memoData

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String?> = _imageUrl

    private val _deleteStatus = MutableLiveData<Boolean>()
    val deleteStatus: LiveData<Boolean> = _deleteStatus

    private val _shareStatus = MutableLiveData<ShareResult>()
    val shareStatus: LiveData<ShareResult> = _shareStatus

    // Repository에서 반환된 LiveData를 직접 사용
    val userWrittenData: LiveData<MutableList<Memo>>
    val allUserWrittenData: LiveData<MutableList<Memo>>

    init {
        val uid = FBAuth.getUid()
        userWrittenData = memoRepo.getMemoData(uid)
        allUserWrittenData = memoRepo.getAllMemoData()
    }

    // 내가 작성하고 공유받은 메모들 가져오기
    fun getUserWrittenAndSharedData(uid: String): LiveData<MutableList<Memo>> {
        val mediatorLiveData = MediatorLiveData<MutableList<Memo>>()
        val memoData  = memoRepo.getMemoData(uid)
        val boardData = memoRepo.getBoardData(uid)

        mediatorLiveData.addSource(memoData) { memos ->
            mediatorLiveData.value = combineDataList(memoData.value, boardData.value)
        }

        mediatorLiveData.addSource(boardData) { memos ->
            mediatorLiveData.value = combineDataList(memoData.value, boardData.value)
        }

        return mediatorLiveData
    }

    // LiveData 합치기
    private fun combineDataList(memoList: MutableList<Memo>?, boardList: MutableList<Memo>?): MutableList<Memo> {
        val combinedList = mutableListOf<Memo>()
        memoList?.let { combinedList.addAll(it) }
        boardList?.let { combinedList.addAll(it) }
        return combinedList
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
    fun saveMemo(memo: Memo, imageData: ByteArray?, isEditMode: Boolean) {
        viewModelScope.launch {
            val result = memoRepo.saveMemo(memo, imageData, isEditMode)
            _saveStatus.value = result
        }
    }

    // 메모 공유하기
    fun shareMemo(memo: Memo, imageData: ByteArray?) {
        viewModelScope.launch {
            val result = memoRepo.shareMemo(memo, imageData)
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