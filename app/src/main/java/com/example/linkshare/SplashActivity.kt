package com.example.linkshare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.linkshare.auth.IntroActivity
import com.example.linkshare.view.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_splash)

        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        if (auth.currentUser?.uid == null) {
            navigateToIntroActivity()
        } else {
            checkNickname()
        }
    }

    // 해당 아이디에 닉네임이 설정되어 있는지 확인
    private fun checkNickname() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            navigateToIntroActivity()
            return
        }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists() && document.data?.containsKey("nickname") == true) {
                    // 닉네임이 존재
                    navigateToMainActivity()
                } else {
                    // 닉네임이 존재하지 않음.
                    signOutGoogle()
                    signOutKakao()
                }
            }
            .addOnFailureListener {
                // 데이터 가져오기 실패
                signOutGoogle()
                signOutKakao()
            }
    }

    // IntroActivity로 이동
    private fun navigateToIntroActivity() {
        val intent = Intent(this, IntroActivity::class.java)
        startActivity(intent)
        finish()
    }

    // MainActivity로 이동
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // 구글 로그아웃
    private fun signOutGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.OUATH_WEB_CLIENT_ID)
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            // Firebase 로그아웃
            FirebaseAuth.getInstance().signOut()
            // IntroActivity로 이동
            navigateToIntroActivity()
        }
    }

    // 카카오 로그아웃
    private fun signOutKakao() {
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e("KakaoLogout", "로그아웃 실패.")
            } else {
                // Firebase 로그아웃
                FirebaseAuth.getInstance().signOut()
                // IntroActivity로 이동
                navigateToIntroActivity()
            }
        }
    }
}