package com.example.linkshare.board

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.linkshare.databinding.ActivityBoardBinding
import com.example.linkshare.util.CustomDialog
import com.example.linkshare.util.CustomDialogInterface
import com.example.linkshare.util.FBRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage

class BoardActivity : AppCompatActivity(), CustomDialogInterface {

    private lateinit var binding: ActivityBoardBinding
    private lateinit var key: String
    private lateinit var writeUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        key = intent.getStringExtra("key").toString()

        getBoardData(key)
        getImageData(key)

        // 수정 버튼 클릭 시
        binding.ivUpdate.setOnClickListener {
            val intent = Intent(this, UpdateBoardActivity::class.java)
            intent.putExtra("key", key)   // key 값 전달
            startActivity(intent)
        }

        // 삭제 버튼 클릭 시
        binding.ivDelete.setOnClickListener {
            showDialog()
        }
    }

    // Firebase에서 데이터 값 가져오기
    private fun getBoardData(key: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {   // 메모가 삭제 됐을 때 정보가 없으면 에러가 나기 때문에 예외처리
                    val dataModel = snapshot.getValue(Board::class.java)

                    binding.tvTitle.text = dataModel!!.title
                    binding.tvLink.text = dataModel.link
                    binding.tvTime.text = dataModel.time
                    binding.tvContent.text = dataModel.content
                    writeUid = dataModel.uid
                } catch (e: Exception) {

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("BoardFragment", "실패")
            }

        }
        FBRef.boardCategory.child(key).addValueEventListener(postListener)
    }

    // Firebase에서 Image 가져오기
    private fun getImageData(key: String) {
        // Reference to an image file in Cloud Storage
        val storageReference = Firebase.storage.reference.child("${key}.png")

        // ImageView in your Activity
        val imageViewFromFB = binding.ivImage

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {
                Glide.with(applicationContext)
                    .load(task.result)
                    .into(imageViewFromFB)
            } else {

            }
        })
    }

    // 다이얼로그 생성
    private fun showDialog() {
        val dialog = CustomDialog(this, "삭제 하시겠습니까?")
        // 다이얼로그 창 밖에 클릭 불가
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "DeleteDialog")
    }

    override fun onClickYesButton() {
        FBRef.boardCategory.child(key).removeValue()
        finish()
    }
}