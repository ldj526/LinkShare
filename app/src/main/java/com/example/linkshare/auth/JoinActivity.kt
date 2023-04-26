package com.example.linkshare.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.databinding.ActivityJoinBinding

class JoinActivity : AppCompatActivity() {

    private var _binding: ActivityJoinBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnJoin.setOnClickListener {
            if (!validateEmail()) {
                return@setOnClickListener
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}