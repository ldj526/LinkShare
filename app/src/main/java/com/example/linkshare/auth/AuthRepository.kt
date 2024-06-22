package com.example.linkshare.auth

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepository(private val auth: FirebaseAuth, private val firestore: FirebaseFirestore) {

    // Email/Password 로그인 성공여부
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(authResult.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 구글로 로그인 성공여부
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser?> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            Result.success(authResult.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithKakao(activity: Activity, token: String): Result<FirebaseUser?> {
        return try {
            val provider = OAuthProvider.newBuilder("oidc.kakao.com")
                .addCustomParameter("prompt", "consent")
                .addCustomParameter("access_token", token)
                .build()
            val authResult = auth.startActivityForSignInWithProvider(activity, provider).await()
            Result.success(authResult.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Firestore에 등록된 닉네임 존재 여부
    suspend fun checkNickname(uid: String): Boolean {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            document.exists()
        } catch (e: Exception) {
            false
        }
    }

    // 카카오 계정의 Email 가져오기
    suspend fun fetchKakaoUserEmail(client: UserApiClient): Result<String?> {
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

    // 이메일 중복 확인
    suspend fun checkEmailDuplication(email: String): Result<Boolean> {
        return try {
            val result = firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()
            Result.success(result.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 닉네임 중복 확인
    suspend fun checkNicknameDuplication(nickname: String): Result<Boolean> {
        return try {
            val result = firestore.collection("users")
                .whereEqualTo("nickname", nickname)
                .get()
                .await()
            Result.success(result.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 회원가입
    suspend fun signUpUser(email: String, password: String, nickname: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("User creation failed")
            val userData = hashMapOf(
                "email" to email,
                "nickname" to nickname
            )
            firestore.collection("users").document(user.uid).set(userData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}