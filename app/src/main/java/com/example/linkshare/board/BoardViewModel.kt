package com.example.linkshare.board

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BoardViewModel : ViewModel() {
    private val boardRepo = BoardRepo()

    fun getAllData(): LiveData<MutableList<BoardModel>> {
        val mutableData = MutableLiveData<MutableList<BoardModel>>()
        boardRepo.getAllData().observeForever {
            mutableData.value = it
        }
        return mutableData
    }

    fun getEqualUidData(): LiveData<MutableList<BoardModel>> {
        val mutableData = MutableLiveData<MutableList<BoardModel>>()
        boardRepo.getEqualUidData().observeForever {
            mutableData.value = it
        }
        return mutableData
    }

    fun getCategoryData(position: Int): LiveData<MutableList<BoardModel>> {
        val mutableData = MutableLiveData<MutableList<BoardModel>>()
        boardRepo.getCategoryData(position).observeForever {
            mutableData.value = it
        }
        return mutableData
    }

    fun getKeyList(): MutableList<String> {
        return boardRepo.getKeyList()
    }
}