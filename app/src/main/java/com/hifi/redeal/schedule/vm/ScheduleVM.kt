package com.hifi.redeal.schedule.vm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.hifi.redeal.schedule.model.ClientData
import com.hifi.redeal.schedule.model.ClientSimpleData
import com.hifi.redeal.schedule.model.ScheduleData
import com.hifi.redeal.schedule.model.ScheduleTotalData
import com.hifi.redeal.schedule.schedule_repository.ScheduleRepository
import java.time.LocalDate

class ScheduleVM: ViewModel() {

    //일정 관련 데이터
    var scheduleListVM = MutableLiveData<MutableList<ScheduleTotalData>>()
    var tempScheduleList = mutableListOf<ScheduleTotalData>()

    var userClientSimpleDataListVM = MutableLiveData<MutableList<ClientSimpleData>>()
    var tempUserClientSimpleDataList = mutableListOf<ClientSimpleData>()

    var userSelectClientSimpleData = MutableLiveData<ClientSimpleData>()
    lateinit var tempUserSelectClientSimpleData : ClientSimpleData

    var selectClientData = MutableLiveData<ClientData>()
    lateinit var tempSelectClientData : ClientData

    var clientLastVisitDate = MutableLiveData<Timestamp>()

    var selectScheduleIdx = 0L
    var selectScheduleData = MutableLiveData<ScheduleData>()

    var editScheduleData = MutableLiveData<ScheduleData>()

    // 사용자가 선택한 데이터
    var selectedScheduleIsVisit = true
    var selectDate = LocalDate.now()

    // 사용자가 선택했던 데이터들을 초기화 한다.
    fun selectClientDataClear() {
        userSelectClientSimpleData = MutableLiveData<ClientSimpleData>()
    }

    fun setEditScheduleData(){
        editScheduleData.postValue(selectScheduleData.value)
    }

    fun getSelectScheduleInfo(userIdx: String, scheduleIdx: String){
        ScheduleRepository.getSelectScheduleInfo(userIdx, scheduleIdx){
            Log.d("ttt","${it.result}")
            val tempSelectScheduleData = ScheduleData(
                it.result["scheduleIdx"] as Long,
                it.result["clientIdx"] as Long,
                it.result["isScheduleFinish"] as Boolean,
                it.result["isVisitSchedule"] as Boolean,
                it.result["scheduleContext"] as String,
                it.result["scheduleDataCreateTime"] as Timestamp,
                it.result["scheduleDeadlineTime"] as Timestamp,
                it.result["scheduleFinishTime"] as Timestamp,
                it.result["scheduleTitle"] as String,
            )
            selectScheduleData.postValue(tempSelectScheduleData)
        }
    }
    fun getSelectClientLastVisitDate (userIdx: String, clientIdx: Long){
        ScheduleRepository.getSelectClientLastVisitDate(userIdx, clientIdx){
            if(it.result.isEmpty){
                clientLastVisitDate.postValue(null)
            } else {
                for(c1 in it.result){
                    val lastTime = c1["scheduleFinishTime"] as Timestamp
                    clientLastVisitDate.postValue(lastTime)
                }
            }
        }
    }

    fun getClientInfo(userIdx: String, clientIdx: Long) {
        ScheduleRepository.getClientInfo(userIdx, clientIdx){
            for(c1 in it.result){
                val clientName = c1["clientName"] as String
                val clientManagerName = c1["clientManagerName"] as String
                val clientState = c1["clientState"] as Long
                val isBookmark = c1["isBookmark"] as Boolean
                val clientAddress = c1["clientAddress"] as String
                val clientCeoPhone = c1["clientCeoPhone"] as String
                val clientDetailAdd = c1["clientDetailAdd"] as String
                val clientExplain = c1["clientExplain"] as String
                val clientFaxNumber = c1["clientFaxNumber"] as String
                val clientManagerPhone = c1["clientManagerPhone"] as String
                val clientMemo = c1["clientMemo"] as String
                tempSelectClientData = ClientData(clientIdx,clientName, clientManagerName, clientState, isBookmark,clientAddress,clientCeoPhone,
                    clientDetailAdd, clientExplain, clientFaxNumber, clientManagerPhone, clientMemo)
                selectClientData.postValue(tempSelectClientData)
            }
        }
    }

    fun addUserSchedule(userIdx: String, scheduleData:ScheduleData, callback1: (Task<Void>) -> Unit){
        ScheduleRepository.getUserAllSchedule(userIdx,{
            for(c1 in it.result){
                val scheduleIdx = c1["scheduleIdx"] as Long
                scheduleData.scheduleIdx = scheduleIdx + 1L
            }
        },{
            ScheduleRepository.setUserSchedule(userIdx, scheduleData, callback1)
        })
    }
    fun getUserSelectClientInfo(userIdx: String, clientIdx:Long){
        ScheduleRepository.getUserSelectClientInfo(userIdx, clientIdx){
            val clientName = it.result["clientName"] as String
            val clientManagerName = it.result["clientManagerName"] as String
            val clientState = it.result["clientState"] as Long
            val isBookmark = it.result["isBookmark"] as Boolean
            tempUserSelectClientSimpleData = ClientSimpleData(clientIdx,clientName, clientManagerName, clientState, isBookmark)
            userSelectClientSimpleData.postValue(tempUserSelectClientSimpleData)
        }
    }


    fun getUserAllClientInfo(userIdx: String){
        tempUserClientSimpleDataList.clear()
        ScheduleRepository.getUserAllClientInfo(userIdx){
            for(c1 in it.result){
                var clientIdx = c1["clientIdx"] as Long
                var clientName = c1["clientName"] as String
                var clientManagerName = c1["clientManagerName"] as String
                var clientState = c1["clientState"] as Long
                var isBookmark = c1["isBookmark"] as Boolean
                tempUserClientSimpleDataList.add(ClientSimpleData(clientIdx, clientName, clientManagerName, clientState, isBookmark))
                userClientSimpleDataListVM.postValue(tempUserClientSimpleDataList)
            }
        }
    }
    fun getUserDayOfSchedule(userIdx: String, date: String){
        tempScheduleList.clear()

        ScheduleRepository.getUserDayOfSchedule(userIdx, date,{
            for (c1 in it.result){
                val scheduleIdx = c1["scheduleIdx"] as Long
                val clientIdx = c1["clientIdx"] as Long
                val isScheduleFinish = c1["isScheduleFinish"] as Boolean
                val isVisitSchedule = c1["isVisitSchedule"] as Boolean
                val scheduleTitle = c1["scheduleTitle"] as String
                val scheduleContext = c1["scheduleContext"] as String

                var scheduleDataCreateTime = c1["scheduleDataCreateTime"] as Timestamp
                var scheduleDeadlineTime = c1["scheduleDeadlineTime"] as Timestamp

                val newScheduleTotalData = ScheduleTotalData(scheduleIdx, clientIdx, isScheduleFinish, isVisitSchedule, scheduleTitle, scheduleContext,
                    scheduleDataCreateTime, scheduleDeadlineTime, null, null, null, null)

                tempScheduleList.add(newScheduleTotalData)
            }
            scheduleListVM.postValue(tempScheduleList)
        },{
            tempScheduleList.forEach {data ->
                ScheduleRepository.getClientInfo(userIdx, data.clientIdx){
                    for(c1 in it.result){
                        data.clientName = c1["clientName"] as String
                        data.clientManagerName = c1["clientManagerName"] as String
                        data.clientState = c1["clientState"] as Long
                        data.isBookmark = c1["isBookmark"] as Boolean
                        scheduleListVM.postValue(tempScheduleList)
                    }
                }
            }
        })
    }
}
