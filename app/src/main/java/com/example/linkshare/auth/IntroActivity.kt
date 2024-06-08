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
import com.example.linkshare.BuildConfig
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityIntroBinding
import com.example.linkshare.view.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private lateinit var auth: FirebaseAuth
    private val ouathClientId = BuildConfig.OUATH_WEB_CLIENT_ID
    private lateinit var googleSignInClient: GoogleSignInClient
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }
    }

    // Google 로그인 결과 처리
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            // Google 로그인이 성공했을 때 Firebase 인증을 진행
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            // Google 로그인 실패 처리
            Toast.makeText(this, "로그인 실패: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)

        // Initialize Firebase Auth
        auth = Firebase.auth

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

            auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    checkNickname(currentUser?.uid)
                } else {
                    Toast.makeText(this, "로그인 실패", Toast.LENGTH_LONG).show()
                }
            }
        }

        googleLogin.setOnClickListener {
            signInGoogle()
        }

        kakaoLogin.setOnClickListener {
            kakaoLogin()
        }
    }

    private fun kakaoLogin() {
        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Toast.makeText(this, "카카오계정으로 로그인 실패 : $error", Toast.LENGTH_SHORT).show()

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                    loginWithKaKaoAccount(this)
                } else if (token != null) {
                    getCustomToken(token.accessToken)
                }
            }
        } else {
            loginWithKaKaoAccount(this)
        }
    }

    // 카카오 계정으로 로그인
    private fun loginWithKaKaoAccount(context: Context) {
        UserApiClient.instance.loginWithKakaoAccount(context) { token: OAuthToken?, error: Throwable? ->
            if (token != null) {
                getCustomToken(token.accessToken)
            }
        }
    }

    // 카카오 로그인에 대한 Token 받아오고 Firebase Authentication에 인증
    private fun getCustomToken(accessToken: String) {
        val functions: FirebaseFunctions = Firebase.functions("asia-northeast3")

        val data = hashMapOf(
            "token" to accessToken
        )
        Log.d("KakaoCheck", "functions: $functions, token: $accessToken, data: $data")

        functions.getHttpsCallable("kakaoCustomAuth").call(data).addOnCompleteListener { task ->
                try {
                    // 호출 성공
                    val result = task.result?.data as HashMap<*, *>
                    var mKey: String? = null
                    for (key in result.keys) {
                        mKey = key.toString()
                    }
                    val customToken = result[mKey!!].toString()

                    // 호출 성공해서 반환받은 커스텀 토큰으로 Firebase Authentication 인증받기
                    firebaseAuthWithKakao(customToken)
                } catch (e: RuntimeExecutionException) {
                    // 호출 실패
                    Toast.makeText(this, "토큰 호출 실패: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // 토큰을 통해 firebase authentication 인증
    private fun firebaseAuthWithKakao(customToken: String) {
        auth.signInWithCustomToken(customToken).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Firebase Authentication 인증 성공 후 로직
                val currentUser = auth.currentUser
                checkNickname(currentUser?.uid)
            } else {
                // 실패 후 로직
                Toast.makeText(this, "로그인 실패", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Google 로그인 Intent
    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    // Google 로그인 후 받은 ID 토큰으로 Firebase 사용자 인증 정보 생성
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    checkNickname(currentUser?.uid)
                } else {
                    Toast.makeText(this, "로그인 실패", Toast.LENGTH_LONG).show()
                }
            }
    }

    // nickname 존재 여부에 따른 Activity 이동
    private fun checkNickname(uid: String?) {
        if (uid == null) {
            Toast.makeText(this, "유저 정보 오류", Toast.LENGTH_LONG).show()
            return
        }

        val docRef = FirebaseFirestore.getInstance().collection("users").document(uid)
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists() && document.data?.containsKey("nickname") == true) {
                    // 닉네임이 존재하면 MainActivity로 이동
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                } else {
                    // 닉네임이 없으면 NicknameActivity로 이동
                    val intent = Intent(this, NicknameActivity::class.java)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this, "닉네임 확인 실패", Toast.LENGTH_LONG).show()
            }
        }
    }
}