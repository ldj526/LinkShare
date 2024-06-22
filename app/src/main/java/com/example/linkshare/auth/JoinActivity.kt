package com.example.linkshare.auth

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityJoinBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class JoinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJoinBinding
    private var isEmailDuplicated = true
    private var isNicknameDuplicated = true
    private var emailFlag = false
    private var pwdFlag = false
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val repository = AuthRepository(auth, firestore)
        authViewModel = ViewModelProvider(this, AuthViewModelFactory(repository))[AuthViewModel::class.java]

        observeViewModel()

        binding.etEmailLayout.editText?.addTextChangedListener(emailListener)
        binding.etPwdLayout.editText?.addTextChangedListener(pwdListener)
        binding.etNicknameLayout.editText?.addTextChangedListener(nicknameListener)
        binding.etEmail.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val email = binding.etEmail.text.toString()
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    showErrorFeedback(binding.etEmailLayout, "이메일 형식이 잘못됐습니다.")
                    emailFlag = false
                    flagCheck()
                } else {
                    authViewModel.checkEmailDuplication(email)
                }
            }
        }

        binding.btnCheckNickname.setOnClickListener {
            val nickname = binding.etNickname.text.toString()
            authViewModel.checkNicknameDuplication(nickname)
        }

        binding.btnJoin.setOnClickListener {
            signUpUser()
        }

        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        authViewModel.isEmailDuplicated.observe(this, Observer { result ->
            result.fold(
                onSuccess = {
                    isEmailDuplicated = !it
                    if (it) {
                        showPositiveFeedback(binding.etEmailLayout, "사용 가능한 이메일입니다.")
                        emailFlag = true
                    } else {
                        showErrorFeedback(binding.etEmailLayout, "이미 사용 중인 이메일입니다.")
                        emailFlag = false
                    }
                    flagCheck()
                },
                onFailure = {
                    showErrorFeedback(binding.etEmailLayout, "이메일 중복 확인에 실패했습니다.")
                    isEmailDuplicated = true
                    emailFlag = false
                    flagCheck()
                }
            )
        })

        authViewModel.isNicknameDuplicated.observe(this, Observer { result ->
            result.fold(
                onSuccess = {
                    isNicknameDuplicated = !it
                    if (it) {
                        showPositiveFeedback(binding.etNicknameLayout, "사용 가능한 닉네임입니다.")
                    } else {
                        showErrorFeedback(binding.etNicknameLayout, "중복된 닉네임입니다.")
                    }
                    flagCheck()
                },
                onFailure = {
                    showErrorFeedback(binding.etNicknameLayout, "닉네임 중복 확인에 실패했습니다.")
                    isNicknameDuplicated = true
                    flagCheck()
                }
            )
        })

        authViewModel.signUpResult.observe(this, Observer { result ->
            result.fold(
                onSuccess = {
                    Toast.makeText(this, "회원가입이 성공적으로 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, IntroActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                },
                onFailure = {
                    Toast.makeText(this, "회원가입에 실패했습니다: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            )
        })
    }

    // 회원가입
    private fun signUpUser() {
        val email = binding.etEmail.text.toString()
        val pwd = binding.etPwd.text.toString()
        val nickname = binding.etNickname.text.toString()
        authViewModel.signUpUser(email, pwd, nickname)
    }

    // TextInputLayout 조건 맞을 때
    private fun showPositiveFeedback(inputLayout: TextInputLayout, message: String) {
        inputLayout.error = message
        inputLayout.setErrorTextColor(ContextCompat.getColorStateList(this, R.color.correct_input_layout))
        inputLayout.boxStrokeErrorColor = ContextCompat.getColorStateList(this, R.color.correct_input_layout)
        inputLayout.hintTextColor = ContextCompat.getColorStateList(this, R.color.correct_input_layout)
    }

    // TextInputLayout 조건 틀렸을 때
    private fun showErrorFeedback(inputLayout: TextInputLayout, message: String) {
        inputLayout.error = message
        inputLayout.setErrorTextColor(ColorStateList.valueOf(Color.RED))
        inputLayout.boxStrokeErrorColor = ColorStateList.valueOf(Color.RED)
        inputLayout.hintTextColor = ColorStateList.valueOf(Color.RED)
    }

    // email check listener
    private val emailListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            binding.etEmailLayout.error = null
            isEmailDuplicated = true
        }

        override fun afterTextChanged(s: Editable?) {
            flagCheck()
        }
    }

    // nickname check listener
    private val nicknameListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            binding.etNicknameLayout.error = null
            isNicknameDuplicated = true
        }

        override fun afterTextChanged(s: Editable?) {
            flagCheck()
        }
    }

    // password check listener
    private val pwdListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            validatePassword(s)
        }
    }

    // 비밀번호 유효성 검사
    private fun validatePassword(s: Editable?) {
        val pwdPattern = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&.])[A-Za-z[0-9]$@$!%*#?&.]+$"
        if (s != null) {
            when {
                s.isEmpty() -> {
                    showErrorFeedback(binding.etPwdLayout, "비밀번호를 입력해주세요.")
                    pwdFlag = false
                }
                (s.length < 8 || s.length > 20) && s.matches(pwdPattern.toRegex()) -> {
                    showErrorFeedback(binding.etPwdLayout, "8자리 이상 20자리 이하로 입력해주세요")
                    pwdFlag = false
                }
                (s.length < 8 || s.length > 20) && !s.matches(pwdPattern.toRegex()) -> {
                    showErrorFeedback(binding.etPwdLayout, "8자리 이상 20자리 이하로 입력해주세요\n영문, 숫자, 특수문자를 1개 이상 입력해주세요.")
                    pwdFlag = false
                }
                (s.length in 8..20) && !s.matches(pwdPattern.toRegex()) -> {
                    showErrorFeedback(binding.etPwdLayout, "영문, 숫자, 특수문자를 1개 이상 입력해주세요.")
                    pwdFlag = false
                }
                (s.length in 8..20) && s.matches(pwdPattern.toRegex()) -> {
                    showPositiveFeedback(binding.etPwdLayout, "올바른 비밀번호입니다.")
                    pwdFlag = true
                }
            }
            flagCheck()
        }
    }

    // email, password가 올바를 경우 회원가입 버튼 활성화
    private fun flagCheck() {
        binding.btnJoin.isEnabled = emailFlag && pwdFlag && !isEmailDuplicated && !isNicknameDuplicated
        Log.d("joinCheck", "$emailFlag, $pwdFlag, ${!isEmailDuplicated}, ${!isNicknameDuplicated}")
    }
}