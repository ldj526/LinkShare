package com.example.linkshare.comment

import com.example.linkshare.util.FBRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CommentRepo {

    // 해당하는 글에 대한 댓글 가져오기
    suspend fun getCommentData(key: String): MutableList<Comment> = withContext(Dispatchers.IO) {
        val commentList = mutableListOf<Comment>()
        val snapshot = FBRef.commentCategory.child(key).get().await()
        // Get Post object and use the values to update the UI
        for (dataModel in snapshot.children) {
            // Memo 형식의 데이터 받기
            val item = dataModel.getValue(Comment::class.java)
            item?.let { commentList.add(it) }
        }
        commentList.sortBy { it.time }
        commentList
    }

    // Firebase에 댓글 내용 입력
    suspend fun insertComment(comment: Comment, key: String): Boolean = withContext(Dispatchers.IO) {
        try {
            FBRef.commentCategory.child(key).push().setValue(comment).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}