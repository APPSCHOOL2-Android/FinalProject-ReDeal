package com.hifi.redeal.map.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.BuildConfig
import com.hifi.redeal.MainActivity
import com.hifi.redeal.map.model.AdmVO
import com.hifi.redeal.map.model.KakaoMapAPI
import com.hifi.redeal.map.model.Place
import com.hifi.redeal.map.model.RegionInfoAPI
import com.hifi.redeal.map.model.ResultSearchAddr
import com.hifi.redeal.map.model.ResultSearchRegion
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ClientRepository {
    companion object {
        fun getClientListByUser(userIdx: String, callback1: (Task<QuerySnapshot>) -> Unit) {
            val database = Firebase.firestore

            val clientDataRef =
                database.collection("userData").document(userIdx).collection("clientData")

            clientDataRef.get().addOnCompleteListener(callback1)

        }

        fun getScheduleListByClientAndUser(
            userIdx: String,
            clientIdx: Long,
            callback1: (Task<QuerySnapshot>) -> Unit
        ) {
            val database = Firebase.firestore

            val scheduleDataRef =
                database.collection("userData").document(userIdx).collection("scheduleData")
                    .whereEqualTo("clientIdx", clientIdx).orderBy("scheduleFinishTime")

            scheduleDataRef.get().addOnCompleteListener(callback1)

        }

        fun getVisitScheduleListByClientAndUser(
            userIdx: String,
            clientIdx: Long,
            callback1: (Task<QuerySnapshot>) -> Unit
        ) {
            val database = Firebase.firestore

            val scheduleDataRef =
                database.collection("userData").document(userIdx).collection("scheduleData")
                    .whereEqualTo("clientIdx", clientIdx).whereEqualTo("isVisitSchedule", true)

            scheduleDataRef.get().addOnCompleteListener(callback1)

        }


    }



}





