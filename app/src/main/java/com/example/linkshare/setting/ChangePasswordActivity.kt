package com.example.linkshare.setting

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
import com.example.linkshare.databinding.ActivityChangePasswordBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var changePasswordViewModel: ChangePasswordViewModel
    private var newPwdFlag = false
    private var newPwdCheckFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        val settingRepository = SettingRepository(db, auth)
        val factory = SettingViewModelFactory(settingRepository)
        changePasswordViewModel = ViewModelProvider(this, factory)[ChangePasswordViewModel::class.java]

        binding.etNewPwdLayout.editText?.addTextChangedListener(newPwdListener)
        binding.etConfirmNewPwdLayout.editText?.addTextChangedListener(newConfirmPwdListener)

        changePasswordViewModel.fetchCurrentUser()
        observeViewModel()

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnUpdatePwd.setOnClickListener {
            updatePassword()
        }
    }

    // Observe ViewModel
    private fun observeViewModel() {
        changePasswordViewModel.updatePasswordResult.observe(this, Observer { result ->
            result.fold(
                onSuccess = {
                    if (it) {
                        Toast.makeText(this, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "비밀번호 변경에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        showErrorFeedback(binding.etCurrentPwdLayout, "비밀번호가 다릅니다")
                    }
                },
                onFailure = {
                    Toast.makeText(this, "비밀번호 변경에 실패했습니다: ${it.message}",Toast.LENGTH_SHORT).show()
                    showErrorFeedback(binding.etCurrentPwdLayout, "비밀번호가 다릅니다")
                }
            )
        })

        changePasswordViewModel.loading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
    }

    // 비밀번호 변경
    private fun updatePassword() {
        val currentPassword = binding.etCurrentPwdLayout.editText?.text.toString()
        val newPassword = binding.etNewPwdLayout.editText?.text.toString()
        val confirmNewPassword = binding.etConfirmNewPwdLayout.editText?.text.toString()

        if (newPassword != confirmNewPassword) {
            Toast.makeText(this, "새 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val email = changePasswordViewModel.currentUser.value?.email ?: return
        changePasswordViewModel.updatePassword(email, currentPassword, newPassword)
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

    // new password listener
    private val newPwdListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            validateNewPassword(s)
        }
    }

    // confirm new password listener
    private val newConfirmPwdListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            validateNewPasswordCheck(s)
        }
    }

    private fun validateNewPassword(s: Editable?) {
        val pwdPattern = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&.])[A-Za-z[0-9]$@$!%*#?&.]+$"
        if (s != null) {
            when {
                s.isEmpty() -> {
                    showErrorFeedback(binding.etNewPwdLayout, "새 비밀번호를 입력해주세요.")
                    newPwdFlag = false
                }
                (s.length < 8 || s.length > 20) && s.matches(pwdPattern.toRegex()) -> {
                    showErrorFeedback(binding.etNewPwdLayout, "8자리 이상 20자리 이하로 입력해주세요")
                    newPwdFlag = false
                }
                (s.length < 8 || s.length > 20) && !s.matches(pwdPattern.toRegex()) -> {
                    showErrorFeedback(binding.etNewPwdLayout, "8자리 이상 20자리 이하로 입력해주세요\n영문, 숫자, 특수문자를 1개 이상 입력해주세요.")
                    newPwdFlag = false
                }
                (s.length in 8..20) && !s.matches(pwdPattern.toRegex()) -> {
                    showErrorFeedback(binding.etNewPwdLayout, "영문, 숫자, 특수문자를 1개 이상 입력해주세요.")
                    newPwdFlag = false
                }
                (s.length in 8..20) && s.matches(pwdPattern.toRegex()) -> {
                    showPositiveFeedback(binding.etNewPwdLayout, "올바른 비밀번호입니다.")
                    newPwdFlag = true
                }
            }
            flagCheck()
        }
    }

    private fun validateNewPasswordCheck(s: Editable?) {
        if (s != null) {
            when {
                s.isEmpty() -> {
                    showErrorFeedback(binding.etConfirmNewPwdLayout, "비밀번호 확인을 입력해주세요.")
                    newPwdCheckFlag = false
                }
                s.toString() != binding.etNewPwdLayout.editText?.text.toString() -> {
                    showErrorFeedback(binding.etConfirmNewPwdLayout, "비밀번호가 일치하지 않습니다.")
                    newPwdCheckFlag = false
                }
                else -> {
                    showPositiveFeedback(binding.etConfirmNewPwdLayout, "비밀번호가 일치합니다.")
                    newPwdCheckFlag = true
                }
            }
            flagCheck()
        }
    }

    // password가 올바를 경우 변경 버튼 활성화
    private fun flagCheck() {
        binding.btnUpdatePwd.isEnabled = newPwdFlag && newPwdCheckFlag
        Log.d("ChangePasswordActivity", "$newPwdFlag, ${!newPwdCheckFlag}")
    }
}