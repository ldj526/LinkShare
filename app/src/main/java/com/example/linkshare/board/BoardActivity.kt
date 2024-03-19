package com.example.linkshare.board

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.linkshare.comment.Comment
import com.example.linkshare.comment.CommentRVAdapter
import com.example.linkshare.comment.CommentViewModel
import com.example.linkshare.databinding.ActivityBoardBinding
import com.example.linkshare.link.Link
import com.example.linkshare.link.UpdateLinkActivity
import com.example.linkshare.util.CustomDialog
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.FBRef
import com.example.linkshare.util.ShareResult
import java.io.ByteArrayOutputStream

class BoardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBoardBinding
    private lateinit var key: String
    private lateinit var writeUid: String
    private val commentList = mutableListOf<Comment>()
    private lateinit var commentRVAdapter: CommentRVAdapter
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0
    private var firebaseRef = ""
    private var shareCnt = 0
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // 결과를 받아 TextView에 설정
                binding.tvMap.text = result.data?.getStringExtra("title")
            }
        }
    private val boardViewModel by lazy { ViewModelProvider(this)[BoardViewModel::class.java] }
    private val commentViewModel by lazy { ViewModelProvider(this)[CommentViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        key = intent.getStringExtra("key").toString()

        // comment RecyclerView 연결
        commentRVAdapter = CommentRVAdapter(commentList)
        binding.rvComment.adapter = commentRVAdapter
        binding.rvComment.layoutManager = LinearLayoutManager(this)

        boardViewModel.getPostData(key)
        boardViewModel.linkData.observe(this) { link ->
            updateBoardData(link)
        }
        boardViewModel.getImageUrl(key)
        boardViewModel.imageUrl.observe(this) { url ->
            url?.let {
                Glide.with(this).load(it).into(binding.ivImage)
            }
        }
        boardViewModel.deleteStatus.observe(this) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "삭제 성공", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "삭제 실패", Toast.LENGTH_SHORT).show()
            }
        }
        boardViewModel.shareStatus.observe(this) { result ->
            when (result) {
                ShareResult.SUCCESS -> {
                    Toast.makeText(this, "공유 성공", Toast.LENGTH_SHORT).show()
                }
                ShareResult.ALREADY_SHARED -> {
                    Toast.makeText(this, "이미 공유된 글입니다.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "공유 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
        boardViewModel.shareCount.observe(this) { newShareCount ->
            binding.tvShareCnt.text = "공유 횟수 : $newShareCount"
        }

        commentViewModel.getCommentData(key).observe(this) { comments ->
            commentRVAdapter.setCommentData(comments)
        }

        commentViewModel.commentStatus.observe(this) { success ->
            if (success){
                Toast.makeText(this, "댓글 작성 성공", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "댓글 작성 실패", Toast.LENGTH_SHORT).show()
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

        // 댓글 입력 버튼 클릭 시
        binding.btnComment.setOnClickListener {
            val comment = Comment(binding.etComment.text.toString(), FBAuth.getUid(), FBAuth.getTime())
            commentViewModel.insertComment(comment, key)
            binding.etComment.setText("")
        }

        binding.tvMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java).apply {
                putExtra("latitude", latitude)
                putExtra("longitude", longitude)
                putExtra("title", binding.tvMap.text)
            }
            startForResult.launch(intent)
        }

        binding.ivShare.setOnClickListener {
            showShareDialog()
        }
    }

    // 삭제 다이얼로그 생성
    private fun showDeleteDialog() {
        val dialog = CustomDialog("삭제 하시겠습니까?", onYesClicked = {
            boardViewModel.deleteLink(FBRef.linkCategory, key)
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

    // 공유 다이얼로그 생성
    private fun showShareDialog() {
        val dialog = CustomDialog("개인 메모로 공유하시겠습니까?", onYesClicked = {
            val data = shareLink()
            boardViewModel.shareLink(key, data)
        })
        // 다이얼로그 창 밖에 클릭 불가
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "ShareDialog")
    }

    // 메모를 내 개인메모 목록으로 가져가기
    private fun shareLink(): ByteArray? {
        val imageView = binding.ivImage.drawable

        val data: ByteArray? = if (imageView is BitmapDrawable) {
            val bitmap = imageView.bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            baos.toByteArray()
        } else {
            null // 이미지가 없을 경우 null로 처리
        }
        return data
    }

    // 글의 데이터를 불러와 UI에 대입
    private fun updateBoardData(link: Link?) {
        link?.let {
            binding.tvTitle.text = it.title
            binding.tvLink.text = it.link
            binding.tvTime.text = it.time
            binding.tvContent.text = it.content
            binding.tvMap.apply {
                text = it.location
                visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
            }
            latitude = it.latitude
            longitude = it.longitude
            writeUid = it.uid
            firebaseRef = it.firebaseRef
            shareCnt = it.shareCount
            binding.tvShareCnt.text = "공유횟수 : ${it.shareCount}"
            adjustBoardViewVisibility(writeUid)
        }
    }

    // UI Visibility 조절
    private fun adjustBoardViewVisibility(linkWriteUid: String) {
        val myUid = FBAuth.getUid()
        val isMyLink = myUid == linkWriteUid

        // 글 쓴 사람이 자신일 경우 수정, 삭제 버튼 보이기
        binding.ivDelete.visibility = if (isMyLink) View.VISIBLE else View.GONE
        binding.ivUpdate.visibility = if (isMyLink) View.VISIBLE else View.GONE
        binding.ivShare.visibility = if (isMyLink) View.GONE else View.VISIBLE
    }
}