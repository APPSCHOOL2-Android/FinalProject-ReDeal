package com.hifi.redeal.map.vm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hifi.redeal.R
import com.hifi.redeal.map.model.ClientDataClass
import com.hifi.redeal.map.repository.ClientRepository
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions

class ClientViewModel : ViewModel() {
    var clientDataListByKeyWord = MutableLiveData<MutableList<ClientDataClass>>()
    var clientDataListAll = MutableLiveData<MutableList<ClientDataClass>>()
    var clientDataListLabel = MutableLiveData<MutableList<LabelOptions>>()
    val selectedButtonId = MutableLiveData<Int>()

    init {
        clientDataListByKeyWord.value = mutableListOf<ClientDataClass>()
        selectedButtonId.value = R.id.mapBottomSheetTabAll
    }

    fun getClientListByKeyword(userIdx: String, keyword: String) {

        val tempList = mutableListOf<ClientDataClass>()

        ClientRepository.getClientListByUser(userIdx) {
            for (snapshot in it.result.documents) {
                Log.d("검색 테스트1", snapshot.toObject(ClientDataClass::class.java).toString())
                if (snapshot.getString("clientName")!!.contains(keyword)) {
                    var item = snapshot.toObject(ClientDataClass::class.java)
                    tempList.add(item!!)
                    Log.d("검색 테스트", tempList.toString())
                }
            }
            clientDataListByKeyWord.value = tempList
        }

    }

    fun getClientListAll(userIdx: String) {
        val tempList = mutableListOf<ClientDataClass>()
        ClientRepository.getClientListByUser(userIdx) {
            for (snapshot in it.result.documents) {
                Log.d("거래처 테스트1", snapshot.toObject(ClientDataClass::class.java).toString())
                var item = snapshot.toObject(ClientDataClass::class.java)
                tempList.add(item!!)
            }
            clientDataListAll.value = tempList
        }

    }

    fun getClientListLabel(userIdx: String) {
        val tempList = mutableListOf<ClientDataClass>()
        val labelList = mutableListOf<LabelOptions>()
        ClientRepository.getClientListByUser(userIdx) {
            for (snapshot in it.result.documents) {
                Log.d("거래처 테스트1", snapshot.toObject(ClientDataClass::class.java).toString())
                var item = snapshot.toObject(ClientDataClass::class.java)
                tempList.add(item!!)
            }

            tempList.forEach {
                ClientRepository.searchAddr(it.clientAddress!!) {
                    val lat = it!!.get(0).y.toDouble()
                    val long = it!!.get(0).x.toDouble()
                    val latLng = LatLng.from(lat, long)
                    labelList.add(
                        LabelOptions.from(latLng)
                            .setStyles(R.drawable.blue_marker)
                    )
                    clientDataListLabel.value = labelList
                    Log.d("라벨3", clientDataListLabel.value.toString())
                }
            }


        }



    }

    fun resetClientListByKeyword() {
        clientDataListAll.value = mutableListOf<ClientDataClass>()
    }

    fun resetClientList() {
        clientDataListByKeyWord.value = mutableListOf<ClientDataClass>()
    }


    fun setSelectedButton(buttonId: Int) {
        selectedButtonId.value = buttonId
    }


}