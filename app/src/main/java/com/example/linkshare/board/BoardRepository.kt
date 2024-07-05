package com.example.linkshare.board

import com.example.linkshare.link.Link
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.FireBaseCollection
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BoardRepository {

    // 카테고리가 일치하는 게시물 가져오기
    suspend fun getEqualCategoryLinkList(category: String, sortOrder: Int): Result<MutableList<Link>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val linkList = mutableListOf<Link>()

            // Firestore에서 특정 카테고리를 포함하는 userLinks 컬렉션의 문서를 가져옵니다.
            val querySnapshot = if (category == "전체보기") {
                FireBaseCollection.firestore.collectionGroup("userLinks").get().await()
            } else {
                FireBaseCollection.firestore.collectionGroup("userLinks")
                    .whereArrayContains("category", category).get().await()
            }

            linkList.addAll(querySnapshot.documents.mapNotNull { it.toObject(Link::class.java) })
            val sortedLinkList = sortLinkList(linkList, sortOrder)
            Result.success(sortedLinkList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 데이터를 정렬하는 함수
    private fun sortLinkList(links: MutableList<Link>, sortOrder: Int): MutableList<Link> {
        return when (sortOrder) {
            0 -> links.sortedByDescending { it.time }
            1 -> links.sortedWith(compareByDescending<Link> { it.shareCount }.thenByDescending { it.time })
            else -> links.sortedByDescending { it.time }
        }.toMutableList()
    }

    // Firebase에서 데이터를 key값 기반으로 받아오는 기능
    suspend fun getLinkDataByKey(uid: String, key: String): Result<Link?> = withContext(Dispatchers.IO) {
        return@withContext try {
            val linkRef = FireBaseCollection.getUserLinksCollection(uid).document(key)
            val linkSnapshot = linkRef.get().await()
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

    // Firebase에 이미 공유되어 있는지 확인
    private suspend fun isLinkAlreadyShared(key: String, currentUserUid: String): Boolean = withContext(Dispatchers.IO) {
        val snapshot = FireBaseCollection.sharedLinkCollection.document(key).get().await()
        val link = snapshot.toObject(Link::class.java)
        return@withContext link?.shareUid == currentUserUid
    }

    // 링크 공유
    suspend fun shareLink(uid: String, key: String, imageData: ByteArray?): Result<Int> = withContext(Dispatchers.IO) {
        val currentUserUid = FBAuth.getUid()
        val linkData = getLinkDataByKey(uid, key).getOrElse { return@withContext Result.failure(it) }

        if (isLinkAlreadyShared(key, currentUserUid)) {
            return@withContext Result.success(linkData!!.shareCount)
        }

        val linkStorageRef = FireBaseCollection.storage.reference.child("$key.png")
        val sharedLinkRef = FireBaseCollection.sharedLinkCollection.document(key)

        return@withContext try {
            val downloadUrl = imageData?.let {
                linkStorageRef.putBytes(it).await()
                linkStorageRef.downloadUrl.await().toString()
            }
            val updatedLink = linkData!!.copy(imageUrl = downloadUrl ?: linkData.imageUrl,
                shareUid = currentUserUid, shareCount = linkData.shareCount + 1, time = FBAuth.getTimestamp())
            sharedLinkRef.set(updatedLink).await()

            // `linkCategory`에 있는 원본 글의 `shareCount` 값을 +1 업데이트하는 과정
            val linkRef = FireBaseCollection.linkCollection.document(key)
            linkRef.update("shareCount", FieldValue.increment(1)).await()
            Result.success(updatedLink.shareCount)
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