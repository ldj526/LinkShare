package com.example.linkshare.board

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.linkshare.utils.FBAuth
import com.example.linkshare.utils.FBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class BoardRepo {

    private val boardDataList = mutableListOf<BoardModel>()
    private val boardKeyList = mutableListOf<String>()
    private val mutableData = MutableLiveData<MutableList<BoardModel>>()

    // 모든 데이터 값 가져오기
    fun getAllData() : LiveData<MutableList<BoardModel>> {

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

                    mutableData.value = boardDataList
                }
                // 최신 글이 가장 위로
                boardKeyList.reverse()
                boardDataList.reverse()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        FBRef.boardList.addValueEventListener(postListener)
        return mutableData
    }

    // 자신의 id값과 일치하는 게시물 가져오기
    fun getEqualUidData(): LiveData<MutableList<BoardModel>>{
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

                    mutableData.value = boardDataList
                }
                // 최신 글이 가장 위로
                boardKeyList.reverse()
                boardDataList.reverse()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        FBRef.boardList.addValueEventListener(postListener)
        return mutableData
    }

    // Category에 맞는 데이터 값 가져오기
    fun getCategoryData(category: Int): LiveData<MutableList<BoardModel>> {
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
                    mutableData.value = boardDataList
                }
                // 최신 글이 가장 위로
                boardKeyList.reverse()
                boardDataList.reverse()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        FBRef.boardList.addValueEventListener(postListener)
        return mutableData
    }

    fun getKeyList(): MutableList<String> {
        return boardKeyList
    }
}