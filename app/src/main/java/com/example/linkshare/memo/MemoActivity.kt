package com.example.linkshare.memo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.linkshare.board.MapActivity
import com.example.linkshare.databinding.ActivityMemoBinding
import com.example.linkshare.util.CustomDialog
import com.example.linkshare.util.CustomDialogInterface
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.FBRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage

class MemoActivity : AppCompatActivity(), CustomDialogInterface {

    private lateinit var binding: ActivityMemoBinding
    private lateinit var key: String
    private lateinit var writeUid: String
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // 결과를 받아 TextView에 설정
                binding.tvMap.text = result.data?.getStringExtra("title")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        key = intent.getStringExtra("key").toString()
        getMemoData(key)
        getImageData(key)

        // 수정 버튼 클릭 시
        binding.ivUpdate.setOnClickListener {
            val intent = Intent(this, UpdateMemoActivity::class.java)
            intent.putExtra("key", key)   // key 값 전달
            startActivity(intent)
        }

        // 삭제 버튼 클릭 시
        binding.ivDelete.setOnClickListener {
            showDialog()
        }

        // 뒤로가기 버튼 클릭 시
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.tvMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java).apply {
                putExtra("latitude", latitude)
                putExtra("longitude", longitude)
                putExtra("title", binding.tvMap.text)
            }
            startForResult.launch(intent)
        }
    }

    // Firebase에서 데이터 값 가져오기
    private fun getMemoData(key: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {   // 메모가 삭제 됐을 때 정보가 없으면 에러가 나기 때문에 예외처리
                    val dataModel = snapshot.getValue(Memo::class.java)
                    Log.d("MemoActivity", "실행")
                    binding.tvTitle.text = dataModel!!.title
                    Log.d("MemoActivity", "${dataModel.title}")
                    binding.tvLink.text = dataModel.link
                    binding.tvTime.text = dataModel.time
                    binding.tvContent.text = dataModel.content
                    binding.tvMap.apply {
                        text = dataModel.location
                        visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
                    }
                    latitude = dataModel.latitude
                    longitude = dataModel.longitude
                    writeUid = dataModel.uid
                    val myUid = FBAuth.getUid()

                    // 글 쓴 사람이 자신일 경우 수정, 삭제 버튼 보이기
                    if (myUid == writeUid) {
                        binding.ivDelete.isVisible = true
                        binding.ivUpdate.isVisible = true
                    }
                } catch (e: Exception) {

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("BoardFragment", "실패")
            }

        }
        FBRef.memoCategory.child(key).addValueEventListener(postListener)
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
        FBRef.memoCategory.child(key).removeValue()
        finish()
    }
}