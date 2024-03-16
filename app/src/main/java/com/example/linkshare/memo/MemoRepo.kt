package com.example.linkshare.memo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.FBRef
import com.example.linkshare.util.ShareResult
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MemoRepo {

    // 자신의 id값과 일치하는 게시물 가져오기
    fun getMemoData(uid: String): LiveData<MutableList<Memo>> {
        val liveData = MutableLiveData<MutableList<Memo>>()

        FBRef.memoCategory.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val memoList = mutableListOf<Memo>()
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
                liveData.value = memoList
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        return liveData
    }

    // Firebase database로부터 내가 공유받은 memo 가져오기
    fun getBoardData(uid: String): LiveData<MutableList<Memo>> {
        val liveData = MutableLiveData<MutableList<Memo>>()

        FBRef.boardCategory.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val memoList = mutableListOf<Memo>()
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
                liveData.value = memoList
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        return liveData
    }
    // Firebase database로부터 모든 사용자의 memo 가져오기
    fun getAllMemoData(): LiveData<MutableList<Memo>> {
        val liveData = MutableLiveData<MutableList<Memo>>()

        FBRef.memoCategory.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val memoList = mutableListOf<Memo>()
                // Get Post object and use the values to update the UI
                for (dataModel in snapshot.children) {
                    // Memo 형식의 데이터 받기
                    val item = dataModel.getValue(Memo::class.java)
                    item?.let { memoList.add(it) }
                }
                memoList.sortByDescending { it.time }
                liveData.value = memoList
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        return liveData
    }

    // 메모, 게시글에서 볼 데이터를 key값 기반으로 받아오는 기능
    suspend fun getMemoDataByKey(key: String): Memo? = withContext(Dispatchers.IO) {
        val memoSnapshot = FBRef.memoCategory.child(key).get().await()
        val boardSnapshot = FBRef.boardCategory.child(key).get().await()

        // memoCategory에서 데이터 찾은 경우
        memoSnapshot.getValue(Memo::class.java)?.let {
            return@withContext it
        }
        // boardCategory에서 데이터 찾은 경우
        boardSnapshot.getValue(Memo::class.java)?.let {
            return@withContext it
        }
        null
    }

    // 이미지 url 받아오는 기능
    suspend fun getImageUrl(key: String): String? = withContext(Dispatchers.IO) {
        val storageRef = Firebase.storage.reference.child("${key}.png")
        try {
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    // Firebase에 이미 공유되어 있는지 확인
    private suspend fun isMemoAlreadyShared(memoKey: String, currentUserUid: String): Boolean = withContext(Dispatchers.IO) {
        val snapshot = FBRef.boardCategory.child(memoKey).get().await()
        val memo = snapshot.getValue(Memo::class.java)
        return@withContext memo?.shareUid == currentUserUid
    }

    // 메모 공유
    suspend fun shareMemo(memoKey: String, imageData: ByteArray?): Pair<ShareResult, Int> = withContext(Dispatchers.IO) {
        val currentUserUid = FBAuth.getUid()
        val memoData = getMemoDataByKey(memoKey) ?: return@withContext Pair(ShareResult.FAILURE, 0)

        if (isMemoAlreadyShared(memoKey, currentUserUid)) {
            return@withContext Pair(ShareResult.ALREADY_SHARED, memoData.shareCount)
        }
        val storageRef = Firebase.storage.reference.child("$memoKey.png")
        val boardRef = FBRef.boardCategory.child(memoKey)

        try {
            val downloadUrl = imageData?.let {
                storageRef.putBytes(it).await()
                storageRef.downloadUrl.await().toString()
            }
            val updatedMemo = memoData.copy(imageUrl = downloadUrl ?: memoData.imageUrl,
                shareUid = currentUserUid, shareCount = memoData.shareCount + 1, time = FBAuth.getTime())
            boardRef.setValue(updatedMemo).await()

            // `memoCategory`에 있는 원본 글의 `shareCount` 값을 +1 업데이트하는 과정
            val memoRef = FBRef.memoCategory.child(memoKey).child("shareCount")
            memoRef.setValue(ServerValue.increment(1)).await()
            Pair(ShareResult.SUCCESS, updatedMemo.shareCount)
        } catch (e: Exception) {
            Pair(ShareResult.FAILURE, memoData.shareCount)
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
                storageRef.putBytes(it).await()
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

    // 메모 삭제
    suspend fun deleteMemo(category: DatabaseReference, key: String): Boolean = withContext(Dispatchers.IO) {
        try {
            category.child(key).removeValue().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}