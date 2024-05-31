package com.example.linkshare.setting

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SettingRepository(private val firestore: FirebaseFirestore) {

    // Firestore에 닉네임 업데이트
    suspend fun updateNickname(userId: String, email: String, nickname: String): Result<Unit> {
        val user = hashMapOf("email" to email, "nickname" to nickname, "lastUpdated" to System.currentTimeMillis())
        return try {
            firestore.collection("users").document(userId).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Firestore에 저장되어 있는 변경 날짜 가져오기
    suspend fun getUserLastUpdated(userId: String): Result<Long> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            Result.success(document.getLong("lastUpdated") ?: 0L)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Firestore에 저장되어 있는 nickname 비교하며 중복체크
    suspend fun isNicknameDuplicated(nickname: String): Result<Boolean> {
        return try {
            val documents = firestore.collection("users")
                .whereEqualTo("nickname", nickname).get().await()
            Result.success(documents.isEmpty.not())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Firestore로부터 닉네임 가져오기
    suspend fun getUserNickname(userId: String): Result<String?> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            Result.success(document.getString("nickname"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}