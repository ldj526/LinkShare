package com.example.linkshare.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.linkshare.util.FBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CommentRepo {

    // 해당하는 글에 대한 댓글 가져오기
    fun getCommentsLiveData(key: String): LiveData<MutableList<Comment>> {
        val liveData = MutableLiveData<MutableList<Comment>>()
        FBRef.commentCategory.child(key).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comments = mutableListOf<Comment>()
                for (dataModel in snapshot.children) {
                    val comment = dataModel.getValue(Comment::class.java)
                    comment?.let { comments.add(it) }
                }
                liveData.postValue(comments)
            }

            override fun onCancelled(error: DatabaseError) {
                // 에러 처리
            }
        })
        return liveData
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