package com.example.linkshare.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkshare.databinding.FragmentMemoBinding
import com.example.linkshare.memo.Memo
import com.example.linkshare.memo.MemoActivity
import com.example.linkshare.memo.MemoListAdapter
import com.example.linkshare.memo.NewMemoActivity
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.FBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MemoFragment : Fragment() {

    private var _binding: FragmentMemoBinding? = null
    private val binding get() = _binding!!
    private val memoList = mutableListOf<Memo>()
    private val memoKeyList = mutableListOf<String>()
    private lateinit var memoAdapter: MemoListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemoBinding.inflate(inflater, container, false)

        memoAdapter = MemoListAdapter(memoList)
        binding.rvMemo.adapter = memoAdapter
        binding.rvMemo.layoutManager = LinearLayoutManager(context)

        memoAdapter.setItemClickListener(object : MemoListAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val intent = Intent(context, MemoActivity::class.java)
                intent.putExtra("key", memoKeyList[position])   // key 값 전달
                startActivity(intent)
            }

        })

        binding.fabNewMemo.setOnClickListener {
            val intent = Intent(context, NewMemoActivity::class.java)
            startActivity(intent)
        }

        getFBData()

        return binding.root
    }

    // Firebase database로부터 memoCategory 가져오기
    private fun getFBMemoData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                for (dataModel in snapshot.children) {
                    // Memo 형식의 데이터 받기
                    val item = dataModel.getValue(Memo::class.java)

                    val myUid = FBAuth.getUid()
                    item?.let {
                        if (it.uid == myUid) {
                            memoList.add(item)
                            memoKeyList.add(dataModel.key.toString())
                        }
                    }
                }
                updateUI()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MemoFragment", "실패")
            }
        }
        FBRef.memoCategory.addValueEventListener(postListener)
    }

    // Firebase database로부터 boardCategory 가져오기
    private fun getFBBoardData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                for (dataModel in snapshot.children) {
                    // Memo 형식의 데이터 받기
                    val item = dataModel.getValue(Memo::class.java)

                    val myUid = FBAuth.getUid()
                    item?.let {
                        if (it.shareUid == myUid) {
                            memoList.add(item)
                            memoKeyList.add(dataModel.key.toString())
                        }
                    }
                }
                updateUI()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MemoFragment", "실패")
            }
        }
        FBRef.boardCategory.addValueEventListener(postListener)
    }

    // Firebase database로부터 data 가져오기
    private fun getFBData() {
        // 중복 방지
        memoList.clear()
        // 데이터 가져오기
        getFBMemoData()
        getFBBoardData()
    }

    // UI update
    private fun updateUI() {
        // 최신 글을 위로
        memoList.sortByDescending { it.time }
        memoKeyList.sortByDescending { it }
        // Sync
        memoAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}