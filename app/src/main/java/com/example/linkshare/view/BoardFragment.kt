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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkshare.R
import com.example.linkshare.board.BoardActivity
import com.example.linkshare.board.BoardModel
import com.example.linkshare.board.BoardRVAdapter
import com.example.linkshare.board.BoardWriteActivity
import com.example.linkshare.databinding.FragmentBoardBinding
import com.example.linkshare.utils.FBAuth
import com.example.linkshare.utils.FBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class BoardFragment : Fragment() {

    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!
    private val boardDataList = mutableListOf<BoardModel>()
    private val boardKeyList = mutableListOf<String>()
    private lateinit var boardRVAdapter: BoardRVAdapter

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
                intent.putExtra("key", boardKeyList[position])
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
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 중복 방지
                boardDataList.clear()
                // Get Post object and use the values to update the UI
                for (dataModel in dataSnapshot.children) {
                    // BoardModel 형식의 데이터 받기
                    val item = dataModel.getValue(BoardModel::class.java)
                    if (item!!.category == category) {
                        boardDataList.add(item!!)
                        boardKeyList.add(dataModel.key.toString())
                    }
                }
                // 최신 글이 가장 위로
                boardKeyList.reverse()
                boardDataList.reverse()
                // Sync
                boardRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        FBRef.boardList.addValueEventListener(postListener)
    }

    // 모든 데이터 값 가져오기
    private fun getFBBoardDataAll() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 중복 방지
                boardDataList.clear()
                // Get Post object and use the values to update the UI
                for (dataModel in dataSnapshot.children) {
                    // BoardModel 형식의 데이터 받기
                    val item = dataModel.getValue(BoardModel::class.java)
                    boardDataList.add(item!!)
                    boardKeyList.add(dataModel.key.toString())
                }
                // 최신 글이 가장 위로
                boardKeyList.reverse()
                boardDataList.reverse()
                // Sync
                boardRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        FBRef.boardList.addValueEventListener(postListener)
    }

    private fun getFBBoardDataEqualUid() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 중복 방지
                boardDataList.clear()
                // Get Post object and use the values to update the UI
                for (dataModel in dataSnapshot.children) {
                    // BoardModel 형식의 데이터 받기
                    val item = dataModel.getValue(BoardModel::class.java)

                    val myUid = FBAuth.getUid()
                    val writeUid = item!!.uid

                    // 내가 쓴 글 일 경우에만 list에 추가
                    if (myUid == writeUid) {
                        boardDataList.add(item!!)
                        boardKeyList.add(dataModel.key.toString())
                    }
                }
                // 최신 글이 가장 위로
                boardKeyList.reverse()
                boardDataList.reverse()
                // Sync
                boardRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        FBRef.boardList.addValueEventListener(postListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}