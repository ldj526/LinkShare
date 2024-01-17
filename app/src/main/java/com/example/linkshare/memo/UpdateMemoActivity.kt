package com.example.linkshare.memo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.R

class UpdateMemoActivity : AppCompatActivity() {

    private lateinit var key: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_update)

        key = intent.getStringExtra("key").toString()
        val writeMemoFragment = WriteMemoFragment.newInstance(true, key)

        // 프래그먼트를 컨테이너에 추가
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, writeMemoFragment)
            .commit()
    }
}