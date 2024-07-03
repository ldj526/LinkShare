package com.example.linkshare.search

import com.example.linkshare.link.Link
import com.example.linkshare.util.FBRef
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SearchRepository(private val userUid: String, private val firestore: FirebaseFirestore) {

    private val userSearchCollection = firestore.collection("users").document(userUid).collection("search_queries")
    private val popularSearchCollection = firestore.collection("popular_search_queries")
    private val searchHistoryCollection = firestore.collection("users").document(userUid).collection("search_history")


    // 링크 검색하기
    suspend fun searchLinks(searchText: String, searchOption: String): Result<MutableList<Link>> = withContext(
        Dispatchers.IO) {
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

    // 최근 검색어에 저장
    suspend fun saveSearchQuery(query: String): Result<Unit> {
        return try {
            val userQueryRef = userSearchCollection.document(query)
            val popularQueryRef = popularSearchCollection.document(query)
            val historyQueryRef = searchHistoryCollection.document(query)
            val timestamp = System.currentTimeMillis()

            firestore.runTransaction { transaction ->
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
            val querySnapshot = popularSearchCollection
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
            val snapshot = popularSearchCollection
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
            userSearchCollection.document(query).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 최근 검색어 가져오기
    suspend fun getLatestSearchQueries(): Result<List<SearchQuery>> {
        return try {
            val snapshot = userSearchCollection
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