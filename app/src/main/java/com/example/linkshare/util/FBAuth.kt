package com.example.linkshare.util

import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FBAuth {
    companion object {
        private lateinit var auth: FirebaseAuth

        // uid 값 가져오기
        fun getUid(): String {
            auth = FirebaseAuth.getInstance()
            return auth.currentUser?.uid.toString()
        }

        // 현재 시각 가져오기
        fun getTime(): String {
            val currentDateTime = Calendar.getInstance().time
            return SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDateTime)
        }
    }
}