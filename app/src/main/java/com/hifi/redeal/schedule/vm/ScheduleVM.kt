package com.hifi.redeal.schedule.vm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.hifi.redeal.schedule.model.ScheduleData
import com.hifi.redeal.schedule.model.ScheduleTotalData
import com.hifi.redeal.schedule.schedule_repository.ScheduleRepository
import java.text.SimpleDateFormat
import java.util.Locale

class ScheduleVM: ViewModel() {

    //일정 관련 데이터
    var scheduleListVM = MutableLiveData<MutableList<ScheduleTotalData>>()
    var tempScheduleList = mutableListOf<ScheduleTotalData>()
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
            scheduleListVM.value = tempScheduleList
        },{
            tempScheduleList.forEach {data ->
                ScheduleRepository.getClientInfo(userIdx, data.clientIdx){
                    for(c1 in it.result){
                        data.clientName = c1["clientName"] as String
                        data.clientManagerName = c1["clientManagerName"] as String
                        data.clientState = c1["clientState"] as Long
                        data.isBookmark = c1["isBookmark"] as Boolean
                        scheduleListVM.value = tempScheduleList
                    }
                }
            }
        })
    }
}
