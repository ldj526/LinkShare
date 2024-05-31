package com.example.linkshare.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.linkshare.setting.UpdateNicknameActivity
import com.example.linkshare.auth.IntroActivity
import com.example.linkshare.databinding.FragmentSettingBinding
import com.example.linkshare.util.CustomDialog
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.FBUser
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.kakao.sdk.user.UserApiClient

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FirebaseAuth 인스턴스 초기화
        auth = Firebase.auth

        // 사용자 닉네임 가져오기
        fetchUserNickname()

        // 현재 사용자 정보 확인
        updateUserAccountUI()

        binding.tvLogout.setOnClickListener {
            showLogoutDialog()
        }

        binding.llNickname.setOnClickListener {
            val intent = Intent(requireContext(), UpdateNicknameActivity::class.java)
            startActivity(intent)
        }

        binding.llPwd.setOnClickListener {

        }

        binding.tvWithdraw.setOnClickListener {

        }
    }

    // 현재 접속중인 사용자의 닉네임 받아오기
    private fun fetchUserNickname() {
        FBUser.getUserNickname(FBAuth.getUid(), onSuccess = { userNickname ->
            binding.tvNickname.text = userNickname ?: "알 수 없음"
        }, onFailure = { exception ->
            Log.e("CommentRVADatper", "닉네임 가져오기 실패", exception)
            binding.tvNickname.text = "알 수 없음"
        })
    }

    // 사용자가 접속한 계정의 정보 받아오기
    private fun updateUserAccountUI() {
        val user = auth.currentUser
        if (user != null) {
            displayAuthenticationMethod(user)
            when {
                isEmailAccount(user) -> updateProfileUI(user.email)
                isGoogleAccount(user) -> updateProfileUI(user.email)
                isKakaoAccount(user) -> fetchKakaoUserEmail()
                else -> updateProfileUI(null)
            }
        } else {
            updateProfileUI(null)
        }
    }

    // 이메일 계정인지 확인
    private fun isEmailAccount(user: FirebaseUser): Boolean {
        return user.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }
    }

    // 구글 계정인지 확인
    private fun isGoogleAccount(user: FirebaseUser): Boolean {
        return user.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }
    }

    // 카카오 계정인지 확인
    private fun isKakaoAccount(user: FirebaseUser): Boolean {
        return user.providerData.any { it.providerId == "kakao.com" }
    }

    // 카카오 계정 이메일 가져오기
    private fun fetchKakaoUserEmail() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Toast.makeText(requireContext(), "카카오 사용자 정보 요청 실패", Toast.LENGTH_SHORT).show()
            } else {
                val email = user?.kakaoAccount?.email
                updateProfileUI(email)
            }
        }
    }

    // 로그인한 플랫폼 표시
    private fun displayAuthenticationMethod(user: FirebaseUser) {
        user.providerData.forEach { profile ->
            val method = when (profile.providerId) {
                EmailAuthProvider.PROVIDER_ID -> "이메일 계정"
                GoogleAuthProvider.PROVIDER_ID -> "구글 계정"
                else -> "카카오 계정"
            }
            binding.tvLoginMethod.text = method
        }
    }

    // 이메일 표시
    private fun updateProfileUI(email: String?) {
        binding.tvProfileEmail.text = email ?: "이메일 정보 없음"
    }

    private fun showLogoutDialog() {
        val dialog = CustomDialog("로그아웃 하시겠습니까?", onYesClicked = {
            auth.signOut()
            val intent = Intent(context, IntroActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        })
        // 다이얼로그 창 밖에 클릭 불가
        dialog.isCancelable = false
        dialog.show(requireActivity().supportFragmentManager, "LogoutDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}