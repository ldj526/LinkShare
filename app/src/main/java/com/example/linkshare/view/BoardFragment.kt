package com.example.linkshare.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkshare.board.BoardActivity
import com.example.linkshare.board.BoardRVAdapter
import com.example.linkshare.databinding.FragmentBoardBinding
import com.example.linkshare.memo.Memo
import com.example.linkshare.memo.MemoViewModel
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.FBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class BoardFragment : Fragment() {

    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!
    private val memoList = mutableListOf<Memo>()
    private val memoKeyList = mutableListOf<String>()
    private lateinit var boardRVAdapter: BoardRVAdapter
    private val boardViewModel by lazy { ViewModelProvider(this)[MemoViewModel::class.java] }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoardBinding.inflate(inflater, container, false)

        boardRVAdapter = BoardRVAdapter(memoList)
        binding.rvBoard.adapter = boardRVAdapter
        binding.rvBoard.layoutManager = LinearLayoutManager(context)

        val uid = FBAuth.getUid()
        boardViewModel.getAllUserWrittenData()

        boardViewModel.allUserWrittenData.observe(viewLifecycleOwner) {
            boardRVAdapter.setBoardData(it)
        }

        boardViewModel.userWrittenData.observe(viewLifecycleOwner) {
            boardRVAdapter.setBoardData(it)
        }

        // 전체보기 클릭 시
        binding.btnAll.setOnClickListener {
            boardViewModel.getAllUserWrittenData()
        }

        // 내 글 보기 클릭 시
        binding.btnMy.setOnClickListener {
            boardViewModel.getUserWrittenData(uid)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}