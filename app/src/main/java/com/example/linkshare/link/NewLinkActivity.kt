package com.example.linkshare.link

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.linkshare.R

class NewLinkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_link_new)

        val writeLinkFragment = WriteLinkFragment.newInstance(false, "")

        // 프래그먼트를 컨테이너에 추가
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, writeLinkFragment)
            .commit()
    }
}