package com.example.linkshare.memo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.linkshare.R
import com.example.linkshare.databinding.ActivityMemoUpdateBinding
import com.example.linkshare.utils.FBRef
import com.example.linkshare.view.MemoListFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

class MemoUpdateActivity : AppCompatActivity() {

    private var _binding: ActivityMemoUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var key: String
    private lateinit var writeUid: String
    private val TAG = MemoUpdateActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMemoUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        key = intent.getStringExtra("key").toString()

        getBoardData(key)
    }

    private fun getBoardData(key: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dataModel = dataSnapshot.getValue(MemoModel::class.java)

                binding.title.setText(dataModel!!.title)
                binding.content.setText(dataModel.content)
                writeUid = dataModel.uid
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