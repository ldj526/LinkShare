package com.example.linkshare.setting

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityUpdateNicknameBinding
import com.example.linkshare.util.CustomDialog
import com.example.linkshare.view.MainActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class UpdateNicknameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateNicknameBinding
    private lateinit var nicknameViewModel: NicknameViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isNicknameDuplicated = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateNicknameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        val settingRepository = SettingRepository(db, auth)
        val factory = SettingViewModelFactory(settingRepository)
        nicknameViewModel = ViewModelProvider(this, factory)[NicknameViewModel::class.java]

        observeViewModel()

        fetchUserNickname()
        fetchUserLastUpdated()

        binding.etNicknameLayout.editText?.addTextChangedListener(nicknameListener)

        binding.btnCheckNickname.setOnClickListener {
            checkNicknameDuplication()
        }

        binding.btnUpdate.setOnClickListener {
            checkNicknameChangeAllowed()
        }
    }

    // ViewModel
    private fun observeViewModel() {
        nicknameViewModel.userNicknameResult.observe(this, Observer { result ->
            result.onSuccess { nickname ->
                binding.etNickname.setText(nickname ?: "알 수 없음")
            }.onFailure {
                Log.e("UpdateNicknameActivity", "닉네임 가져오기 실패", it)
                binding.etNickname.setText("알 수 없음")
            }
        })

        nicknameViewModel.lastUpdatedResult.observe(this, Observer { result ->
            result.onSuccess { lastUpdated ->
                binding.etNickname.tag = lastUpdated
            }.onFailure {
                Log.e("UpdateNicknameActivity", "마지막 업데이트 날짜 가져오기 실패", it)
            }
        })

        nicknameViewModel.nicknameDuplicationResult.observe(this, Observer { result ->
            result.onSuccess { isDuplicated ->
                isNicknameDuplicated = isDuplicated
                if (isDuplicated) {
                    showErrorFeedback(binding.etNicknameLayout, "중복된 닉네임입니다.")
                } else {
                    showPositiveFeedback(binding.etNicknameLayout, "사용 가능한 닉네임입니다.")
                    binding.btnUpdate.isEnabled = true
                }
            }.onFailure {
                showErrorFeedback(binding.etNicknameLayout, "닉네임 중복을 확인하는데 실패했습니다.")
                isNicknameDuplicated = true
            }
        })

        nicknameViewModel.updateNicknameResult.observe(this, Observer { result ->
            result.onSuccess {
                Toast.makeText(this, "성공", Toast.LENGTH_SHORT).show()
                navigateToSettingFragment()
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

    // 현재 접속중인 사용자의 Nickname 받아오기
    private fun fetchUserNickname() {
        val userId = auth.currentUser?.uid ?: return
        nicknameViewModel.fetchUserNickname(userId)
    }

    // 현재 접속중인 사용자의 마지막 업데이트 날짜 받아오기
    private fun fetchUserLastUpdated() {
        val userId = auth.currentUser?.uid ?: return
        nicknameViewModel.fetchUserLastUpdated(userId)
    }

    // Nickname 변경 가능 여부 확인
    private fun checkNicknameChangeAllowed() {
        val lastUpdated = binding.etNickname.tag as? Long ?: 0L
        if (nicknameViewModel.checkNicknameChangeAllowed(lastUpdated)) {
            showUpdateNicknameDialog()
        } else {
            val remainingDays = nicknameViewModel.getRemainingDays(lastUpdated)
            Toast.makeText(this, "닉네임 변경은 ${remainingDays}일 후에 가능합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // Nickname 중복 확인
    private fun checkNicknameDuplication() {
        val nickname = binding.etNickname.text.toString()
        nicknameViewModel.checkNicknameDuplication(nickname)
    }

    // Nickname 변경
    private fun updateNickname() {
        val nickname = binding.etNickname.text.toString()
        val email = auth.currentUser?.email ?: return
        val userId = auth.currentUser?.uid ?: return
        nicknameViewModel.updateNickname(userId, email, nickname)
    }

    // MainActivity로 돌아가서 SettingFragment로 navigate
    private fun navigateToSettingFragment() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.putExtra("navigateToSettingFragment", true)
        startActivity(intent)
        finish()
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
        binding.btnUpdate.isEnabled = !isNicknameDuplicated
    }

    // Nickname 변경 Dialog
    private fun showUpdateNicknameDialog() {
        val dialog = CustomDialog("변경 하시겠습니까?", onYesClicked = {
            updateNickname()
        })
        // 다이얼로그 창 밖에 클릭 불가
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "UpdateNicknameDialog")
    }
}