package com.example.linkshare.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.databinding.ActivityIntroBinding
import com.example.linkshare.view.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class IntroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var _binding: ActivityIntroBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        binding.joinText.setOnClickListener {
            // JoinActivity 로 이동
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.emailArea.text.toString()
            val pwd = binding.pwdArea.text.toString()

            auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_LONG).show()
                    // MainActivity 로 이동
                    val intent = Intent(this, MainActivity::class.java)
                    // 뒤로가기 눌렀을 시 이전 화면 스택을 지워줌
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "로그인 실패", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}