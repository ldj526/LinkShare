package com.example.linkshare.memo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.databinding.ActivityMemoWriteBinding
import com.example.linkshare.utils.FBAuth
import com.example.linkshare.utils.FBRef

class MemoWriteActivity : AppCompatActivity() {

    private var _binding: ActivityMemoWriteBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMemoWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.writeBtn.setOnClickListener {
            val title = binding.title.text.toString()
            val content = binding.content.text.toString()
            val uid = FBAuth.getUid()
            val time = FBAuth.getTime()

            FBRef.memoList.push().setValue(MemoModel(title, content, uid, time))

            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}