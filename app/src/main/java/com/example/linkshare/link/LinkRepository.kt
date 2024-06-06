package com.example.linkshare.link

import com.example.linkshare.util.FBRef
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LinkRepository {

    // 자신의 id값과 일치하는 게시물 가져오기
    suspend fun getEqualUidListData(uid: String): Result<MutableList<Link>> = withContext(Dispatchers.IO) {
        try {
            val linkList = mutableListOf<Link>()
            val snapshot = FBRef.linkCategory.get().await()
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
            Result.success(linkList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Firebase database로부터 내가 공유받은 링크 목록 가져오기
    suspend fun getSharedListData(uid: String): Result<MutableList<Link>> = withContext(Dispatchers.IO) {
        try {
            val linkList = mutableListOf<Link>()
            val snapshot = FBRef.sharedLinkCategory.get().await()
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
            Result.success(linkList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Firebase에서 데이터를 key값 기반으로 받아오는 기능
    suspend fun getLinkDataByKey(key: String): Result<Link?> = withContext(Dispatchers.IO) {
        return@withContext try {
            val linkSnapshot = FBRef.linkCategory.child(key).get().await()
            val sharedLinkSnapshot = FBRef.sharedLinkCategory.child(key).get().await()

            // sharedLinkCategory에서 데이터 찾은 경우
            sharedLinkSnapshot.getValue(Link::class.java)?.let {
                Result.success(it)
            } ?: linkSnapshot.getValue(Link::class.java)?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Link not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 이미지 url 받아오는 기능
    suspend fun getImageUrl(key: String): Result<String?> = withContext(Dispatchers.IO) {
        val storageRef = Firebase.storage.reference.child("${key}.png")
        return@withContext try {
            Result.success(storageRef.downloadUrl.await().toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 링크 저장
    suspend fun saveLink(link: Link, imageData: ByteArray?, isEditMode: Boolean): Result<Boolean> = withContext(Dispatchers.IO) {
        val key = if (isEditMode) link.key else FBRef.linkCategory.push().key!!
        val storageRef = Firebase.storage.reference.child("$key.png")
        val linkRef = FBRef.linkCategory.child(key)

        return@withContext try {
            val updateLink = link.copy(key = key)
            val downloadUrl = imageData?.let {
                storageRef.putBytes(it).await().storage.downloadUrl.await().toString()
            }
            val finalLink = updateLink.copy(imageUrl = downloadUrl ?: link.imageUrl)
            linkRef.setValue(finalLink).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 링크 삭제
    suspend fun deleteLink(ref: DatabaseReference, key: String): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            ref.child(key).removeValue().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}