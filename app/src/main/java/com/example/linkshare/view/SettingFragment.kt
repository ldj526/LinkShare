package com.example.linkshare.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.linkshare.auth.IntroActivity
import com.example.linkshare.databinding.FragmentSettingBinding
import com.example.linkshare.util.CustomDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(layoutInflater, container, false)

        auth = Firebase.auth

        binding.tvLogout.setOnClickListener {
            showLogoutDialog()
        }
        return binding.root
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