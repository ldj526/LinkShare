package com.example.linkshare.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkshare.databinding.FragmentMemolistBinding
import com.example.linkshare.memo.MemoModel
import com.example.linkshare.memo.MemoRVAdapter
import com.example.linkshare.memo.MemoWriteActivity
import com.example.linkshare.utils.FBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

class MemoListFragment : Fragment() {

    private var _binding: FragmentMemolistBinding? = null
    private val binding get() = _binding!!
    private val memoDataList = mutableListOf<MemoModel>()
    private lateinit var memoRVAdapter: MemoRVAdapter
    private val TAG = MemoListFragment::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMemolistBinding.inflate(inflater, container, false)

        // ListView 연결
        memoRVAdapter = MemoRVAdapter(memoDataList)
        binding.memoRV.adapter = memoRVAdapter
        binding.memoRV.layoutManager = LinearLayoutManager(context)

        memoRVAdapter.setItemClickListener(object : MemoRVAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                Toast.makeText(context, "dd", Toast.LENGTH_LONG).show()
            }
        })

        binding.fbAdd.setOnClickListener {
            val intent = Intent(context, MemoWriteActivity::class.java)
            startActivity(intent)
        }

        getFBBoardData()

        return binding.root
    }

    private fun getFBBoardData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 중복 방지
                memoDataList.clear()
                // Get Post object and use the values to update the UI
                for (dataModel in dataSnapshot.children) {
                    // BoardModel 형식의 데이터 받기
                    val item = dataModel.getValue(MemoModel::class.java)
                    memoDataList.add(item!!)
                }
                // 최신 글이 가장 위로
                memoDataList.reverse()
                // Syncㅣ
                memoRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Timber.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.memoList.addValueEventListener(postListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}