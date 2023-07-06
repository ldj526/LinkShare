package com.example.linkshare.memo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MemoViewModel: ViewModel() {

    private val memoRepo = MemoRepo()

    fun getFBMemoData(): LiveData<MutableList<MemoModel>> {
        val mutableData = MutableLiveData<MutableList<MemoModel>>()
        memoRepo.getFBMemoData().observeForever {
            mutableData.value = it
        }
        return mutableData
    }

    fun getMemoKeyList(): MutableList<String> {
        return memoRepo.getMemoKeyList()
    }
}