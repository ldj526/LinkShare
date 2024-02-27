package com.example.linkshare.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkshare.board.Board
import com.example.linkshare.board.BoardRVAdapter
import com.example.linkshare.board.NewBoardActivity
import com.example.linkshare.board.UpdateBoardActivity
import com.example.linkshare.databinding.FragmentBoardBinding

class BoardFragment : Fragment() {

    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!
    private val boardDataList = mutableListOf<Board>()
    private lateinit var boardRVAdapter: BoardRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoardBinding.inflate(inflater, container, false)

        boardRVAdapter = BoardRVAdapter(boardDataList)
        binding.rvBoard.adapter = boardRVAdapter
        binding.rvBoard.layoutManager = LinearLayoutManager(context)

        boardRVAdapter.setItemClickListener(object :BoardRVAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val intent = Intent(context, UpdateBoardActivity::class.java)
                startActivity(intent)
            }
        })

        binding.fabNewBoard.setOnClickListener {
            val intent = Intent(context, NewBoardActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}