package com.hifi.redeal.memo.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.hifi.redeal.memo.model.RecordMemoData
import com.hifi.redeal.memo.repository.RecordMemoRepository

class RecordMemoViewModel : ViewModel() {
    val recordMemoList = MutableLiveData<List<RecordMemoData>>()

    init{
        recordMemoList.value = listOf<RecordMemoData>()
    }
    fun getRecordMemoList(userIdx:Long, clientIdx:Long){
        RecordMemoRepository.getRecordMemoAll(userIdx, clientIdx){ documentSnapshot ->
            val recordMemoData = mutableListOf<RecordMemoData>()
            for(item in documentSnapshot){
                val context = item.get("recordMemoContext") as String
                val date = item.get("recordMemoDate") as Timestamp
                val audioSrc = item.get("recordMemoSrc") as String
                val audioFilename = item.get("recordMemoFilename") as String
                val newPhotoMemo = RecordMemoData(null, context, date.seconds, audioSrc, audioFilename)
                recordMemoData.add(newPhotoMemo)
            }
            recordMemoList.postValue(recordMemoData)
        }
    }
}