package com.example.linkshare.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkshare.R
import com.example.linkshare.board.*
import com.example.linkshare.databinding.FragmentBoardBinding

class BoardFragment : Fragment() {

    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!
    private val boardDataList = mutableListOf<BoardModel>()
    private lateinit var boardRVAdapter: BoardRVAdapter
    private val viewModel by lazy { ViewModelProvider(this)[BoardViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBoardBinding.inflate(inflater, container, false)

        // RecyclerView 연결
        boardRVAdapter = BoardRVAdapter(boardDataList)
        binding.boardRV.adapter = boardRVAdapter
        binding.boardRV.layoutManager = LinearLayoutManager(context)

        binding.fbAdd.setOnClickListener {
            val intent = Intent(context, BoardWriteActivity::class.java)
            startActivity(intent)
        }

        // RecyclerView Click
        boardRVAdapter.setItemClickListener(object : BoardRVAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val intent = Intent(context, BoardActivity::class.java)
                intent.putExtra("key", getKeyList()[position])
                startActivity(intent)
            }
        })

        binding.btnMyPost.setOnClickListener {
            getFBBoardDataEqualUid()
            binding.spCategory.setSelection(0)
        }

        // Spinner 연결
        val spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.seeType,
            android.R.layout.simple_spinner_item
        )

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spCategory.adapter = spinnerAdapter

        binding.spCategory.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    getFBBoardDataAll()
                } else {
                    getFBBoardData(position)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        return binding.root
    }

    // Category에 맞는 데이터 값 가져오기
    private fun getFBBoardData(category: Int) {
        viewModel.getCategoryData(category).observe(viewLifecycleOwner, Observer {
            boardRVAdapter.setData(it)
            boardRVAdapter.notifyDataSetChanged()
        })
    }

    // 모든 데이터 값 가져오기
    private fun getFBBoardDataAll() {
        viewModel.getAllData().observe(viewLifecycleOwner, Observer {
            boardRVAdapter.setData(it)
            boardRVAdapter.notifyDataSetChanged()
        })
    }

    // 자신의 id값과 일치하는 게시물 가져오기
    private fun getFBBoardDataEqualUid() {
        viewModel.getEqualUidData().observe(viewLifecycleOwner, Observer {
            boardRVAdapter.setData(it)
            boardRVAdapter.notifyDataSetChanged()
        })
    }

    // 게시판의 key 값 받아오기
    private fun getKeyList(): MutableList<String> {
        return viewModel.getKeyList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}