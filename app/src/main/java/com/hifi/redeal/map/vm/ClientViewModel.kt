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
import com.kakao.vectormap.MapConfig
import com.kakao.vectormap.MapLogger
import com.kakao.vectormap.MapOverlay
import com.kakao.vectormap.MapReadyCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.RoadViewRequest.Marker
import com.kakao.vectormap.graphics.MapRenderer
import com.kakao.vectormap.internal.MapViewHolder
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelLayerOptions
import com.kakao.vectormap.label.LabelManager
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextStyle
import java.text.SimpleDateFormat
import java.util.Calendar

class ClientViewModel : ViewModel() {
    var clientDataListByKeyWord = MutableLiveData<MutableList<ClientDataClass>>()
    var clientDataListAll = MutableLiveData<MutableList<ClientDataClass>>()
    var currentAddress = MutableLiveData<LatLng>()
    var clientDataListTemp = MutableLiveData<MutableList<ClientDataClass>>()
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
        ClientRepository.getClientListByUser(userIdx) {
            for (snapshot in it.result.documents) {
                Log.d("거래처 테스트1", snapshot.toObject(ClientDataClass::class.java).toString())
                var item = snapshot.toObject(ClientDataClass::class.java)
                tempList.add(item!!)
            }
            clientDataListAll.value = tempList
        }

    }

    fun getClientListTodayVisit(userIdx: String) {
        val clientTempList = mutableListOf<ClientDataClass>()
        ClientRepository.getClientListByUser(userIdx) {
            for (snapshot in it.result.documents) {
                Log.d("거래처정 테스트1", snapshot.toObject(ClientDataClass::class.java).toString())
                var item = snapshot.toObject(ClientDataClass::class.java)
                ClientRepository.getVisitScheduleListByClientAndUser(userIdx, item!!.clientIdx) {
                    Log.d("거래처정 테스트2", item.clientIdx.toString())
                    var i = 0;
                    while (i <= it.result.documents.size - 1) {
                        val snapshot = it.result.documents.get(i)
                        Log.d("거래처정 테스트3", snapshot.toObject(ScheduleDataClass::class.java).toString())
                        val dateFormat = SimpleDateFormat("yyyy.MM.dd")
                        val tempSnapShotDate =
                            dateFormat.format(snapshot.getDate("scheduleDeadlineTime"))
                        val today = dateFormat.format(Calendar.getInstance().time)
                        Log.d("일정정1", tempSnapShotDate)
                        Log.d("일정정2", today)
                        if (tempSnapShotDate.equals(today)
                        ) {
                            Log.d(
                                "일정정3",
                                snapshot.toObject(ScheduleDataClass::class.java).toString()
                            )
                            clientTempList.add(item)
                            i++
                        } else {
                            i++
                        }
                        Log.d("일정정4", clientTempList.toString())
                    }
                    Log.d("일정정5", clientTempList.toString())
                    clientDataListAll.value = clientTempList
                }
            }
        }
    }

    fun getClientListBookMark(userIdx: String){
        val tempList = mutableListOf<ClientDataClass>()
        ClientRepository.getClientListByUser(userIdx) {
            for (snapshot in it.result.documents) {
                Log.d("거래처 테스트1", snapshot.toObject(ClientDataClass::class.java).toString())
                var item = snapshot.toObject(ClientDataClass::class.java)
                tempList.add(item!!)
            }
            clientDataListAll.value = tempList.filter { it.isBookmark == true } as MutableList<ClientDataClass>
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

            tempList.forEach { c ->
                ClientRepository.searchAddr(c.clientAddress!!) {
                    val lat = it!!.get(0).y.toDouble()
                    val long = it!!.get(0).x.toDouble()
                    val latLng = LatLng.from(lat, long)

                    val style = LabelStyles.from(
                        LabelStyle.from(R.drawable.blue_marker).setTextStyles(
                            LabelTextStyle.from(30, R.color.primary20, 1, Color.WHITE)
                        )
                    );

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


    fun setSelectedButton(buttonId: Int) {
        selectedButtonId.value = buttonId
    }


}