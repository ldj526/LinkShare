package com.example.linkshare.board.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CommentViewModel : ViewModel() {

    private val commentRepo = CommentRepo()

    fun getCommentData(key: String): LiveData<MutableList<CommentModel>> {
        val mutableData = MutableLiveData<MutableList<CommentModel>>()
        commentRepo.getCommentData(key).observeForever {
            mutableData.value = it
        }
        return mutableData
    }
}