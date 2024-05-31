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
import com.example.linkshare.databinding.ActivityNicknameBinding
import com.example.linkshare.view.MainActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class NicknameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNicknameBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isNicknameDuplicated = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNicknameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        binding.etNicknameLayout.editText?.addTextChangedListener(nicknameListener)

        binding.btnCheckNickname.setOnClickListener {
            checkNicknameDuplication()
        }

        binding.btnNext.setOnClickListener {
            createNickname()
        }
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

    // Nickname 만들고 MainActivity로 이동
    private fun createNickname() {
        val nickname = binding.etNickname.text.toString()
        val email = auth.currentUser!!.uid
        val user = hashMapOf("email" to email, "nickname" to nickname)
        // Firestore에 사용자 정보 저장
        db.collection("users").document(auth.currentUser!!.uid).set(user).addOnSuccessListener {
            Toast.makeText(this, "성공", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }.addOnFailureListener {
            Toast.makeText(this, "저장하는데 실패했습니다.", Toast.LENGTH_SHORT).show()
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