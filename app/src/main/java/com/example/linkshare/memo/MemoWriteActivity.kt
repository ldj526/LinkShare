package com.example.linkshare.memo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.databinding.ActivityMemoWriteBinding
import com.example.linkshare.utils.FBAuth
import com.example.linkshare.utils.FBRef
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class MemoWriteActivity : AppCompatActivity() {

    private var _binding: ActivityMemoWriteBinding? = null
    private val binding get() = _binding!!
    private var isImageUpload = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMemoWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageView.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)
            isImageUpload = true
        }

        binding.writeBtn.setOnClickListener {
            val title = binding.title.text.toString()
            val content = binding.content.text.toString()
            val uid = FBAuth.getUid()
            val time = FBAuth.getTime()

            val key = FBRef.memoList.push().key.toString()

            FBRef.memoList.child(key).setValue(MemoModel(title, content, uid, time))

            if(isImageUpload){
                imageUpload(key)
            }
            finish()
        }
    }

    // Firebase storage에 image upload
    private fun imageUpload(key: String) {
        // Get the data from an ImageView as bytes

        val storage = Firebase.storage
        // Create a storage reference from our app
        val storageRef = storage.reference
        // Create a reference to "mountains.jpg"
        val mountainsRef = storageRef.child("$key.png")

        val imageView = binding.imageView
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = mountainsRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
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