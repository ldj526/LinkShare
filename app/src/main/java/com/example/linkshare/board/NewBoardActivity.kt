package com.example.linkshare.board

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.R
import com.example.linkshare.memo.WriteBoardFragment

class NewBoardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_board)

        val writeBoardFragment = WriteBoardFragment.newInstance(false, "")

        // 프래그먼트를 컨테이너에 추가
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, writeBoardFragment)
            .commit()
    }
}