package com.example.linkshare.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CommentViewModel(private val commentRepository: CommentRepository): ViewModel() {

    private var commentsLiveData: LiveData<MutableList<Comment>>? = null

    private val _deleteStatus = MutableLiveData<Boolean>()
    val deleteStatus: LiveData<Boolean> = _deleteStatus

    private val _commentStatus = MutableLiveData<Boolean>()
    val commentStatus: LiveData<Boolean> = _commentStatus

    // 해당하는 글에 대한 댓글 가져오기
    fun getCommentData(key: String): LiveData<MutableList<Comment>> {
        if (commentsLiveData == null) {
            commentsLiveData = commentRepository.getCommentsLiveData(key)
        }
        return commentsLiveData!!
    }

    // 댓글 입력하기
    fun insertComment(comment: Comment, key: String) {
        viewModelScope.launch {
            val result = commentRepository.insertComment(comment, key)
            _commentStatus.value = result
        }
    }

    // 댓글 삭제
    fun deleteComment(postKey: String, commentId: String) {
        viewModelScope.launch {
            val result = commentRepository.deleteComment(postKey, commentId)
            _deleteStatus.value = result
        }
    }
}