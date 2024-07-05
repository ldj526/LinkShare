package com.example.linkshare.util

import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FBAuth {
    companion object {
        private lateinit var auth: FirebaseAuth

        // uid 값 가져오기
        fun getUid(): String {
            auth = FirebaseAuth.getInstance()
            return auth.currentUser?.uid.toString()
        }

        // 현재 시각 가져오기
        fun formatTimestamp(time: Long): String {
            val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREAN)
            val tz = TimeZone.getTimeZone("Asia/Seoul")
            dateFormat.timeZone = tz
            return dateFormat.format(Date(time))
        }

        // 현재 시각을 Timestamp로 가져오기
        fun getTimestamp(): Long {
            return System.currentTimeMillis()
        }
    }
}