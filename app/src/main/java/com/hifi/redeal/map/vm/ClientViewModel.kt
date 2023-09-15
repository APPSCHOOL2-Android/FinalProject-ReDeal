package com.hifi.redeal.map.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hifi.redeal.map.model.ClientDataClass
import com.hifi.redeal.map.repository.ClientRepository

class ClientViewModel : ViewModel() {
    var clientDataListByKeyWord = MutableLiveData<MutableList<ClientDataClass>>()

    fun getClientListByKeyword(userIdx : String, keyword:String){

        val tempList = mutableListOf<ClientDataClass>()

        ClientRepository.getClientListByUser(userIdx, keyword){
                for (snapshot in it.result.documents) {
                    if (snapshot.getString("clientName")!!.contains(keyword)) {
                        var item = snapshot.toObject(ClientDataClass::class.java)
                        tempList.add(item!!)
                    }
                }
            clientDataListByKeyWord.value = tempList

        }
    }
}