package com.hifi.redeal.schedule.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.hifi.redeal.schedule.schedule_repository.ScheduleRepository
import java.text.SimpleDateFormat
import java.util.Locale

class ScheduleVM: ViewModel() {
    fun getUserDayOfSchedule(userIdx: String, date: String){
        ScheduleRepository.getUserDayOfSchedule(userIdx, date){
            for (c1 in it.result){
                Log.d("ttt","${c1.data.values}")

                var getTime = c1.data["scheduleDeadlineTime"] as Timestamp
                val date = getTime.toDate()
                val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                val formattedDate = sdf.format(date)
                getTime.toDate()

                Log.d("ttt","변환된 시간 값 : $formattedDate")
            }
        }
    }
}