package com.example.linkshare.setting

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NicknameRepository(private val firestore: FirebaseFirestore, private val auth: FirebaseAuth, private val context: Context? = null) {

    // Firestore로부터 닉네임 가져오기
    suspend fun getUserNickname(userId: String): Result<String?> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            Result.success(document.getString("nickname"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Firestore에 닉네임 업데이트
    suspend fun updateNickname(userId: String, email: String, nickname: String): Result<Unit> {
        val user = hashMapOf("email" to email, "nickname" to nickname, "lastUpdated" to System.currentTimeMillis())
        return try {
            firestore.collection("users").document(userId).set(user, SetOptions.merge()).await()
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

    // 카카오 사용자 이메일 가져오기
    private suspend fun fetchKakaoUserEmail(): Result<String?> {
        return try {
            val result = suspendCoroutine { continuation ->
                UserApiClient.instance.me { user, error ->
                    if (error != null) {
                        continuation.resumeWith(Result.failure(error))
                    } else {
                        continuation.resume(Result.success(user?.kakaoAccount?.email))
                    }
                }
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 구글 사용자 이메일 가져오기
    private fun fetchGoogleUserEmail(): Result<String?> {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(context!!)
            val email = account?.email
            if (email != null) {
                Result.success(email)
            } else {
                Result.failure(Exception("Google user email is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 로그인 제공자에 따른 이메일 가져오기
    suspend fun fetchUserEmail(providerId: String): Result<String?> {
        return when (providerId) {
            GoogleAuthProvider.PROVIDER_ID -> fetchGoogleUserEmail()
            "oidc.kakao.com" -> fetchKakaoUserEmail()
            EmailAuthProvider.PROVIDER_ID -> {
                val email = auth.currentUser?.email
                if (email != null) Result.success(email) else Result.failure(Exception("Email not found"))
            }
            else -> Result.failure(Exception("Unknown provider"))
        }
    }
}