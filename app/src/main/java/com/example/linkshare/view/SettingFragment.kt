package com.example.linkshare.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.linkshare.R
import com.example.linkshare.auth.IntroActivity
import com.example.linkshare.databinding.FragmentSettingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSettingBinding.inflate(inflater, container, false)

        auth = Firebase.auth

        binding.logout.setOnClickListener {
            showDialog()
        }

        return binding.root
    }

    private fun showDialog() {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.custom_dialog, null)
        val mBuilder =
            AlertDialog.Builder(requireContext()).setView(mDialogView).setTitle("로그아웃 하시겠습니까?")
        alertDialog = mBuilder.show()

        alertDialog!!.findViewById<Button>(R.id.removeBtn)?.setOnClickListener {
            auth.signOut()
            val intent = Intent(context, IntroActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        alertDialog!!.findViewById<Button>(R.id.cancelBtn)?.setOnClickListener {
            alertDialog!!.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Dialog가 fragment 종료 후에도 남아있는 것을 방지
        if (alertDialog != null && alertDialog!!.isShowing) {
            alertDialog!!.dismiss()
        }
        _binding = null
    }
}