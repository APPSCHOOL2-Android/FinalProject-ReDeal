package com.hifi.redeal.map.vm

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hifi.redeal.R
import com.hifi.redeal.map.model.ClientDataClass
import com.hifi.redeal.map.model.ScheduleDataClass
import com.hifi.redeal.map.repository.ClientRepository
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextStyle

class ClientViewModel : ViewModel() {
    var clientDataListByKeyWord = MutableLiveData<MutableList<ClientDataClass>>()
    var clientDataListAll = MutableLiveData<MutableList<ClientDataClass>>()
    var scheduleDataClassListByClient = MutableLiveData<MutableList<ScheduleDataClass>>()
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
        val scheduleTempList = mutableListOf<ScheduleDataClass>()
        ClientRepository.getClientListByUser(userIdx) {
            for (snapshot in it.result.documents) {
                Log.d("거래처 테스트1", snapshot.toObject(ClientDataClass::class.java).toString())
                var item = snapshot.toObject(ClientDataClass::class.java)
                tempList.add(item!!)
            }
            clientDataListAll.value = tempList
            tempList.forEach {c->
                ClientRepository.getScheduleListByClientAndUser(userIdx,c.clientIdx){
                    for (snapshot in it.result.documents) {
                        if (snapshot.getBoolean("isVisitSchedule")!!.equals(true) && snapshot.getBoolean("isScheduleFinish")!!.equals(true)) {
                            Log.d(
                                "일정 테스트1",
                                snapshot.toObject(ScheduleDataClass::class.java).toString()
                            )
                            var item = snapshot.toObject(ScheduleDataClass::class.java)
                            scheduleTempList.add(item!!)
                        }
                    }
                    scheduleDataClassListByClient.value = scheduleTempList
                    Log.d("일정 테스트2", scheduleDataClassListByClient.value.toString())
                }
            }
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

            tempList.forEach {c ->
                ClientRepository.searchAddr(c.clientAddress!!) {
                    val lat = it!!.get(0).y.toDouble()
                    val long = it!!.get(0).x.toDouble()
                    val latLng = LatLng.from(lat, long)

                    val style = LabelStyles.from(LabelStyle.from(R.drawable.blue_marker).setTextStyles(
                        LabelTextStyle.from(30, R.color.primary20, 1, Color.WHITE)));

                    labelList.add(
                        LabelOptions.from(latLng)
                            .setStyles(style).setTexts(c.clientName)
                    )
                    clientDataListLabel.value = labelList
                    Log.d("라벨3", clientDataListLabel.value.toString())
                }
            }


        }



    }

    fun resetClientListByKeyword() {
        clientDataListByKeyWord.value = mutableListOf<ClientDataClass>()
    }

    fun resetClientList() {
        clientDataListAll.value = mutableListOf<ClientDataClass>()

    }


    fun setSelectedButton(buttonId: Int) {
        selectedButtonId.value = buttonId
    }


}