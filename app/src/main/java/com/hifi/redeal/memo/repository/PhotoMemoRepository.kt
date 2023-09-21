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

class PhotoMemoRepository {
    companion object{
        fun getPhotoMemoAll(userIdx:String, clientIdx:Long, callback1: (QuerySnapshot) -> Unit){
            val db = Firebase.firestore
            val photoMemoRef = db.collection("userData")
                .document("$userIdx")
                .collection("photoMemoData")
                .whereEqualTo("clientIdx", clientIdx)
            photoMemoRef.get()
                .addOnSuccessListener(callback1)
        }

        fun addPhotoMemo(userIdx:String, clientIdx:Long, photoMemoContext:String, uriList:MutableList<Uri>, callback:(Task<Void>) -> Unit){
            val db = Firebase.firestore
            val photoMemoRef = db.collection("userData")
                .document("$userIdx")
                .collection("photoMemoData")
            photoMemoRef
                .orderBy("photoMemoIdx", Query.Direction.DESCENDING)
                .limit(1)
                .get().addOnSuccessListener{querySnapshot ->
                    val photoMemoIdx = if(!querySnapshot.isEmpty){
                        querySnapshot.documents[0].getLong("photoMemoIdx")!! + 1
                    }else{
                        1
                    }
                    var uploadCnt = 0
                    val fileNameList = mutableListOf<String>()
                    for(i in 0 until uriList.size){
                        val fileName = "image_user${userIdx}_client${clientIdx}_photoMemo${photoMemoIdx}_$i"
                        uploadImage(userIdx, uriList[i], fileName){isSuccessful ->
                            uploadCnt++
                            if(isSuccessful){
                                fileNameList.add(fileName)
                                if(uploadCnt == uriList.size){
                                    val photoMemo = hashMapOf(
                                        "clientIdx" to clientIdx,
                                        "photoMemoContext" to photoMemoContext,
                                        "photoMemoSrcArr" to fileNameList,
                                        "photoMemoDate" to Timestamp(Date()),
                                        "photoMemoIdx" to photoMemoIdx
                                    )
                                    photoMemoRef.document("$photoMemoIdx").set(photoMemo).addOnCompleteListener(callback)
                                }
                            }
                        }
                    }
                }
        }

        fun getPhotoMemoImgUrl(userIdx: String, filename: String, callback: (String) -> Unit) {
            val storage = FirebaseStorage.getInstance()
            val fileRef = storage.reference.child("user${userIdx}/$filename")
            fileRef.downloadUrl.addOnCompleteListener{
                callback(it.result.toString())
            }
        }

        private fun uploadImage(userIdx: String, uploadUri: Uri, fileName:String, callback:(Boolean)-> Unit){
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