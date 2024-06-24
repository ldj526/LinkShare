package com.example.linkshare.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.linkshare.BuildConfig
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityIntroBinding
import com.example.linkshare.setting.SetNicknameActivity
import com.example.linkshare.view.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private val ouathClientId = BuildConfig.OUATH_WEB_CLIENT_ID
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var authViewModel: AuthViewModel
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInGoogleResult(task)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val repository = AuthRepository(auth, firestore)
        authViewModel = ViewModelProvider(this, AuthViewModelFactory(repository))[AuthViewModel::class.java]

        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)

        // Google 로그인 옵션 구성
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(ouathClientId)
            .requestEmail()
            .build()
        val kakaoLogin: View = findViewById(R.id.kakao_login)
        val googleLogin: View = findViewById(R.id.google_login)

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.tvJoin.setOnClickListener {
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pwd = binding.etPwd.text.toString()
            authViewModel.signInWithEmailAndPassword(email, pwd)
        }

        googleLogin.setOnClickListener {
            signInGoogle()
        }

        kakaoLogin.setOnClickListener {
            kakaoLogin()
        }

        observeViewModel()
    }

    // Observe ViewModel
    private fun observeViewModel() {
        authViewModel.loginResult.observe(this) { result ->
            result.onSuccess { user ->
                user?.uid?.let { checkNickname(it) }
            }.onFailure {
                Toast.makeText(this, "로그인 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        authViewModel.nicknameExists.observe(this) { exists ->
            if (exists) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else {
                val intent = Intent(this, SetNicknameActivity::class.java)
                startActivity(intent)
            }
        }

        authViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    // Google 로그인 Intent
    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    // Google 로그인 결과 처리
    private fun handleSignInGoogleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            // Google 로그인이 성공했을 때 Firebase 인증을 진행
            val email = account.email
            authViewModel.signInWithGoogle(account.idToken!!, email)
        } catch (e: ApiException) {
            // Google 로그인 실패 처리
            Toast.makeText(this, "로그인 실패: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }

    // 카카오 로그인 방법
    private fun kakaoLogin() {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                handleKakaoLoginResult(token, error)
            }
        } else {
            loginWithKakaoAccount(this)
        }
    }

    // 카카오 로그인 결과 처리
    private fun handleKakaoLoginResult(token: OAuthToken?, error: Throwable?) {
        if (error != null) {
            Toast.makeText(this, "카카오 로그인 실패: $error", Toast.LENGTH_SHORT).show()
            if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                return
            }
        } else if (token != null) {
            Log.d("KakaoCheck", "Kakao Access Token: ${token.accessToken}")
            authViewModel.signInWithKakao(this, token.accessToken)
        }
    }

    // 카카오 계정으로 로그인
    private fun loginWithKakaoAccount(context: Context) {
        UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
            handleKakaoLoginResult(token, error)
        }
    }

    // nickname 존재 여부에 따른 Activity 이동
    private fun checkNickname(uid: String) {
        authViewModel.checkNickname(uid)
    }
}