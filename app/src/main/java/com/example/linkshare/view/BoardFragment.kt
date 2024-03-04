package com.example.linkshare.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkshare.board.Board
import com.example.linkshare.board.BoardActivity
import com.example.linkshare.board.BoardRVAdapter
import com.example.linkshare.board.NewBoardActivity
import com.example.linkshare.board.UpdateBoardActivity
import com.example.linkshare.databinding.FragmentBoardBinding
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.FBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class BoardFragment : Fragment() {

    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!
    private val boardList = mutableListOf<Board>()
    private val boardKeyList = mutableListOf<String>()
    private lateinit var boardRVAdapter: BoardRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoardBinding.inflate(inflater, container, false)

        boardRVAdapter = BoardRVAdapter(boardList)
        binding.rvBoard.adapter = boardRVAdapter
        binding.rvBoard.layoutManager = LinearLayoutManager(context)

        boardRVAdapter.setItemClickListener(object : BoardRVAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val intent = Intent(context, BoardActivity::class.java)
                intent.putExtra("key", boardKeyList[position])   // key 값 전달
                startActivity(intent)
            }
        })

        binding.fabNewBoard.setOnClickListener {
            val intent = Intent(context, NewBoardActivity::class.java)
            startActivity(intent)
        }

        getFBBoardData()

        // 전체보기 클릭 시
        binding.btnAll.setOnClickListener {
            getFBBoardData()
        }

        // 내 글 보기 클릭 시
        binding.btnMy.setOnClickListener {
            getFBBoardDataEqualUid()
        }

        return binding.root
    }

    // Firebase database로부터 data 가져오기
    private fun getFBBoardData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 중복 방지
                boardList.clear()
                // Get Post object and use the values to update the UI
                for (dataModel in snapshot.children) {
                    // Memo 형식의 데이터 받기
                    val item = dataModel.getValue(Board::class.java)
                    boardList.add(item!!)
                    boardKeyList.add(dataModel.key.toString())
                }
                // 최신 글이 가장 위로
                boardList.reverse()
                boardKeyList.reverse()
                // Sync
                boardRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("BoardFragment", "실패")
            }
        }
        FBRef.boardCategory.addValueEventListener(postListener)
    }

    // Firebase에서 내가 쓴 게시글 목록만 가져오기
    private fun getFBBoardDataEqualUid() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 중복 방지
                boardList.clear()
                // Get Post object and use the values to update the UI
                for (dataModel in dataSnapshot.children) {
                    // BoardModel 형식의 데이터 받기
                    val item = dataModel.getValue(Board::class.java)

                    val myUid = FBAuth.getUid()
                    val writeUid = item!!.uid

                    // 내가 쓴 글 일 경우에만 list에 추가
                    if (myUid == writeUid) {
                        boardList.add(item)
                        boardKeyList.add(dataModel.key.toString())
                    }
                }
                // 최신 글이 가장 위로
                boardKeyList.reverse()
                boardList.reverse()
                // Sync
                boardRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        FBRef.boardCategory.addValueEventListener(postListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}