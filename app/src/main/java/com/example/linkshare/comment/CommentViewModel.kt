package com.example.linkshare.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CommentViewModel: ViewModel() {

    private val commentRepo = CommentRepo()
    private var commentsLiveData: LiveData<MutableList<Comment>>? = null

    private val _commentStatus = MutableLiveData<Boolean>()
    val commentStatus: LiveData<Boolean> = _commentStatus

    // 해당하는 글에 대한 댓글 가져오기
    fun getCommentData(key: String): LiveData<MutableList<Comment>> {
        if (commentsLiveData == null) {
            commentsLiveData = commentRepo.getCommentsLiveData(key)
        }
        return commentsLiveData!!
    }

    // 댓글 입력하기
    fun insertComment(comment: Comment, key: String) {
        viewModelScope.launch {
            val result = commentRepo.insertComment(comment, key)
            _commentStatus.value = result
        }
    }
}