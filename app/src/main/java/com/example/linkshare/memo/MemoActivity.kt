package com.example.linkshare.memo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.linkshare.board.MapActivity
import com.example.linkshare.databinding.ActivityMemoBinding
import com.example.linkshare.util.CustomDialog
import com.example.linkshare.util.CustomDialogInterface
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.FBRef

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
    private val memoViewModel by lazy { ViewModelProvider(this)[MemoViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        key = intent.getStringExtra("key").toString()

        memoViewModel.getPostData(key)
        memoViewModel.memoData.observe(this) { memo ->
            updateMemoData(memo)
        }
        memoViewModel.getImageUrl(key)
        memoViewModel.imageUrl.observe(this) { url ->
            url?.let {
                Glide.with(this).load(it).into(binding.ivImage)
            }
        }

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

    // 글의 데이터를 불러와 UI에 대입
    private fun updateMemoData(memo: Memo?) {
        memo?.let {
            binding.tvTitle.text = it.title
            binding.tvLink.text = it.link
            binding.tvContent.text = it.content
            binding.tvMap.apply {
                text = it.location
                visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
            }
            latitude = it.latitude
            longitude = it.longitude
            writeUid = it.uid
            adjustMemoViewVisibility(writeUid)
        }
    }

    // UI Visibility 조절
    private fun adjustMemoViewVisibility(memoWriteUid: String) {
        val myUid = FBAuth.getUid()
        val isMyMemo = myUid == memoWriteUid

        // 글 쓴 사람이 자신일 경우 수정, 삭제 버튼 보이기
        binding.ivDelete.visibility = if (isMyMemo) View.VISIBLE else View.GONE
        binding.ivUpdate.visibility = if (isMyMemo) View.VISIBLE else View.GONE
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