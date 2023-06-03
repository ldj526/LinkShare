package com.example.linkshare.memo

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
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

        binding.imageView.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)
        }

        binding.writeBtn.setOnClickListener {
            val title = binding.title.text.toString()
            val content = binding.content.text.toString()
            val uid = FBAuth.getUid()
            val time = FBAuth.getTime()

            FBRef.memoList.push().setValue(MemoModel(title, content, uid, time))

            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 100) {
            binding.imageView.setImageURI(data?.data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}