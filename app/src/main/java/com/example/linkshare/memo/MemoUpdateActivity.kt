package com.example.linkshare.memo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.Layout
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityMemoUpdateBinding
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

class MemoUpdateActivity : AppCompatActivity() {

    private var _binding: ActivityMemoUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var key: String
    private lateinit var writeUid: String
    private val TAG = MemoUpdateActivity::class.java.simpleName
    private var isImageUpload = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMemoUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        key = intent.getStringExtra("key").toString()

        getBoardData(key)
        getImageData(key)

        binding.updateBtn.setOnClickListener {
            updateMemoData(key)

            if (isImageUpload) {
                imageUpload(key)
            }
            finish()
        }

        binding.memoDelete.setOnClickListener {
            showDialog()
        }

        binding.imageView.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)
            isImageUpload = true
        }
    }

    private fun showDialog() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView).setTitle("게시글 삭제")
        val alertDialog = mBuilder.show()

        alertDialog.findViewById<Button>(R.id.removeBtn)?.setOnClickListener {
            Toast.makeText(this, "삭제", Toast.LENGTH_LONG).show()
        }

        alertDialog.findViewById<Button>(R.id.cancelBtn)?.setOnClickListener {
            Toast.makeText(this, "취소", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateMemoData(key: String) {
        // 수정한 메모 데이터를 Firebase로 업로드
        FBRef.memoList.child(key).setValue(
            MemoModel(
                binding.title.text.toString(),
                binding.content.text.toString(),
                writeUid,
                FBAuth.getTime()
            )
        )
        finish()
    }

    // firebase storage에 이미지 업로드 하기
    private fun imageUpload(key: String) {
        // Get the data from an ImageView as bytes

        val storage = Firebase.storage
        // Create a storage reference from our app
        val storageRef = storage.reference
        // Create a reference to "mountains.jpg"
        val mountainsRef = storageRef.child("${key}.png")

        val imageView = binding.imageView
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = mountainsRef.putBytes(data)
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

    // firebase에서 이미지 받아오기
    private fun getImageData(key: String) {
        // Reference to an image file in Cloud Storage
        val storageReference = Firebase.storage.reference.child("${key}.png")

        // ImageView in your Activity
        val imageViewFromFB = binding.imageView

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {
                Glide.with(this)
                    .load(task.result)
                    .into(imageViewFromFB)
            } else {

            }
        })
    }

    // firebase에서 데이터값 받아오기
    private fun getBoardData(key: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dataModel = dataSnapshot.getValue(MemoModel::class.java)

                binding.title.setText(dataModel!!.title)
                binding.content.setText(dataModel!!.content)
                writeUid = dataModel!!.uid
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.memoList.child(key).addValueEventListener(postListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}