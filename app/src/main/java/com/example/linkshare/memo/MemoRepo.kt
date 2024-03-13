package com.example.linkshare.memo

import com.example.linkshare.util.FBRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MemoRepo {

    // 자신의 id값과 일치하는 게시물 가져오기
    suspend fun getMemoData(uid: String): MutableList<Memo> = withContext(Dispatchers.IO) {
        val memoList = mutableListOf<Memo>()
        val snapshot = FBRef.memoCategory.get().await()
        // Get Post object and use the values to update the UI
        for (dataModel in snapshot.children) {
            // Memo 형식의 데이터 받기
            val item = dataModel.getValue(Memo::class.java)
            item?.let {
                if (it.uid == uid) {
                    memoList.add(it)
                }
            }
        }
        memoList.sortByDescending { it.time }
        memoList
    }

    // Firebase database로부터 내가 공유받은 memo 가져오기
    suspend fun getBoardData(uid: String): MutableList<Memo> = withContext(Dispatchers.IO) {
        val memoList = mutableListOf<Memo>()
        val snapshot = FBRef.boardCategory.get().await()
        // Get Post object and use the values to update the UI
        for (dataModel in snapshot.children) {
            // Memo 형식의 데이터 받기
            val item = dataModel.getValue(Memo::class.java)
            item?.let {
                if (it.shareUid == uid) {
                    memoList.add(item)
                }
            }
        }
        memoList.sortByDescending { it.time }
        memoList
    }

    // Firebase database로부터 모든 사용자의 memo 가져오기
    suspend fun getAllMemoData(): MutableList<Memo> = withContext(Dispatchers.IO) {
        val memoList = mutableListOf<Memo>()
        val snapshot = FBRef.memoCategory.get().await()
        // Get Post object and use the values to update the UI
        for (dataModel in snapshot.children) {
            // Memo 형식의 데이터 받기
            val item = dataModel.getValue(Memo::class.java)
            memoList.add(item!!)
        }
        memoList.sortByDescending { it.time }
        memoList
    }
}