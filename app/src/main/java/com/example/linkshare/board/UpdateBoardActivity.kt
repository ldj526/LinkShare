package com.example.linkshare.board

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.R
import com.example.linkshare.memo.WriteBoardFragment

class UpdateBoardActivity : AppCompatActivity() {

    private lateinit var key: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_board)

        key = intent.getStringExtra("key").toString()
        val writeBoardFragment = WriteBoardFragment.newInstance(true, key)

        // 프래그먼트를 컨테이너에 추가
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, writeBoardFragment)
            .commit()
    }
}