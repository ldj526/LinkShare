package com.example.linkshare.setting

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivitySetNicknameBinding
import com.example.linkshare.view.MainActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class SetNicknameActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetNicknameBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var nicknameViewModel: NicknameViewModel
    private var isNicknameDuplicated = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetNicknameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        val nicknameRepository = NicknameRepository(db, auth, this)
        val factory = NicknameViewModelFactory(nicknameRepository)
        nicknameViewModel = ViewModelProvider(this, factory)[NicknameViewModel::class.java]

        observeViewModel()

        binding.etNicknameLayout.editText?.addTextChangedListener(nicknameListener)

        binding.btnCheckNickname.setOnClickListener {
            checkNicknameDuplication()
        }

        binding.btnNext.setOnClickListener {
            createNickname()
        }

        nicknameViewModel.setLoading(false)
    }

    // ViewModel
    private fun observeViewModel() {
        nicknameViewModel.nicknameDuplicationResult.observe(this, Observer { result ->
            result.onSuccess { isDuplicated ->
                isNicknameDuplicated = isDuplicated
                if (isDuplicated) {
                    showErrorFeedback(binding.etNicknameLayout, "중복된 닉네임입니다.")
                } else {
                    showPositiveFeedback(binding.etNicknameLayout, "사용 가능한 닉네임입니다.")
                }
                flagCheck()
            }.onFailure {
                showErrorFeedback(binding.etNicknameLayout, "닉네임 중복을 확인하는데 실패했습니다.")
                isNicknameDuplicated = true
                flagCheck()
            }
        })

        nicknameViewModel.updateNicknameResult.observe(this, Observer { result ->
            result.onSuccess {
                Toast.makeText(this, "성공", Toast.LENGTH_SHORT).show()
                moveToMain()
            }.onFailure {
                Toast.makeText(this, "저장하는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })

        nicknameViewModel.loading.observe(this, Observer { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    private fun moveToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private val nicknameListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            binding.etNicknameLayout.error = null
            binding.etNicknameLayout.hintTextColor = null
            isNicknameDuplicated = true
        }

        override fun afterTextChanged(s: Editable?) {
            flagCheck()
        }

    }

    // Nickname 중복 확인
    private fun checkNicknameDuplication() {
        val nickname = binding.etNickname.text.toString()
        if (nickname.isBlank()) {
            showErrorFeedback(binding.etNicknameLayout, "닉네임을 입력해주세요.")
            return
        }
        nicknameViewModel.checkNicknameDuplication(nickname)
    }

    // Firestore에 email, nickname 저장
    private fun createNickname() {
        val nickname = binding.etNickname.text.toString()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "사용자 정보를 가져오지 못했습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val loginProvider = currentUser.providerData.firstOrNull { it.providerId != "firebase" }?.providerId
        if (loginProvider == null) {
            Toast.makeText(this, "로그인 제공자를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        nicknameViewModel.fetchUserEmail(loginProvider).observe(this) { result ->
            result.onSuccess { email ->
                nicknameViewModel.updateNickname(userId, email!!, nickname)
            }.onFailure {
                Toast.makeText(this, "이메일을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // TextInputLayout 조건 맞을 때
    private fun showPositiveFeedback(inputLayout: TextInputLayout, message: String) {
        inputLayout.error = message
        inputLayout.errorIconDrawable = null
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

    // Nickname 중복체크
    private fun flagCheck() {
        binding.btnNext.isEnabled = !isNicknameDuplicated
    }
}