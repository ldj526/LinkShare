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
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityJoinBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore


class JoinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJoinBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isEmailDuplicated = true
    private var isNicknameDuplicated = true
    private var emailFlag = false
    private var pwdFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        binding.etEmailLayout.editText?.addTextChangedListener(emailListener)
        binding.etPwdLayout.editText?.addTextChangedListener(pwdListener)
        binding.etNicknameLayout.editText?.addTextChangedListener(nicknameListener)
        binding.etEmail.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val email = binding.etEmail.text.toString()
                checkEmailDuplication(email)
            }
        }

        binding.btnCheckNickname.setOnClickListener {
            checkNicknameDuplication()
        }

        binding.btnJoin.setOnClickListener {
            signUpUser()
        }

        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    // 닉네임 중복 확인
    private fun checkNicknameDuplication() {
        val nickname = binding.etNickname.text.toString()

        if (nickname == "") {
            showErrorFeedback(binding.etNicknameLayout, "닉네임을 입력하세요.")
            isNicknameDuplicated = true
            flagCheck()
        } else {
            db.collection("users").whereEqualTo("nickname", nickname).get()
                .addOnSuccessListener { documents ->
                    isNicknameDuplicated = documents.isEmpty.not()
                    if (documents.isEmpty) {
                        showPositiveFeedback(binding.etNicknameLayout, "사용 가능한 닉네임입니다.")
                    } else {
                        showErrorFeedback(binding.etNicknameLayout, "중복된 닉네임입니다.")
                    }
                    flagCheck()
                }.addOnFailureListener {
                showErrorFeedback(binding.etNicknameLayout, "닉네임 중복을 확인하는데 실패했습니다.")
                isNicknameDuplicated = true
                flagCheck()
            }
        }
    }

    // 이메일 중복 확인
    private fun checkEmailDuplication(email: String) {
        when {
            email.isEmpty() -> {
                showErrorFeedback(binding.etEmailLayout, "이메일을 입력해주세요.")
                emailFlag = false
                flagCheck()
            }

            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showErrorFeedback(binding.etEmailLayout, "이메일 형식이 잘못됐습니다.")
                emailFlag = false
                flagCheck()
            }

            else -> {
                db.collection("users").whereEqualTo("email", email).get().addOnSuccessListener { documents ->
                    isEmailDuplicated = documents.isEmpty.not()
                    if (documents.isEmpty) {
                        showPositiveFeedback(binding.etEmailLayout, "사용 가능한 이메일입니다.")
                        emailFlag = true
                    } else {
                        showErrorFeedback(binding.etEmailLayout, "이미 사용 중인 이메일입니다.")
                        emailFlag = false
                    }
                    flagCheck()
                }.addOnFailureListener {
                    showErrorFeedback(binding.etEmailLayout, "이메일 중복 확인에 실패했습니다.")
                    isEmailDuplicated = true
                    emailFlag = false
                    flagCheck()
                }
            }
        }
    }

    // 회원가입
    private fun signUpUser() {
        val email = binding.etEmail.text.toString()
        val pwd = binding.etPwd.text.toString()
        val nickname = binding.etNickname.text.toString()
        auth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // 사용자 계정 생성 성공
                val user = hashMapOf("email" to email, "nickname" to nickname)
                // Firestore에 사용자 정보 저장
                db.collection("users").document(auth.currentUser!!.uid).set(user).addOnSuccessListener {
                        Toast.makeText(this, "성공", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, IntroActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                    }.addOnFailureListener {
                    Toast.makeText(this, "저장하는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "실패", Toast.LENGTH_SHORT).show()
            }
        }
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
            val pwdPattern = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&.])[A-Za-z[0-9]$@$!%*#?&.]+$"
            if (s != null) {
                when {
                    s.isEmpty() -> {
                        showErrorFeedback(binding.etPwdLayout, "비밀번호를 입력해주세요.")
                        pwdFlag = false
                    }

                    else -> {
                        when {
                            (s.length < 8 || s.length > 20) && s.matches(pwdPattern.toRegex()) -> {
                                showErrorFeedback(binding.etPwdLayout, "8자리 이상 20자리 이하로 입력해주세요")
                                pwdFlag = false
                            }

                            (s.length < 8 || s.length > 20) && !s.matches(pwdPattern.toRegex()) -> {
                                showErrorFeedback(binding.etPwdLayout, "8자리 이상 20자리 이하로 입력해주세요\n" +
                                        "영문, 숫자, 특수문자를 1개 이상 입력해주세요.")
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
                    }
                }
                flagCheck()
            }
        }
    }

    // email, password가 올바를 경우 회원가입 버튼 활성화
    private fun flagCheck() {
        binding.btnJoin.isEnabled = emailFlag && pwdFlag && !isEmailDuplicated && !isNicknameDuplicated
        Log.d("joinCheck", "$emailFlag, $pwdFlag, ${!isEmailDuplicated}, ${!isNicknameDuplicated}")
    }
}