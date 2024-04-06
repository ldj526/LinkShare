package com.example.linkshare.util

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class FBUser {
    companion object {
        private val db = Firebase.firestore

        fun getUserNickname(uid: String, onSuccess: (String?) -> Unit, onFailure: (Exception) -> Unit) {
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val nickname = document.getString("nickname") // "nickname"은 사용자 문서의 닉네임 필드명
                        onSuccess(nickname)
                    } else {
                        Log.d("Firestore", "No such document")
                        onSuccess(null)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("Firestore", "get failed with ", exception)
                    onFailure(exception)
                }
        }
    }
}