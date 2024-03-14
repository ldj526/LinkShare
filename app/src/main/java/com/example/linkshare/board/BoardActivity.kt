package com.example.linkshare.board

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.linkshare.comment.Comment
import com.example.linkshare.comment.CommentRVAdapter
import com.example.linkshare.databinding.ActivityBoardBinding
import com.example.linkshare.memo.Memo
import com.example.linkshare.memo.MemoViewModel
import com.example.linkshare.memo.UpdateMemoActivity
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
import java.io.ByteArrayOutputStream

class BoardActivity : AppCompatActivity(), CustomDialogInterface {

    private lateinit var binding: ActivityBoardBinding
    private lateinit var key: String
    private lateinit var writeUid: String
    private val commentList = mutableListOf<Comment>()
    private val commentKeyList = mutableListOf<String>()
    private lateinit var commentRVAdapter: CommentRVAdapter
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
        binding = ActivityBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        key = intent.getStringExtra("key").toString()

        // comment RecyclerView 연결
        commentRVAdapter = CommentRVAdapter(commentList)
        binding.rvComment.adapter = commentRVAdapter
        binding.rvComment.layoutManager = LinearLayoutManager(this)

        memoViewModel.getMemoDataForUpdate(key)
        memoViewModel.memoData.observe(this) { memo ->
            memo?.let {
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
                val myUid = FBAuth.getUid()

                // 글 쓴 사람이 자신일 경우 수정, 삭제 버튼 보이기
                if (myUid == writeUid) {
                    binding.ivDelete.visibility = View.VISIBLE
                    binding.ivUpdate.visibility = View.VISIBLE
                    binding.ivShare.visibility = View.GONE
                } else {
                    binding.ivDelete.visibility = View.GONE
                    binding.ivUpdate.visibility = View.GONE
                    binding.ivShare.visibility = View.VISIBLE
                }
            }
        }
        memoViewModel.getImageUrlForUpdate(key)
        memoViewModel.imageUrl.observe(this) { url ->
            url?.let {
                Glide.with(this).load(it).into(binding.ivImage)
            }
        }

        getCommentData(key)

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

        // 댓글 입력 버튼 클릭 시
        binding.btnComment.setOnClickListener {
            insertComment(key)
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
            shareMemo()
        }
    }

    // Firebase에 댓글 내용 입력
    private fun insertComment(key: String) {
        FBRef.commentCategory.child(key).push().setValue(
            Comment(
                binding.etComment.text.toString(),
                FBAuth.getUid(), FBAuth.getTime()
            )
        )

        // 댓글 입력 후 빈 공간으로 해주기 위함
        binding.etComment.setText("")
    }

    // Firebase에서 댓글 데이터 가져오기
    private fun getCommentData(key: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                // 중복되는 데이터가 생기므로 기존에 있던 데이터들을 삭제해준다.
                commentList.clear()
                for (dataModel in dataSnapshot.children) {
                    // CommentModel 형식의 데이터 받기
                    val item = dataModel.getValue(Comment::class.java)
                    commentList.add(item!!)
                    commentKeyList.add(dataModel.key.toString())
                }
                commentRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        FBRef.commentCategory.child(key).addValueEventListener(postListener)
    }

    // 다이얼로그 생성
    private fun showDialog() {
        val dialog = CustomDialog(this, "삭제 하시겠습니까?")
        // 다이얼로그 창 밖에 클릭 불가
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "DeleteDialog")
    }

    // 작성된 글 내 개인메모로 가져오기
    private fun shareMemo() {
        val title = binding.tvTitle.text.toString()
        val content = binding.tvContent.text.toString()
        val link = binding.tvLink.text.toString()
        val location = binding.tvMap.text.toString()
        val currentUid = FBAuth.getUid()
        val time = FBAuth.getTime()
        FBRef.boardCategory.push().setValue(Memo(key, title, content, link, location, latitude, longitude, writeUid, time, currentUid))
        imageUpload(key)
    }

    // Firebase에 Image Upload
    private fun imageUpload(key: String) {
        // Get the data from an ImageView as bytes

        val storage = Firebase.storage
        // Create a storage reference from our app
        val storageRef = storage.reference
        // Create a reference to "mountains.jpg"
        val mountainsRef = storageRef.child("$key.png")

        val imageView = binding.ivImage
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

    override fun onClickYesButton() {
        FBRef.memoCategory.child(key).removeValue()
        finish()
    }
}