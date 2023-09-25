package com.hifi.redeal.memo.repository

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date

class RecordMemoRepository {
    companion object{
        fun getRecordMemoAll(userIdx:String, clientIdx:Long, callback1: (QuerySnapshot) -> Unit){
            val db = Firebase.firestore
            val photoMemoRef = db.collection("userData")
                .document("$userIdx")
                .collection("recordMemoData")
                .whereEqualTo("clientIdx", clientIdx)
            photoMemoRef.get()
                .addOnSuccessListener(callback1)
        }

        fun addRecordMemo(userIdx:String, clientIdx:Long, recordMemoContext:String, audioFileUri:Uri, audioFileName:String, callback:(Task<Void>) -> Unit){
            val db = Firebase.firestore
            val recordMemoRef = db.collection("userData")
                .document("$userIdx")
                .collection("recordMemoData")
            recordMemoRef
                .orderBy("recordMemoIdx", Query.Direction.DESCENDING)
                .limit(1)
                .get().addOnSuccessListener{querySnapshot ->
                    val recordMemoIdx = if(!querySnapshot.isEmpty){
                        querySnapshot.documents[0].getLong("recordMemoIdx")!! + 1
                    }else{
                        1
                    }
                    val photoMemo = hashMapOf(
                        "clientIdx" to clientIdx,
                        "recordMemoContext" to recordMemoContext,
                        "recordMemoSrc" to audioFileUri,
                        "recordMemoDate" to Timestamp(Date()),
                        "recordMemoIdx" to recordMemoIdx,
                        "recordMemoFilename" to audioFileName
                    )
                    recordMemoRef.document("$recordMemoIdx").set(photoMemo).addOnCompleteListener(callback)
                }
        }
    }
}