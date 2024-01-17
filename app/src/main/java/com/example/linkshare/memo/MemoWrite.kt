package com.example.linkshare.memo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.databinding.ActivityMemoWriteBinding
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.FBRef

class MemoWrite : AppCompatActivity() {

    private lateinit var binding: ActivityMemoWriteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val content = binding.etContent.text.toString()
            val link = binding.etLink.text.toString()
            val uid = FBAuth.getUid()
            val time = FBAuth.getTime()

            // Firebase database에 추가
            FBRef.memoCategory.push().setValue(Memo(title, content, link, uid, time))
            finish()
        }
    }
}