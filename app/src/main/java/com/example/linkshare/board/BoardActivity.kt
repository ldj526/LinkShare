package com.example.linkshare.board

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.linkshare.R
import com.example.linkshare.board.comment.CommentModel
import com.example.linkshare.board.comment.CommentRVAdapter
import com.example.linkshare.board.comment.CommentViewModel
import com.example.linkshare.databinding.ActivityBoardBinding
import com.example.linkshare.utils.FBAuth
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
    private val commentDataList = mutableListOf<CommentModel>()
    private lateinit var commentRVAdapter: CommentRVAdapter
    private val viewModel by lazy { ViewModelProvider(this)[CommentViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        key = intent.getStringExtra("key").toString()

        binding.ivDelete.setOnClickListener {
            deleteDialog()
        }

        binding.ivUpdate.setOnClickListener {
            updateDialog()
        }

        binding.commentBtn.setOnClickListener {
            insertComment(key, binding.commentArea.text.toString())
        }

        // RecyclerView 연결
        commentRVAdapter = CommentRVAdapter(commentDataList)
        binding.commentRV.adapter = commentRVAdapter
        binding.commentRV.layoutManager = LinearLayoutManager(baseContext)

        getCommentData(key)

        getBoardData(key)
        getImageData(key)
    }

    // Firebase에 있는 댓글 가져오기
    private fun getCommentData(key: String) {
        viewModel.getCommentData(key).observe(this, Observer {
            commentRVAdapter.setData(it)
            commentRVAdapter.notifyDataSetChanged()
        })
    }

    // Firebase에 댓글 저장
    private fun insertComment(key: String, str: String) {
        viewModel.insertComment(key, str)
        binding.commentArea.setText("")
    }

    private fun updateDialog() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView).setTitle("수정 하시겠습니까?")
        val alertDialog = mBuilder.show()

        alertDialog.findViewById<Button>(R.id.yesBtn)?.setOnClickListener {
            val intent = Intent(this, BoardUpdateActivity::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
            alertDialog.dismiss()
            finish()
        }

        alertDialog.findViewById<Button>(R.id.noBtn)?.setOnClickListener {
            alertDialog.dismiss()
        }
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
                try {
                    // 데이터를 하나만 가져오면 되므로 반복문 사용이 필요 없다.
                    val dataModel = dataSnapshot.getValue(BoardModel::class.java)

                    // boardModel의 category position을 문자로 받기 위해 spinneradapter 연결
                    val spinnerAdapter = ArrayAdapter.createFromResource(
                        applicationContext,
                        R.array.type,
                        android.R.layout.simple_spinner_item
                    )
                    val category = spinnerAdapter.getItem(dataModel!!.category)

                    binding.title.text = dataModel!!.title
                    binding.time.text = dataModel!!.time
                    binding.category.text = category
                    binding.content.text = dataModel!!.content

                    val myUid = FBAuth.getUid()
                    val writeUid = dataModel.uid

                    // 글 쓴 사람이 자신일 경우 수정, 삭제 버튼 보이기
                    if (myUid == writeUid) {
                        binding.ivDelete.isVisible = true
                        binding.ivUpdate.isVisible = true
                    }
                } catch (e: Exception) {

                }
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