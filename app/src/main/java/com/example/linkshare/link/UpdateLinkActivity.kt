package com.example.linkshare.link

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.R

class UpdateLinkActivity : AppCompatActivity() {

    private lateinit var key: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_link_update)

        key = intent.getStringExtra("key").toString()
        val writeLinkFragment = WriteLinkFragment.newInstance(true, key)

        // 프래그먼트를 컨테이너에 추가
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, writeLinkFragment)
            .commit()
    }
}