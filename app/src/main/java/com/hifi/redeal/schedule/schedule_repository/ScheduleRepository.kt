package com.hifi.redeal.schedule.schedule_repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduleRepository {
    companion object{

        fun updateUserDayOfScheduleState(userIdx: String, scheduleIdx: String, callback1: (Task<Transaction>) -> Unit){
            val db = Firebase.firestore
            val scheduleRef = db.collection("userData")
                .document(userIdx)
                .collection("scheduleData")
                .document(scheduleIdx)

            db.runTransaction {transaction ->
                val snapshot = transaction.get(scheduleRef)
                val newState = snapshot.getBoolean("isScheduleFinish")!!
                transaction.update(scheduleRef, "isScheduleFinish", !newState)
            }.addOnCompleteListener(callback1)

        }

        fun getClientInfo(userIdx: String, clinetIdx: Long, callback1: (Task<QuerySnapshot>) -> Unit){
            val db = Firebase.firestore
            val clientRef = db.collection("userData")
                .document(userIdx)
                .collection("clientData")
            val query = clientRef
                .whereEqualTo("clientIdx", clinetIdx)

            query.get().addOnCompleteListener(callback1)
        }
        fun getUserDayOfSchedule(userIdx: String, date: String, callback1: (Task<QuerySnapshot>) -> Unit, callback2: (Task<QuerySnapshot>) -> Unit){
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

            query.get().addOnCompleteListener(callback1).addOnCompleteListener(callback2)
        }
    }
}