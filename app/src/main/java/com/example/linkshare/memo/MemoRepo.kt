package com.example.linkshare.memo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.linkshare.board.BoardModel
import com.example.linkshare.utils.FBAuth
import com.example.linkshare.utils.FBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

class MemoRepo {

    private val memoDataList = mutableListOf<MemoModel>()
    private val memoKeyList = mutableListOf<String>()
    private val mutableData = MutableLiveData<MutableList<MemoModel>>()

    fun getFBMemoData(): LiveData<MutableList<MemoModel>> {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 중복 방지
                memoDataList.clear()
                // Get Post object and use the values to update the UI
                for (dataModel in dataSnapshot.children) {
                    // BoardModel 형식의 데이터 받기
                    val item = dataModel.getValue(MemoModel::class.java)

                    val myUid = FBAuth.getUid()
                    val writeUid = item!!.uid

                    // 내가 쓴 글 일 경우에만 list에 추가
                    if (myUid == writeUid) {
                        memoDataList.add(item!!)
                        memoKeyList.add(dataModel.key.toString())
                    }
                    mutableData.value = memoDataList
                }
                // 최신 글이 가장 위로
                memoKeyList.reverse()
                memoDataList.reverse()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        FBRef.memoList.addValueEventListener(postListener)
        return mutableData
    }

    fun getMemoKeyList(): MutableList<String> {
        return memoKeyList
    }
}