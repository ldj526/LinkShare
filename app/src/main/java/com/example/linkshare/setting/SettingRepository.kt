package com.example.linkshare.setting

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
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

class SettingRepository(private val firestore: FirebaseFirestore, private val auth: FirebaseAuth, private val context: Context) {

    // 현재 사용자 가져오기
    suspend fun getCurrentUser(): FirebaseUser? {
        return withContext(Dispatchers.IO) {
            auth.currentUser
        }
    }

    // 사용자의 카카오 이메일 가져오기
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
            val account = GoogleSignIn.getLastSignedInAccount(context)
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
            "구글" -> fetchGoogleUserEmail()
            "카카오" -> fetchKakaoUserEmail()
            "이메일" -> {
                val email = auth.currentUser?.email
                if (email != null) Result.success(email) else Result.failure(Exception("Email not found"))
            }
            else -> Result.failure(Exception("Unknown provider"))
        }
    }

    // 로그인 제공자 가져오기
    fun getLoginProvider(): String {
        val user = auth.currentUser
        return when {
            user?.providerData?.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } == true -> "구글"
            user?.providerData?.any { it.providerId == "oidc.kakao.com" } == true -> "카카오"
            user?.providerData?.any { it.providerId == EmailAuthProvider.PROVIDER_ID } == true -> "이메일"
            else -> "Unknown"
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