package com.example.linkshare.main

import com.example.linkshare.link.Link
import com.example.linkshare.util.FireBaseCollection
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainViewRepository {

    // Top 조회수/공유수 데이터를 가져오는 함수
    suspend fun getTopLinks(timeRange: TimeRange, sortBy: SortBy): Result<MutableList<Link>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val currentTime = System.currentTimeMillis()
            val timeRangeMillis = when (timeRange) {
                TimeRange.DAILY -> 24L * 60 * 60 * 1000 // 86,400,000
                TimeRange.WEEKLY -> 7L * 24 * 60 * 60 * 1000    // 604,800,000
                TimeRange.MONTHLY -> 30L * 24 * 60 * 60 * 1000  // 2,592,000,000
            }
            val startTime = currentTime - timeRangeMillis

            val query = FireBaseCollection.firestore.collectionGroup("userLinks")
                .whereGreaterThanOrEqualTo("time", startTime)
                .orderBy("time", Query.Direction.DESCENDING)
                .orderBy(sortBy.field, Query.Direction.DESCENDING)
                .limit(10)

            val querySnapshot = query.get().await()

            val linkList = querySnapshot.documents.mapNotNull { it.toObject(Link::class.java) }.toMutableList()
            Result.success(linkList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    enum class TimeRange {
        DAILY, WEEKLY, MONTHLY
    }

    enum class SortBy(val field: String) {
        VIEWS("viewCount"),
        SHARES("shareCount")
    }
}