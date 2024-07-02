package com.example.linkshare.search

import com.example.linkshare.link.Link
import com.example.linkshare.util.FBRef
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SearchRepository(private val userUid: String, private val firestore: FirebaseFirestore) {

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
            val searchQuery = SearchQuery(query)
            firestore.collection("users").document(userUid).collection("search_queries")
                .add(searchQuery).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 최근 검색어 삭제
    suspend fun deleteSearchQuery(searchQuery: SearchQuery): Result<Unit> {
        return try {
            val querySnapshot = firestore.collection("users")
                .document(userUid)
                .collection("search_queries")
                .whereEqualTo("query", searchQuery.query)
                .whereEqualTo("timestamp", searchQuery.timestamp)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                firestore.collection("users").document(userUid).collection("search_queries")
                    .document(document.id).delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 최근 검색어 가져오기
    suspend fun getLatestSearchQueries(): Result<List<SearchQuery>> {
        return try {
            val snapshot = firestore.collection("users").document(userUid)
                .collection("search_queries")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(15)
                .get()
                .await()

            val queries = snapshot.documents.map { document ->
                document.toObject(SearchQuery::class.java)!!
            }
            Result.success(queries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}