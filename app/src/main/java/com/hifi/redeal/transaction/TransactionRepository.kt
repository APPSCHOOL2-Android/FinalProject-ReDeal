package com.hifi.redeal.transaction

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class TransactionRepository {

    companion object{
        fun getClientInfo(userIdx: String, clientIdx: Long, callback1: (Task<QuerySnapshot>) -> Unit){
            val db = Firebase.firestore
            db.collection("userData")
                .document(userIdx)
                .collection("clientData")
                .whereEqualTo("clientIdx", clientIdx)
                .get()
                .addOnCompleteListener(callback1)
        }

        fun getAllTransactionData(userIdx: String, callback1: (Task<QuerySnapshot>) -> Unit, callback2: (Task<QuerySnapshot>) -> Unit){
            val db = Firebase.firestore
            db.collection("userData")
                .document(userIdx)
                .collection("transactionData")
                .get()
                .addOnCompleteListener(callback1)
                .addOnCompleteListener(callback2)
        }

    }
}