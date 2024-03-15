package com.example.linkshare.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkshare.board.BoardRVAdapter
import com.example.linkshare.databinding.FragmentBoardBinding
import com.example.linkshare.memo.Memo
import com.example.linkshare.memo.MemoViewModel

class BoardFragment : Fragment() {

    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!
    private val memoList = mutableListOf<Memo>()
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

        boardViewModel.allUserWrittenData.observe(viewLifecycleOwner) { memos ->
            boardRVAdapter.setBoardData(memos)
        }

        binding.btnAll.setOnClickListener {
            boardViewModel.allUserWrittenData.value?.let { memos ->
                boardRVAdapter.setBoardData(memos)
            }
        }

        binding.btnMy.setOnClickListener {
            boardViewModel.userWrittenData.value?.let { memos ->
                boardRVAdapter.setBoardData(memos)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}