package com.hifi.redeal.memo.vm

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.hifi.redeal.memo.model.RecordMemoData
import com.hifi.redeal.memo.repository.RecordMemoRepository
import java.io.File

class RecordMemoViewModel : ViewModel() {
    val recordMemoList = MutableLiveData<List<RecordMemoData>>()

    init{
        recordMemoList.value = listOf<RecordMemoData>()
    }
    fun getRecordMemoList(userIdx:Long, clientIdx:Long, mainContext:Context){
        RecordMemoRepository.getRecordMemoAll(userIdx, clientIdx){ documentSnapshot ->
            val recordMemoData = mutableListOf<RecordMemoData>()
            for(item in documentSnapshot){
                val context = item.get("recordMemoContext") as String
                val date = item.get("recordMemoDate") as Timestamp
                val audioSrc = item.get("recordMemoSrc") as String
                val audioFilename = item.get("recordMemoFilename") as String
                val fileLocation = File(mainContext.getExternalFilesDir(null), "recordings")
                val recordFileLocation = File(fileLocation, "$audioFilename")
                var audioFileUri:Uri? = null
                if(recordFileLocation.exists()){
                    audioFileUri = Uri.fromFile(recordFileLocation)
                }
                Log.d("testaaa", "$audioFileUri")
                val newPhotoMemo = RecordMemoData(null, context, date.seconds + 32400, audioSrc, audioFilename, audioFileUri)
                recordMemoData.add(newPhotoMemo)
            }
            recordMemoList.postValue(recordMemoData)
        }
    }
}