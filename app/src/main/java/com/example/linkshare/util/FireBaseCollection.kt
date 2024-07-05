package com.example.linkshare.util

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FireBaseCollection {

    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val linkCollection: CollectionReference by lazy { firestore.collection("links") }
    val sharedLinkCollection: CollectionReference by lazy { firestore.collection("sharedLinks") }
    val commentCollection: CollectionReference by lazy { firestore.collection("comments") }
    val userSearchCollection: CollectionReference by lazy { firestore.collection("users") }
    val popularSearchCollection: CollectionReference by lazy { firestore.collection("popular_search_queries") }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    // 사용자 링크 컬렉션 참조
    fun getUserLinksCollection(uid: String): CollectionReference {
        return linkCollection.document(uid).collection("userLinks")
    }

    // 사용자 댓글 컬렉션 참조
    fun getUserCommentsCollection(linkId: String): CollectionReference {
        return commentCollection.document(linkId).collection("comments")
    }

    // 사용자 최근 검색어 기록
    fun getUserCurrentSearchCollection(uid: String): CollectionReference {
        return userSearchCollection.document(uid).collection("search_queries")
    }

    // 모든 사용자 검색 기록
    fun getAllUserSearchCollection(uid: String): CollectionReference {
        return userSearchCollection.document(uid).collection("search_history")
    }
}