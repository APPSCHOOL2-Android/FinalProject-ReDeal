package com.hifi.redeal.memo.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class MemoRepository {
    companion object{

        fun getUserRecordMemoAll(userIdx:String, callback1: (QuerySnapshot) -> Unit){
            val db = Firebase.firestore
            val photoMemoRef = db.collection("userData")
                .document("$userIdx")
                .collection("recordMemoData")
            photoMemoRef.get()
                .addOnSuccessListener(callback1)
        }

        fun getUserPhotoMemoAll(userIdx:String, callback1: (QuerySnapshot) -> Unit){
            val db = Firebase.firestore
            val photoMemoRef = db.collection("userData")
                .document(userIdx)
                .collection("photoMemoData")
            photoMemoRef.get()
                .addOnSuccessListener(callback1)
        }

        fun getUserMemoClientInfo(userIdx:String, clientIdx:Long, callback1: (DocumentSnapshot) -> Unit){
            val db = Firebase.firestore
            val photoMemoRef = db.collection("userData")
                .document(userIdx)
                .collection("clientData")
                .document("$clientIdx")
            photoMemoRef.get()
                .addOnSuccessListener(callback1)
        }

        fun getUserPhotoMemoImgUrl(userIdx: String, filename: String, callback: (String) -> Unit) {
            val storage = FirebaseStorage.getInstance()
            val fileRef = storage.reference.child("user${userIdx}/$filename")
            fileRef.downloadUrl.addOnCompleteListener{
                callback(it.result.toString())
            }
        }
    }
}