package com.example.linkshare.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.linkshare.util.FireBaseCollection
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CommentRepository {

    private val firestore = Firebase.firestore

    // 특정 글에 대한 모든 댓글 가져오기
    fun getComments(linkId: String): LiveData<MutableList<Comment>> {
        val commentsLiveData = MutableLiveData<MutableList<Comment>>()
        val query = FireBaseCollection.getUserCommentsCollection(linkId)

        query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val comments = snapshot.documents.mapNotNull { it.toObject(Comment::class.java) }.toMutableList()
                comments.sortByDescending { it.time }
                commentsLiveData.postValue(comments)
            } else {
                commentsLiveData.postValue(mutableListOf())
            }
        }
        return commentsLiveData
    }

    // Firebase에 댓글 내용 입력
    suspend fun insertComment(comment: Comment, linkId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val ref = FireBaseCollection.getUserCommentsCollection(linkId).document()
            comment.id = ref.id
            ref.set(comment).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // 댓글 삭제
    suspend fun deleteComment(linkId: String, commentId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val ref = FireBaseCollection.getUserCommentsCollection(linkId).document(commentId)
            ref.delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // 닉네임 가져오기
    suspend fun getUserNickname(uid: String): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val document = firestore.collection("users").document(uid).get().await()
            document.getString("nickname")
        } catch (e: Exception) {
            null
        }
    }
}