package com.example.linkshare.search

import android.util.Log
import com.example.linkshare.link.Link
import com.example.linkshare.util.FireBaseCollection
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SearchRepository(private val userUid: String) {

    // 링크 검색하기
    suspend fun searchLinks(searchText: String, searchOption: String): Result<MutableList<Link>> = withContext(
        Dispatchers.IO) {
        return@withContext try {
            val linkList = mutableListOf<Link>()
            val searchLower = searchText.lowercase()
            val firestore = FireBaseCollection.firestore

            // Firestore에서 검색어와 일치하는 userLinks 컬렉션의 문서를 가져옵니다.
            val querySnapshot = when (searchOption) {
                "제목" -> firestore.collectionGroup("userLinks")
                    .whereGreaterThanOrEqualTo("title", searchLower)
                    .whereLessThanOrEqualTo("title", "$searchLower\uf8ff")
                    .get().await()
                "내용" -> firestore.collectionGroup("userLinks")
                    .whereGreaterThanOrEqualTo("content", searchLower)
                    .whereLessThanOrEqualTo("content", "$searchLower\uf8ff")
                    .get().await()
                "링크" -> firestore.collectionGroup("userLinks")
                    .whereGreaterThanOrEqualTo("link", searchLower)
                    .whereLessThanOrEqualTo("link", "$searchLower\uf8ff")
                    .get().await()
                else -> throw IllegalArgumentException("Invalid search option")
            }
            linkList.addAll(querySnapshot.documents.mapNotNull { it.toObject(Link::class.java) })
            linkList.sortByDescending { it.time }
            Result.success(linkList)
        } catch (e: Exception) {
            Log.e("SearchRepository", "Error fetching links", e)
            Result.failure(e)
        }
    }

    // 최근 검색어에 저장
    suspend fun saveSearchQuery(query: String): Result<Unit> {
        return try {
            val userQueryRef = FireBaseCollection.getUserCurrentSearchCollection(userUid).document(query)
            val popularQueryRef = FireBaseCollection.popularSearchCollection.document(query)
            val historyQueryRef = FireBaseCollection.getAllUserSearchCollection(userUid).document(query)
            val timestamp = System.currentTimeMillis()

            FireBaseCollection.firestore.runTransaction { transaction ->
                // 최근 검색어 업데이트
                val userSearchSnapshot = transaction.get(userQueryRef)
                val popularSnapshot = transaction.get(popularQueryRef)
                val historySnapshot = transaction.get(historyQueryRef)

                if (userSearchSnapshot.exists()) {
                    transaction.delete(userQueryRef)
                }
                transaction.set(userQueryRef, mapOf("timestamp" to timestamp))

                // 동일 사용자가 같은 검색어 입력 시 검색횟수에 추가되지 않음.
                if (!historySnapshot.exists()) {
                    transaction.set(historyQueryRef, mapOf("timestamp" to timestamp))

                    // 인기 검색어 업데이트
                    if (popularSnapshot.exists()) {
                        val newCount = popularSnapshot.getLong("count")!! + 1
                        transaction.update(popularQueryRef, "count", newCount, "timestamp", timestamp)
                    } else {
                        transaction.set(popularQueryRef, mapOf("count" to 1, "timestamp" to timestamp))
                    }
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 자동완성을 위한 검색어 query 가져오기
    suspend fun getAutoCompleteSuggestions(query: String): Result<List<String>> {
        return try {
            val querySnapshot = FireBaseCollection.popularSearchCollection
                .orderBy(FieldPath.documentId())
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(10)
                .get()
                .await()

            val suggestions = querySnapshot.documents.map { document ->
                document.id to (document.getLong("count") ?: 0L)
            }
                .sortedByDescending { it.second }
                .map { it.first }

            Result.success(suggestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 인기 있는 검색어 가져오기
    suspend fun getPopularSearchQueries(): Result<List<SearchQuery>> {
        return try {
            val snapshot = FireBaseCollection.popularSearchCollection
                .orderBy("count", Query.Direction.DESCENDING)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(10)
                .get()
                .await()

            val queries = snapshot.documents.map { document ->
                val popularQuery = document.toObject(SearchQuery::class.java)
                if (popularQuery != null) {
                    popularQuery.query = document.id
                }
                popularQuery!!
            }
            Result.success(queries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 최근 검색어 삭제
    suspend fun deleteSearchQuery(query: String): Result<Unit> {
        return try {
            FireBaseCollection.getUserCurrentSearchCollection(userUid).document(query).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 최근 검색어 가져오기
    suspend fun getLatestSearchQueries(): Result<List<SearchQuery>> {
        return try {
            val snapshot = FireBaseCollection.getUserCurrentSearchCollection(userUid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(15)
                .get()
                .await()

            val queries = snapshot.documents.map { document ->
                val searchQuery = document.toObject(SearchQuery::class.java)
                if (searchQuery != null) {
                    searchQuery.query = document.id
                }
                searchQuery!!
            }
            Result.success(queries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}