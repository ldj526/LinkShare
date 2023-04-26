package com.example.linkshare.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {

    private var _binding: ActivityIntroBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityIntroBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.joinText.setOnClickListener {
            // JoinActivity 로 이동
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}