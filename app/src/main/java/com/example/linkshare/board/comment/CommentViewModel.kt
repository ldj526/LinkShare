package com.example.linkshare.board.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task

class CommentViewModel : ViewModel() {

    private val commentRepo = CommentRepo()

    fun getCommentData(key: String): LiveData<MutableList<CommentModel>> {
        val mutableData = MutableLiveData<MutableList<CommentModel>>()
        commentRepo.getCommentData(key).observeForever {
            mutableData.value = it
        }
        return mutableData
    }

    fun insertComment(key: String, str: String): Task<Void>{
        return commentRepo.insertComment(key, str)
    }
}