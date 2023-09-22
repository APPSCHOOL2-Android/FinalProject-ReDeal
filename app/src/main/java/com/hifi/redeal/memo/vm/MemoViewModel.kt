package com.hifi.redeal.memo.vm

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.hifi.redeal.memo.model.UserPhotoMemoData
import com.hifi.redeal.memo.model.UserRecordMemoData
import com.hifi.redeal.memo.repository.MemoRepository
import java.io.File

class MemoViewModel : ViewModel(){
    val userPhotoMemoList = MutableLiveData<List<UserPhotoMemoData>>()
    val userRecordMemoList = MutableLiveData<List<UserRecordMemoData>>()

    init{
        userPhotoMemoList.value = listOf<UserPhotoMemoData>()
        userRecordMemoList.value = listOf<UserRecordMemoData>()
    }
    fun getUserPhotoMemoList(userIdx:String){
        MemoRepository.getUserPhotoMemoAll(userIdx){ querySnapshot ->
            val photoMemoData = mutableListOf<UserPhotoMemoData>()
            for(document in querySnapshot){
                val clientIdx = document.get("clientIdx") as Long
                val context = document.get("photoMemoContext") as String
                val date = document.get("photoMemoDate") as Timestamp
                val srcArr = document.get("photoMemoSrcArr") as List<String>
                val newPhotoMemo = UserPhotoMemoData(clientIdx, context, date.seconds + 32400, srcArr)
                photoMemoData.add(newPhotoMemo)
            }
            userPhotoMemoList.postValue(photoMemoData)
        }
    }

    fun getUserRecordMemoList(userIdx:String, mainContext: Context){
        MemoRepository.getUserRecordMemoAll(userIdx){ querySnapshot ->
            val userRecordMemoData = mutableListOf<UserRecordMemoData>()
            for(document in querySnapshot){
                val clientIdx = document.get("clientIdx") as Long
                val context = document.get("recordMemoContext") as String
                val date = document.get("recordMemoDate") as Timestamp
                val audioFilename = document.get("recordMemoFilename") as String
                val fileLocation = File(mainContext.getExternalFilesDir(null), "recordings")
                val recordFileLocation = File(fileLocation, "$audioFilename")
                var audioFileUri: Uri? = null
                if(recordFileLocation.exists()){
                    audioFileUri = Uri.fromFile(recordFileLocation)
                }
                val newPhotoMemo = UserRecordMemoData(clientIdx, context, date.seconds + 32400, audioFileUri, audioFilename)
                userRecordMemoData.add(newPhotoMemo)
            }
            userRecordMemoData.reverse()
            userRecordMemoList.postValue(userRecordMemoData)
        }
    }
}