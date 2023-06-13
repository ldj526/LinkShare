package com.example.linkshare.board

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityBoardBinding
import com.example.linkshare.utils.FBRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class BoardActivity : AppCompatActivity() {

    private var _binding: ActivityBoardBinding? = null
    private val binding get() = _binding!!
    private lateinit var key: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        key = intent.getStringExtra("key").toString()

        binding.ivDelete.setOnClickListener {
            deleteDialog()
        }

        getBoardData(key)
        getImageData(key)
    }

    private fun deleteDialog() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView).setTitle("삭제 하시겠습니까?")
        val alertDialog = mBuilder.show()

        alertDialog.findViewById<Button>(R.id.yesBtn)?.setOnClickListener {
            FBRef.boardList.child(key).removeValue()
            alertDialog.dismiss()
            finish()
        }

        alertDialog.findViewById<Button>(R.id.noBtn)?.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun getImageData(key: String) {
        // Reference to an image file in Cloud Storage
        val storageReference = Firebase.storage.reference.child("${key}.png")

        // ImageView in your Activity
        val imageViewFromFB = binding.ivImage

        // Upload 됐을 경우 이미지를 보여주고 그렇지 않으면 안보이게 한다.
        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {
                Glide.with(this)
                    .load(task.result)
                    .into(imageViewFromFB)
            } else {
                binding.ivImage.isVisible = false
            }
        })
    }

    private fun getBoardData(key: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 데이터를 하나만 가져오면 되므로 반복문 사용이 필요 없다.
                val dataModel = dataSnapshot.getValue(BoardModel::class.java)

                binding.title.text = dataModel!!.title
                binding.time.text = dataModel!!.time
                binding.content.text = dataModel!!.content
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        FBRef.boardList.child(key).addValueEventListener(postListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}