package com.example.linkshare.board

import com.example.linkshare.link.Link
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.FBRef
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BoardRepository {
    // 링크 검색하기
    suspend fun searchLinks(searchText: String, searchOption: String): Result<MutableList<Link>> = withContext(Dispatchers.IO) {
        try {
            val linkList = mutableListOf<Link>()
            val snapshot = FBRef.linkCategory.get().await()
            // Get Post object and use the values to update the UI
            for (dataModel in snapshot.children) {
                // Link 형식의 데이터 받기
                val item = dataModel.getValue(Link::class.java)
                item?.let {
                    val containedLink = when (searchOption) {
                        "제목" -> it.title.contains(searchText, true)
                        "내용" -> it.content.contains(searchText, true)
                        "링크" -> it.link.contains(searchText, true)
                        else -> false
                    }
                    if (containedLink) linkList.add(it)
                }
            }
            linkList.sortByDescending { it.time }
            Result.success(linkList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 카테고리가 일치하는 게시물 가져오기
    suspend fun getEqualCategoryLinkList(category: String, sortOrder: Int): Result<MutableList<Link>> = withContext(Dispatchers.IO) {
        try {
            val linkList = mutableListOf<Link>()
            val snapshot = FBRef.linkCategory.get().await()
            for (dataModel in snapshot.children) {
                val item = dataModel.getValue(Link::class.java)
                item?.let {
                    if (category == "전체보기" || (!it.category.isNullOrEmpty() && it.category.contains(category))) {
                        linkList.add(it)
                    }
                }
            }
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
    suspend fun getLinkDataByKey(key: String): Result<Link?> = withContext(Dispatchers.IO) {
        return@withContext try {
            val linkSnapshot = FBRef.linkCategory.child(key).get().await()
            val sharedLinkSnapshot = FBRef.sharedLinkCategory.child(key).get().await()

            // linkCategory에서 데이터 찾은 경우
            linkSnapshot.getValue(Link::class.java)?.let {
                Result.success(it)
            } ?: sharedLinkSnapshot.getValue(Link::class.java)?.let {// sharedLinkCategory에서 데이터 찾은 경우
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

    // Firebase에 이미 공유되어 있는지 확인
    private suspend fun isLinkAlreadyShared(key: String, currentUserUid: String): Boolean = withContext(Dispatchers.IO) {
        val snapshot = FBRef.sharedLinkCategory.child(key).get().await()
        val link = snapshot.getValue(Link::class.java)
        return@withContext link?.shareUid == currentUserUid
    }

    // 링크 공유
    suspend fun shareLink(key: String, imageData: ByteArray?): Result<Int> = withContext(Dispatchers.IO) {
        val currentUserUid = FBAuth.getUid()
        val linkData = getLinkDataByKey(key).getOrElse { return@withContext Result.failure(it) }

        if (isLinkAlreadyShared(key, currentUserUid)) {
            return@withContext Result.success(linkData!!.shareCount)
        }
        val linkStorageRef = Firebase.storage.reference.child("$key.png")
        val sharedLinkRef = FBRef.sharedLinkCategory.child(key)

        return@withContext try {
            val downloadUrl = imageData?.let {
                linkStorageRef.putBytes(it).await()
                linkStorageRef.downloadUrl.await().toString()
            }
            val updatedLink = linkData!!.copy(imageUrl = downloadUrl ?: linkData.imageUrl,
                shareUid = currentUserUid, shareCount = linkData.shareCount + 1, time = FBAuth.getTime())
            sharedLinkRef.setValue(updatedLink).await()

            // `linkCategory`에 있는 원본 글의 `shareCount` 값을 +1 업데이트하는 과정
            val linkRef = FBRef.linkCategory.child(key).child("shareCount")
            linkRef.setValue(ServerValue.increment(1)).await()
            Result.success(updatedLink.shareCount)
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