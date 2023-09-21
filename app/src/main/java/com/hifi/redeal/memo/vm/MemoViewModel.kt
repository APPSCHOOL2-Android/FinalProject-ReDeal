package com.hifi.redeal.memo.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.hifi.redeal.memo.model.UserPhotoMemoData
import com.hifi.redeal.memo.repository.MemoRepository

class MemoViewModel : ViewModel(){
    val userPhotoMemoList = MutableLiveData<List<UserPhotoMemoData>>()

    init{
        userPhotoMemoList.value = listOf<UserPhotoMemoData>()
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
}