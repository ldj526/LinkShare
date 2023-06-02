package com.example.linkshare.memo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.linkshare.databinding.ActivityBoardWriteBinding
import com.example.linkshare.utils.FBAuth
import com.example.linkshare.utils.FBRef

class MemoWriteActivity : AppCompatActivity() {

    private var _binding: ActivityBoardWriteBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityBoardWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.writeBtn.setOnClickListener {
            val title = binding.title.text.toString()
            val content = binding.content.text.toString()
            val uid = FBAuth.getUid()
            val time = FBAuth.getTime()

            FBRef.category.push().setValue(MemoModel(title, content, uid, time))

            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}