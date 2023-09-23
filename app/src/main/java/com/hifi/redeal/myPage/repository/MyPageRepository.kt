package com.hifi.redeal.myPage.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

class MyPageRepository {
    companion object{
        fun getUserInfo(userIdx:String, callback1: (DocumentSnapshot) -> Unit){
            val db = Firebase.firestore
            val photoMemoRef = db.collection("userData")
                .document(userIdx)
            photoMemoRef.get()
                .addOnSuccessListener(callback1)
        }

        fun updateUserName(userIdx: String, userNewName:String, callback:() -> Unit) {
            val db = Firebase.firestore
            val userDocRef = db.collection("userData").document(userIdx)
            userDocRef.update("userName", userNewName).addOnSuccessListener {
                callback()
            }
        }

        fun addRequestMessage(message:String){
            val db = Firebase.firestore
            val uuid = UUID.randomUUID().toString()
            val requestMessageRef = db.collection("requestMessage").document(uuid)
            val requestMessage = hashMapOf(
                "message" to message
            )
            requestMessageRef.set(requestMessage)
        }

    }
}