package com.example.linkshare.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.linkshare.auth.IntroActivity
import com.example.linkshare.databinding.FragmentSettingBinding
import com.example.linkshare.setting.ChangePasswordActivity
import com.example.linkshare.setting.SettingRepository
import com.example.linkshare.setting.SettingViewModel
import com.example.linkshare.setting.SettingViewModelFactory
import com.example.linkshare.setting.UpdateNicknameActivity
import com.example.linkshare.util.CustomDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.user.UserApiClient

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingViewModel: SettingViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var userApiClient: UserApiClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(layoutInflater, container, false)

        // Initialize Firebase Auth
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        userApiClient = UserApiClient.instance
        val settingRepository = SettingRepository(db, auth, requireContext())
        val factory = SettingViewModelFactory(settingRepository)
        settingViewModel = ViewModelProvider(this, factory)[SettingViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingViewModel.fetchUserDetails()
        observeViewModel()

        // 사용자 닉네임 가져오기
        fetchUserNickname()

        binding.tvLogout.setOnClickListener {
            showLogoutDialog()
        }

        binding.llNickname.setOnClickListener {
            val intent = Intent(requireContext(), UpdateNicknameActivity::class.java)
            startActivity(intent)
        }

        binding.llPwd.setOnClickListener {
            val intent = Intent(requireContext(), ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        binding.tvWithdraw.setOnClickListener {
            showWithdrawDialog()
        }
    }

    // ViewModel
    private fun observeViewModel() {
        settingViewModel.authMethod.observe(viewLifecycleOwner, Observer { provider ->
            binding.tvLoginMethod.text = provider
            binding.llPwd.visibility = if (provider == "이메일") View.VISIBLE else View.GONE
        })

        settingViewModel.profileEmail.observe(viewLifecycleOwner, Observer { result ->
            result.onSuccess { email ->
                binding.tvProfileEmail.text = email
            }.onFailure {
                Toast.makeText(requireContext(), "이메일을 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })

        settingViewModel.userNicknameResult.observe(viewLifecycleOwner, Observer { result ->
            result.onSuccess { nickname ->
                binding.tvNickname.text = nickname ?: "알 수 없음"
            }.onFailure {
                Log.e("UpdateNicknameActivity", "닉네임 가져오기 실패", it)
                binding.tvNickname.text = "알 수 없음"
            }
        })

        settingViewModel.deleteResult.observe(viewLifecycleOwner, Observer { result ->
            result.onSuccess {
                // 탈퇴 성공 시 IntroActivity로 이동
                val intent = Intent(requireContext(), IntroActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }.onFailure {
                Toast.makeText(requireContext(), "회원탈퇴에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        })

        settingViewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                binding.pbNickname.visibility = View.VISIBLE
                binding.tvNickname.visibility = View.GONE
            } else {
                binding.pbNickname.visibility = View.GONE
                binding.tvNickname.visibility = View.VISIBLE
            }
        })

        settingViewModel.emailLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                binding.pbProfileEmail.visibility = View.VISIBLE
                binding.tvProfileEmail.visibility = View.GONE
            } else {
                binding.pbProfileEmail.visibility = View.GONE
                binding.tvProfileEmail.visibility = View.VISIBLE
            }
        })

        settingViewModel.loginMethodLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                binding.pbLoginMethod.visibility = View.VISIBLE
                binding.tvLoginMethod.visibility = View.GONE
            } else {
                binding.pbLoginMethod.visibility = View.GONE
                binding.tvLoginMethod.visibility = View.VISIBLE
            }
        })
    }

    // 현재 접속중인 사용자의 닉네임 받아오기
    private fun fetchUserNickname() {
        val userId = auth.currentUser?.uid ?: return
        settingViewModel.fetchUserNickname(userId)
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

    private fun showWithdrawDialog() {
        val dialog = CustomDialog("회원탈퇴 하시겠습니까?\n탈퇴 시 모든 정보가 사라집니다.", onYesClicked = {
            settingViewModel.deleteUserAccount()
        })
        // 다이얼로그 창 밖에 클릭 불가
        dialog.isCancelable = false
        dialog.show(requireActivity().supportFragmentManager, "WithdrawDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}