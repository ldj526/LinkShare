package com.example.linkshare

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.databinding.ActivityJoinBinding

class JoinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJoinBinding
    private var emailFlag = false
    private var pwdFlag = false
    private var pwdCheckFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etEmailLayout.editText?.addTextChangedListener(emailListener)
        binding.etPwdLayout.editText?.addTextChangedListener(pwdListener)
        binding.etPwdCheckLayout.editText?.addTextChangedListener(pwdListener)
        binding.btnJoin.setOnClickListener {
            val intent = Intent(this, IntroActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }

    // email check listener
    private val emailListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            if (s != null) {
                when {
                    s.isEmpty() -> {
                        binding.etEmailLayout.error = "이메일을 입력해주세요."
                        emailFlag = false
                    }

                    !s.matches(emailPattern.toRegex()) -> {
                        binding.etEmailLayout.error = "이메일 형식이 잘못됐습니다."
                        emailFlag = false
                    }

                    else -> {
                        binding.etEmailLayout.error = null
                        emailFlag = true
                    }
                }
                flagCheck()
            }
        }
    }

    // password check listener
    private val pwdListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                when {
                    s.isEmpty() -> {
                        binding.etPwdLayout.error = "비밀번호를 입력해주세요."
                    }

                    s.isNotEmpty() -> {
                        binding.etPwdLayout.error = null
                        when {
                            binding.etPwdLayout.editText?.text.toString() != ""
                                    && binding.etPwdLayout.editText?.text.toString() != binding.etPwdCheckLayout.editText?.text.toString() -> {
                                binding.etPwdCheckLayout.error = "비밀번호가 일치하지 않습니다"
                                pwdCheckFlag = false
                                pwdFlag = true
                            }

                            else -> {
                                binding.etPwdCheckLayout.error = null
                                pwdCheckFlag = true
                            }
                        }
                    }
                }
                flagCheck()
            }
        }

    }

    // email, password가 올바를 경우 회원가입 버튼 활성화
    private fun flagCheck() {
        binding.btnJoin.isEnabled = emailFlag && pwdFlag && pwdCheckFlag
    }
}