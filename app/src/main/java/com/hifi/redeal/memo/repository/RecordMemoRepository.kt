package com.hifi.redeal.memo.repository

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class RecordMemoRepository {
    companion object{
        fun getRecordMemoAll(userIdx:Long, clientIdx:Long, callback1: (QuerySnapshot) -> Unit){
            val db = Firebase.firestore
            val photoMemoRef = db.collection("userData")
                .document("$userIdx")
                .collection("recordMemoData")
                .whereEqualTo("clientIdx", clientIdx)
            photoMemoRef.get()
                .addOnSuccessListener(callback1)
        }

        fun addRecordMemo(userIdx:Long, clientIdx:Long, recordMemoContext:String, uploadUri:Uri, audioFileName:String, callback:(Task<Void>) -> Unit){
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
                    val fileName = "audio_user${userIdx}_client${clientIdx}_audioMemo${recordMemoIdx}"
                    uploadAudio(userIdx, uploadUri, fileName){isSuccessful ->
                        if(isSuccessful){
                            val photoMemo = hashMapOf(
                                "clientIdx" to clientIdx,
                                "recordMemoContext" to recordMemoContext,
                                "recordMemoSrc" to fileName,
                                "recordMemoDate" to Timestamp(Date()),
                                "recordMemoIdx" to recordMemoIdx,
                                "recordMemoFilename" to audioFileName
                            )
                            recordMemoRef.document("$recordMemoIdx").set(photoMemo).addOnCompleteListener(callback)
                        }
                    }
                }
        }

        fun getPhotoMemoImgUrl(userIdx: Long, filename: String, callback: (String) -> Unit) {
            val storage = FirebaseStorage.getInstance()
            val fileRef = storage.reference.child("user${userIdx}/$filename")
            fileRef.downloadUrl.addOnCompleteListener{
                callback(it.result.toString())
            }
        }

        private fun uploadAudio(userIdx: Long, uploadUri: Uri, fileName:String, callback:(Boolean)-> Unit){
            val storage = FirebaseStorage.getInstance()
            val imageRef = storage.reference.child("user${userIdx}/$fileName")
            imageRef.putFile(uploadUri).addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    callback(true)
                }else{
                    callback(false)
                }
            }
        }
    }
}