package com.example.linkshare.setting

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SettingRepository(private val firestore: FirebaseFirestore, private val auth: FirebaseAuth, private val userApiClient: UserApiClient? = null) {

    // 현재 사용자 가져오기
    suspend fun getCurrentUser(): FirebaseUser? {
        return withContext(Dispatchers.IO) {
            auth.currentUser
        }
    }

    // 사용자의 이메일 가져오기
    fun isEmailAccount(user: FirebaseUser): Boolean {
        return user.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }
    }

    // 사용자의 구글 데이터 가져오기
    fun isGoogleAccount(user: FirebaseUser): Boolean {
        return user.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }
    }

    // 사용자의 카카오 데이터 가져오기
    fun isKakaoAccount(user: FirebaseUser): Boolean {
        return user.providerData.any { it.providerId == "oidc.kakao.com" }
    }

    // 사용자의 카카오 이메일 가져오기
    suspend fun fetchKakaoUserEmail(): Result<String?> {
        val client = userApiClient ?: return Result.failure(IllegalStateException("UserApiClient is not provided"))
        return suspendCoroutine { continuation ->
            client.me { user, error ->
                if (error != null) {
                    continuation.resumeWith(Result.failure(error))
                } else {
                    val email = user?.kakaoAccount?.email
                    continuation.resume(Result.success(email))
                }
            }
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

    // 비밀번호 변경 시 확인을 위한 재인증
    suspend fun reauthenticateUser(email: String, currentPassword: String): Result<Boolean> {
        val user = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        return try {
            withContext(Dispatchers.IO) {
                user.reauthenticate(credential).await()
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 비밀번호 변경
    suspend fun updatePassword(newPassword: String): Result<Boolean> {
        val user = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
        return try {
            withContext(Dispatchers.IO) {
                user.updatePassword(newPassword).await()
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Firestore와 Authentication에서 삭제
    suspend fun deleteUserAccount(): Result<Unit> {
        val user = auth.currentUser
        val userId = user?.uid ?: return Result.failure(Exception("User not logged in"))

        return try {
            firestore.collection("users").document(userId).delete().await()
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}