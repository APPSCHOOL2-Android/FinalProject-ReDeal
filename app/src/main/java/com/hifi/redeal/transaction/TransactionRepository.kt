package com.hifi.redeal.transaction

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.transaction.model.ClientData
import com.hifi.redeal.transaction.model.TransactionData

class TransactionRepository {

    companion object{

        fun setClientTransactionDataList(userIdx: String, newTransactionData: TransactionData, callback1: (Task<DocumentSnapshot>) -> Unit){
            var clientData = ClientData()
            var transactionList = mutableListOf<Long>()
            val db = Firebase.firestore
            db.collection("userData")
                .document(userIdx)
                .collection("clientData")
                .document("${newTransactionData.clientIdx}")
                .get()
                .addOnSuccessListener {
                    clientData = it.toObject<ClientData>()!!
                    transactionList = clientData.transactionIdxList?.toMutableList()!!
                    transactionList.add(newTransactionData.transactionIdx)
                    clientData.transactionIdxList = transactionList.toList()
                }.addOnSuccessListener {
                    db.collection("userData")
                        .document(userIdx)
                        .collection("clientData")
                        .document("${newTransactionData.clientIdx}")
                        .set(clientData)
                }.addOnCompleteListener(callback1)
        }
        fun setTransactionData(userIdx: String, transactionData: TransactionData, callback1: (Task<Void>) -> Unit){
            val db = Firebase.firestore
            db.collection("userData")
                .document(userIdx)
                .collection("transactionData")
                .document("${transactionData.transactionIdx}")
                .set(transactionData)
                .addOnCompleteListener(callback1)
        }
        fun getClientInfo(userIdx: String, clientIdx: Long, callback1: (Task<QuerySnapshot>) -> Unit){
            val db = Firebase.firestore
            db.collection("userData")
                .document(userIdx)
                .collection("clientData")
                .whereEqualTo("clientIdx", clientIdx)
                .get()
                .addOnCompleteListener(callback1)
        }

        fun getNextTransactionIdx(userIdx: String, callback: (Task<QuerySnapshot>) -> Unit){
            val db = Firebase.firestore
            val transactionDataRef = db.collection("userData")
                .document(userIdx)
                .collection("transactionData")

            transactionDataRef.orderBy("transactionIdx", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnCompleteListener(callback)
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