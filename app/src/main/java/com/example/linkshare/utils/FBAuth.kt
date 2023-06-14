package com.example.linkshare.utils

import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

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
            val formatter = SimpleDateFormat("yy년 MM월 dd일 E요일 HH시 mm분", Locale.KOREA)
            val calendar = Calendar.getInstance()
            formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            return formatter.format(calendar.time)
        }
    }
}