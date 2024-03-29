package com.example.linkshare.link

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.linkshare.R
import com.example.linkshare.board.MapActivity
import com.example.linkshare.databinding.ActivityLinkBinding
import com.example.linkshare.util.CustomDialog
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.FBRef
import com.google.android.flexbox.FlexboxLayout

class LinkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLinkBinding
    private lateinit var key: String
    private lateinit var writeUid: String
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0
    private var firebaseRef = ""
    private val linkDataResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 결과를 받아 TextView에 설정
            binding.tvMap.text = result.data?.getStringExtra("title")
        }
    }
    private val linkViewModel: LinkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        key = intent.getStringExtra("key").toString()
        firebaseRef = intent.getStringExtra("firebaseRef").toString()

        linkViewModel.getPostData(key)
        linkViewModel.linkData.observe(this) { link ->
            updateLinkData(link)
        }
        linkViewModel.getImageUrl(key)
        linkViewModel.imageUrl.observe(this) { url ->
            url?.let {
                Glide.with(this).load(it).into(binding.ivImage)
            }
        }
        linkViewModel.deleteStatus.observe(this) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "삭제 성공", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "삭제 실패", Toast.LENGTH_SHORT).show()
            }
        }

        // 수정 버튼 클릭 시
        binding.ivUpdate.setOnClickListener {
            showUpdateDialog()
        }

        // 삭제 버튼 클릭 시
        binding.ivDelete.setOnClickListener {
            showDeleteDialog()
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
            linkDataResultLauncher.launch(intent)
        }
    }

    // 글의 데이터를 불러와 UI에 대입
    private fun updateLinkData(link: Link?) {
        link?.let {
            it.category?.forEach { category ->
                val textView = TextView(this).apply {
                    text = category
                    layoutParams = FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(5, 5, 5, 5)
                    }
                    // 현재 선택된 카테고리인지 확인하여 UI 업데이트
                    setBackgroundResource(R.drawable.category_unselected_background)
                }
                binding.categoryFlexboxLayout.addView(textView)
            }
            binding.tvTitle.text = it.title
            binding.tvLink.text = it.link
            binding.tvContent.text = it.content
            binding.tvMap.apply {
                text = it.location
                visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
            }
            latitude = it.latitude
            longitude = it.longitude
            binding.tvTime.text = it.time
            writeUid = it.uid
            firebaseRef = it.firebaseRef
            adjustMemoViewVisibility(writeUid, it.shareUid)
        }
    }

    // UI Visibility 조절
    private fun adjustMemoViewVisibility(linkWriteUid: String, shareUid: String?) {
        val myUid = FBAuth.getUid()
        val isMyMemo = myUid == linkWriteUid
        val isSharedMemo = myUid == (shareUid ?: "")

        // 글 쓴 사람이 자신일 경우 수정, 삭제 버튼 보이기
        binding.ivDelete.visibility = if (isMyMemo || isSharedMemo) View.VISIBLE else View.GONE
        binding.ivUpdate.visibility = if (isMyMemo) View.VISIBLE else View.GONE
    }

    // 삭제 다이얼로그 생성
    private fun showDeleteDialog() {
        val dialog = CustomDialog("삭제 하시겠습니까?", onYesClicked = {
            if (firebaseRef == "link") linkViewModel.deleteMemo(FBRef.linkCategory, key)
            else if (firebaseRef == "sharedLink") linkViewModel.deleteMemo(FBRef.sharedLinkCategory, key)
        })
        // 다이얼로그 창 밖에 클릭 불가
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "DeleteDialog")
    }

    // 수정 다이얼로그 생성
    private fun showUpdateDialog() {
        val dialog = CustomDialog("수정 하시겠습니까?", onYesClicked = {
            val intent = Intent(this, UpdateLinkActivity::class.java)
            intent.putExtra("key", key)   // key 값 전달
            startActivity(intent)
        })
        // 다이얼로그 창 밖에 클릭 불가
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "UpdateDialog")
    }
}