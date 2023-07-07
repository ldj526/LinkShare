package com.example.linkshare.board.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.linkshare.utils.FBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class CommentRepo {

    private val commentDataList = mutableListOf<CommentModel>()
    private val mutableData = MutableLiveData<MutableList<CommentModel>>()

    fun getCommentData(key: String): LiveData<MutableList<CommentModel>> {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                // 중복되는 데이터가 생기므로 기존에 있던 데이터들을 삭제해준다.
                commentDataList.clear()
                for (dataModel in dataSnapshot.children) {
                    // CommentModel 형식의 데이터 받기
                    val item = dataModel.getValue(CommentModel::class.java)
                    commentDataList.add(item!!)

                    mutableData.value = commentDataList
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        FBRef.commentList.child(key).addValueEventListener(postListener)

        return mutableData
    }

}