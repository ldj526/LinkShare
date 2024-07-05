package com.example.linkshare.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkshare.util.FBAuth
import kotlinx.coroutines.launch

class CommentViewModel(private val commentRepository: CommentRepository) : ViewModel() {

    private val _comments = MutableLiveData<MutableList<Comment>>()
    val comments: LiveData<MutableList<Comment>> = _comments

    private val _deleteStatus = MutableLiveData<Boolean>()
    val deleteStatus: LiveData<Boolean> = _deleteStatus

    private val _commentStatus = MutableLiveData<Boolean>()
    val commentStatus: LiveData<Boolean> = _commentStatus

    // 해당하는 글에 대한 댓글 가져오기
    fun getComments(linkId: String) {
        val commentsLiveData = commentRepository.getComments(linkId)
        commentsLiveData.observeForever {
            _comments.value = it
        }
    }

    // 댓글 입력하기
    fun insertComment(comment: Comment, linkId: String) {
        viewModelScope.launch {
            val nickname = commentRepository.getUserNickname(FBAuth.getUid())
            val updatedComment = comment.copy(nickname = nickname)
            val result = commentRepository.insertComment(updatedComment,linkId)
            _commentStatus.value = result
        }
    }

    // 댓글 삭제
    fun deleteComment(linkId: String, commentId: String) {
        viewModelScope.launch {
            val result = commentRepository.deleteComment(linkId, commentId)
            _deleteStatus.value = result
        }
    }
}