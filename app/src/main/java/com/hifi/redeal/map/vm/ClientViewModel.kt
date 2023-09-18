package com.hifi.redeal.map.vm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hifi.redeal.map.model.ClientDataClass
import com.hifi.redeal.map.repository.ClientRepository

class ClientViewModel : ViewModel() {
    var clientDataListByKeyWord = MutableLiveData<MutableList<ClientDataClass>>()
    var clientDataListAll = MutableLiveData<MutableList<ClientDataClass>>()
    init{
        clientDataListByKeyWord.value = mutableListOf<ClientDataClass>()
    }

    fun getClientListByKeyword(userIdx : String, keyword:String){

        val tempList = mutableListOf<ClientDataClass>()

        ClientRepository.getClientListByUser(userIdx){
                for (snapshot in it.result.documents) {
                   Log.d("검색 테스트1",snapshot.toObject(ClientDataClass::class.java).toString())
                    if (snapshot.getString("clientName")!!.contains(keyword)) {
                        var item = snapshot.toObject(ClientDataClass::class.java)
                        tempList.add(item!!)
                        Log.d("검색 테스트",tempList.toString())
                    }
                }
            clientDataListByKeyWord.value = tempList
        }

    }

    fun getClientListAll(userIdx : String){
        val tempList = mutableListOf<ClientDataClass>()
        ClientRepository.getClientListByUser(userIdx){
            for (snapshot in it.result.documents) {
                Log.d("거래처 테스트1",snapshot.toObject(ClientDataClass::class.java).toString())
                var item = snapshot.toObject(ClientDataClass::class.java)
                tempList.add(item!!)
            }
            clientDataListAll.value = tempList
        }

    }

    fun resetClientList(){
        clientDataListByKeyWord.value= mutableListOf<ClientDataClass>()
    }


}