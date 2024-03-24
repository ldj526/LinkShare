package com.example.linkshare.board

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.linkshare.link.Link
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

class BoardRepo {

    // 카테고리가 일치하는 게시물 가져오기
    fun getEqualCategoryListData(category: String): LiveData<MutableList<Link>> {
        val liveData = MutableLiveData<MutableList<Link>>()

        FBRef.linkCategory.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val linkList = mutableListOf<Link>()
                // Get Post object and use the values to update the UI
                for (dataModel in snapshot.children) {
                    // Memo 형식의 데이터 받기
                    val item = dataModel.getValue(Link::class.java)
                    item?.let {
                        if (!it.category.isNullOrEmpty() && it.category.contains(category)) {
                            linkList.add(it)
                        }
                    }
                }
                linkList.sortByDescending { it.time }
                liveData.value = linkList
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
        return liveData
    }

    // 자신의 id값과 일치하는 게시물 가져오기
    fun getEqualUidListData(uid: String): LiveData<MutableList<Link>> {
        val liveData = MutableLiveData<MutableList<Link>>()

        FBRef.linkCategory.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val linkList = mutableListOf<Link>()
                // Get Post object and use the values to update the UI
                for (dataModel in snapshot.children) {
                    // Memo 형식의 데이터 받기
                    val item = dataModel.getValue(Link::class.java)
                    item?.let {
                        if (it.uid == uid) {
                            linkList.add(it)
                        }
                    }
                }
                linkList.sortByDescending { it.time }
                liveData.value = linkList
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        return liveData
    }

    // Firebase database로부터 모든 사용자의 링크 목록 가져오기
    fun getAllLinkListData(): LiveData<MutableList<Link>> {
        val liveData = MutableLiveData<MutableList<Link>>()

        FBRef.linkCategory.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val linkList = mutableListOf<Link>()
                // Get Post object and use the values to update the UI
                for (dataModel in snapshot.children) {
                    // Memo 형식의 데이터 받기
                    val item = dataModel.getValue(Link::class.java)
                    item?.let { linkList.add(it) }
                }
                linkList.sortByDescending { it.time }
                liveData.value = linkList
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        return liveData
    }

    // Firebase에서 데이터를 key값 기반으로 받아오는 기능
    suspend fun getLinkDataByKey(key: String): Link? = withContext(Dispatchers.IO) {
        val linkSnapshot = FBRef.linkCategory.child(key).get().await()
        val sharedLinkSnapshot = FBRef.sharedLinkCategory.child(key).get().await()

        // linkCategory에서 데이터 찾은 경우
        linkSnapshot.getValue(Link::class.java)?.let {
            return@withContext it
        }
        // sharedLinkCategory에서 데이터 찾은 경우
        sharedLinkSnapshot.getValue(Link::class.java)?.let {
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
    private suspend fun isLinkAlreadyShared(key: String, currentUserUid: String): Boolean = withContext(Dispatchers.IO) {
        val snapshot = FBRef.sharedLinkCategory.child(key).get().await()
        val link = snapshot.getValue(Link::class.java)
        return@withContext link?.shareUid == currentUserUid
    }

    // 링크 공유
    suspend fun shareLink(key: String, imageData: ByteArray?): Pair<ShareResult, Int> = withContext(Dispatchers.IO) {
        val currentUserUid = FBAuth.getUid()
        val linkData = getLinkDataByKey(key) ?: return@withContext Pair(ShareResult.FAILURE, 0)

        if (isLinkAlreadyShared(key, currentUserUid)) {
            return@withContext Pair(ShareResult.ALREADY_SHARED, linkData.shareCount)
        }
        val linkStorageRef = Firebase.storage.reference.child("$key.png")
        val sharedLinkRef = FBRef.sharedLinkCategory.child(key)

        try {
            val downloadUrl = imageData?.let {
                linkStorageRef.putBytes(it).await()
                linkStorageRef.downloadUrl.await().toString()
            }
            val updatedLink = linkData.copy(imageUrl = downloadUrl ?: linkData.imageUrl,
                shareUid = currentUserUid, shareCount = linkData.shareCount + 1, time = FBAuth.getTime())
            sharedLinkRef.setValue(updatedLink).await()

            // `linkCategory`에 있는 원본 글의 `shareCount` 값을 +1 업데이트하는 과정
            val linkRef = FBRef.linkCategory.child(key).child("shareCount")
            linkRef.setValue(ServerValue.increment(1)).await()
            Pair(ShareResult.SUCCESS, updatedLink.shareCount)
        } catch (e: Exception) {
            Pair(ShareResult.FAILURE, linkData.shareCount)
        }
    }

    // 링크 삭제
    suspend fun deleteLink(ref: DatabaseReference, key: String): Boolean = withContext(Dispatchers.IO) {
        try {
            ref.child(key).removeValue().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}