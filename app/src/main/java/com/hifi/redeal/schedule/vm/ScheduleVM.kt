package com.hifi.redeal.schedule.vm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.hifi.redeal.schedule.model.ScheduleData
import com.hifi.redeal.schedule.schedule_repository.ScheduleRepository
import java.text.SimpleDateFormat
import java.util.Locale

class ScheduleVM: ViewModel() {

    //일정 관련 데이터
    var scheduleListVM = MutableLiveData<MutableList<ScheduleData>>()
    var tempScheduleList = mutableListOf<ScheduleData>()
    fun getUserDayOfSchedule(userIdx: String, date: String){
        tempScheduleList.clear()

        ScheduleRepository.getUserDayOfSchedule(userIdx, date){
            for (c1 in it.result){
                val clientIdx = c1["clientIdx"] as Long
                val isScheduleFinish = c1["isScheduleFinish"] as Boolean
                val isVisitSchedule = c1["isVisitSchedule"] as Boolean
                val scheduleContext = c1["scheduleContext"] as String
                var scheduleDataCreateTime = c1["scheduleDataCreateTime"] as Timestamp
                var scheduleDeadlineTime = c1["scheduleDeadlineTime"] as Timestamp

                val scheduleTitle = c1["scheduleTitle"] as String
                val newScheduleData = ScheduleData(clientIdx, isScheduleFinish, isVisitSchedule, scheduleContext,
                    scheduleDataCreateTime, scheduleDeadlineTime, scheduleTitle)
                tempScheduleList.add(newScheduleData)
            }
            scheduleListVM.value = tempScheduleList
        }
    }
}

//data class ScheduleData(val clientIdx: Int, val isScheduleFinish: Boolean, val isVisitSchedule: Boolean, val scheduleContext: String,
//                        val scheduleDataCreateTime: String, val scheduleDeadlineTime: String, val scheduleTitle: String)