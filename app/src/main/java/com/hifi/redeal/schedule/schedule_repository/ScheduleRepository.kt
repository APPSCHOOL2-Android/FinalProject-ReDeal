package com.hifi.redeal.schedule.schedule_repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduleRepository {
    companion object{
        fun getUserDayOfSchedule(userIdx: String, date: String, callback1: (Task<QuerySnapshot>) -> Unit){
            val db = Firebase.firestore
            val scheduleRef = db.collection("userData")
                .document(userIdx)
                .collection("scheduleData")

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = sdf.parse(date)

            val startDate = Timestamp(formattedDate)
            val endDate = Timestamp(Date(formattedDate.time + 24 * 60 * 60 * 1000)) // 다음 날의 timestamp

            val query = scheduleRef
                .whereGreaterThanOrEqualTo("scheduleDeadlineTime", startDate)
                .whereLessThan("scheduleDeadlineTime", endDate)

            query.get().addOnCompleteListener(callback1)
        }
    }
}