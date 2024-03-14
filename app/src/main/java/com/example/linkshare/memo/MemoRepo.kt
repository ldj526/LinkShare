package com.example.linkshare.memo

import com.example.linkshare.util.FBRef
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
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
            item?.let { memoList.add(it) }
        }
        memoList.sortByDescending { it.time }
        memoList
    }

    // 수정화면에서 데이터를 가져오기 위함.
    suspend fun getMemoDataForUpdate(key: String): Memo? = withContext(Dispatchers.IO) {
        val snapshot = FBRef.memoCategory.child(key).get().await()
        snapshot.getValue(Memo::class.java)
    }

    // 수정화면에서 이미지만 가져오기
    suspend fun getImageUrlForUpdate(key: String): String? = withContext(Dispatchers.IO) {
        val storageRef = Firebase.storage.reference.child("${key}.png")
        try {
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    // 메모 저장
    suspend fun saveMemo(memo:Memo, imageData: ByteArray?, isEditMode: Boolean): Boolean = withContext(Dispatchers.IO) {
        val key = if (isEditMode) memo.key else FBRef.memoCategory.push().key!!
        val storageRef = Firebase.storage.reference.child("$key.png")
        val memoRef = FBRef.memoCategory.child(key)

        try {
            // 이미지가 있는 경우
            imageData?.let {
                val uploadTask = storageRef.putBytes(it).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()
                val updateMemo = memo.copy(key = key, imageUrl = downloadUrl)
                memoRef.setValue(updateMemo).await()
            } ?: run {
                // 이미지가 없는 경우
                val updatedMemo = memo.copy(key = key)
                memoRef.setValue(updatedMemo).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}