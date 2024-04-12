package com.example.linkshare.link

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.linkshare.util.FBRef
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LinkRepo {

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

    // Firebase database로부터 내가 공유받은 링크 목록 가져오기
    fun getSharedListData(uid: String): LiveData<MutableList<Link>> {
        val liveData = MutableLiveData<MutableList<Link>>()

        FBRef.sharedLinkCategory.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val linkList = mutableListOf<Link>()
                // Get Post object and use the values to update the UI
                for (dataModel in snapshot.children) {
                    // Memo 형식의 데이터 받기
                    val item = dataModel.getValue(Link::class.java)
                    item?.let {
                        if (it.shareUid == uid) {
                            linkList.add(item)
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

    // 링크 저장
    suspend fun saveLink(link: Link, imageData: ByteArray?, isEditMode: Boolean): Boolean = withContext(Dispatchers.IO) {
        val key = if (isEditMode) link.key else FBRef.linkCategory.push().key!!
        val storageRef = Firebase.storage.reference.child("$key.png")
        val linkRef = FBRef.linkCategory.child(key)

        try {
            val updateLink = link.copy(key = key)
            val downloadUrl = imageData?.let {
                storageRef.putBytes(it).await().storage.downloadUrl.await().toString()
            }
            val finalLink = updateLink.copy(imageUrl = downloadUrl ?: link.imageUrl)
            linkRef.setValue(finalLink).await()
            true
        } catch (e: Exception) {
            false
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