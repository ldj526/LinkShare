package com.example.linkshare.board

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityBoardUpdateBinding
import com.example.linkshare.utils.FBAuth
import com.example.linkshare.utils.FBRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import timber.log.Timber
import java.io.ByteArrayOutputStream

class BoardUpdateActivity : AppCompatActivity() {

    private lateinit var key: String
    private var _binding: ActivityBoardUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var writerUid: String
    private var isImageUpload = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityBoardUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        key = intent.getStringExtra("key").toString()

        getBoardData(key)
        getImageData(key)

        val spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.type,
            android.R.layout.simple_spinner_item
        )

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spCategory.adapter = spinnerAdapter

        binding.updateBtn.setOnClickListener {
            updateBoardData(key)

            if (isImageUpload) {
                imageUpload(key)
            }
            finish()
        }

        binding.ivImage.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)
            isImageUpload = true
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

        val imageView = binding.ivImage
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
            binding.ivImage.setImageURI(data?.data)
        }
    }

    // firebase에서 이미지 받아오기
    private fun getImageData(key: String) {
        // Reference to an image file in Cloud Storage
        val storageReference = Firebase.storage.reference.child("${key}.png")

        // ImageView in your Activity
        val imageViewFromFB = binding.ivImage

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {
                Glide.with(this)
                    .load(task.result)
                    .into(imageViewFromFB)
            } else {

            }
        })
    }

    private fun updateBoardData(key: String) {
        // 수정한 게시판 데이터를 Firebase로 업로드
        FBRef.boardList.child(key).setValue(
            BoardModel(
                binding.title.text.toString(),
                binding.content.text.toString(),
                binding.spCategory.selectedItem.toString(),
                writerUid,
                FBAuth.getTime()
            )
        )
        finish()
    }

    // firebase에서 데이터값 받아오기
    private fun getBoardData(key: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 메모가 삭제 됐을 때 정보가 없으면 에러가 나기 때문에 예외처리를 해준다.
                try {
                    val dataModel = dataSnapshot.getValue(BoardModel::class.java)

                    binding.title.setText(dataModel!!.title)
                    binding.content.setText(dataModel!!.content)
                    writerUid = dataModel!!.uid
                } catch (e: Exception) {

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.w("loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.boardList.child(key).addValueEventListener(postListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}