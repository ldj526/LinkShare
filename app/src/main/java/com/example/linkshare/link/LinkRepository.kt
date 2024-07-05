package com.example.linkshare.link

import com.example.linkshare.util.FireBaseCollection
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LinkRepository {

    // 자신의 id값과 일치하는 게시물 가져오기
    suspend fun getEqualUidListData(uid: String): Result<MutableList<Link>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val userLinksCollection = FireBaseCollection.getUserLinksCollection(uid)
            val querySnapshot = userLinksCollection.get().await()
            val linkList = querySnapshot.documents.mapNotNull { it.toObject(Link::class.java) }.toMutableList()
            linkList.sortedByDescending { it.time }
            Result.success(linkList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Firebase database로부터 내가 공유받은 링크 목록 가져오기
    suspend fun getSharedListData(uid: String): Result<MutableList<Link>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val querySnapshot = FireBaseCollection.sharedLinkCollection.whereEqualTo("shareUid", uid).get().await()
            val linkList = querySnapshot.documents.mapNotNull { it.toObject(Link::class.java) }.toMutableList()
            linkList.sortByDescending { it.time }
            Result.success(linkList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Firebase에서 데이터를 key값 기반으로 받아오는 기능
    suspend fun getLinkDataByKey(uid: String, key: String): Result<Link?> = withContext(Dispatchers.IO) {
        return@withContext try {
            val linkSnapshot = FireBaseCollection.getUserLinksCollection(uid).document(key).get().await()
            val link = linkSnapshot.toObject(Link::class.java)
            Result.success(link)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 이미지 url 받아오는 기능
    suspend fun getImageUrl(key: String): Result<String?> = withContext(Dispatchers.IO) {
        val storageRef = FireBaseCollection.storage.reference.child("${key}.png")
        return@withContext try {
            Result.success(storageRef.downloadUrl.await().toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 링크 저장
    suspend fun saveLink(link: Link, imageData: ByteArray?, isEditMode: Boolean): Result<Boolean> = withContext(Dispatchers.IO) {
        val uid = link.uid
        val userCollection = FireBaseCollection.getUserLinksCollection(uid)

        val key = if (isEditMode) link.key else userCollection.document().id
        val storageRef = Firebase.storage.reference.child("$key.png")
        val linkRef = userCollection.document(key)

        return@withContext try {
            val updateLink = link.copy(key = key)
            val downloadUrl = imageData?.let {
                storageRef.putBytes(it).await().storage.downloadUrl.await().toString()
            }
            val finalLink = updateLink.copy(imageUrl = downloadUrl ?: link.imageUrl)
            linkRef.set(finalLink).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 링크 삭제
    suspend fun deleteLink(uid: String, key: String): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            val linkRef = FireBaseCollection.getUserLinksCollection(uid).document(key)
            linkRef.delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}