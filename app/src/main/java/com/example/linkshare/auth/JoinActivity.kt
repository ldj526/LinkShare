package com.example.linkshare.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.databinding.ActivityJoinBinding
import com.example.linkshare.view.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class JoinActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var _binding: ActivityJoinBinding? = null
    private val binding get() = _binding!!
    var isGoToJoin = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.btnJoin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pwd = binding.etPwd.text.toString()

            // 이메일 형식 체크
            if (!validateEmail()) {
                isGoToJoin = false
                return@setOnClickListener
            }

            // 비밀번호 형식 체크
            if (!validatePwd()) {
                isGoToJoin = false
                return@setOnClickListener
            }

            if (isGoToJoin) {
                auth.createUserWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(this, "성공", Toast.LENGTH_LONG).show()

                            val intent = Intent(this, MainActivity::class.java)

                            // Activity 이동 후 뒤로가기 눌렀을 때 이전 화면 없애주는 것
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(this, "실패", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }

    // email 형식을 확인하는 기능
    private fun validateEmail(): Boolean {
        val value = binding.etEmail.text.toString()
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

        return if (value.isEmpty()) {
            binding.etEmail.error = "이메일을 입력해주세요."
            false
        } else if (!value.matches(emailPattern.toRegex())) {
            binding.etEmail.error = "이메일 형식이 잘못됐습니다."
            false
        } else {
            binding.etEmail.error = null
            true
        }
    }

    // 비밀번호 형식을 확인하는 기능
    private fun validatePwd(): Boolean {
        val pwd = binding.etPwd.text.toString()
        val pwdCheck = binding.etPwdCheck.text.toString()

        return if (pwd.isEmpty()) {
            binding.etPwd.error = "비밀번호를 입력해주세요."
            false
        } else if (pwdCheck.isEmpty()) {
            binding.etPwdCheck.error = "비밀번호 체크를 입력해주세요."
            false
        } else if (!pwd.equals(pwdCheck)) {
            binding.etPwd.error = "비밀번호가 같은지 확인해주세요"
            binding.etPwdCheck.error = "비밀번호가 같은지 확인해주세요"
            false
        } else {
            binding.etPwd.error = null
            binding.etPwdCheck.error = null
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}