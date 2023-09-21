package com.hifi.redeal.account.repository

import android.util.Log
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.BuildConfig
import com.hifi.redeal.account.repository.model.ClientData
import com.hifi.redeal.account.repository.model.ContactData
import com.hifi.redeal.account.repository.model.Coordinate
import com.hifi.redeal.account.repository.model.FullAddrResponse
import com.hifi.redeal.account.repository.model.ScheduleData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder

class AccountDetailRepository {
    val db = Firebase.firestore

    fun getClient(userId: String, clientIdx: Long, callback: (ClientData?) -> Unit) {
        val userRef = db.collection("userData").document(userId).collection("clientData").document("$clientIdx")
            .get()
            .addOnSuccessListener {
                val client = it.toObject<ClientData>()

                var contactFetch = false
                var scheduleFetch = false

                db.collection("userData").document(userId).collection("contactData")
                    .whereEqualTo("clientIdx", client?.clientIdx)
                    .orderBy("contactDate", Query.Direction.DESCENDING).limit(1)
                    .get()
                    .addOnSuccessListener {
                        for (doc in it) {
                            client?.recentContactDate = doc.toObject<ContactData>().contactDate
                        }
                        contactFetch = true

                        if (contactFetch && scheduleFetch) {
                            callback(client)
                        }
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                    }

                db.collection("userData").document(userId).collection("scheduleData")
                    .whereEqualTo("clientIdx", client?.clientIdx)
                    .whereEqualTo("isVisitSchedule", true)
                    .whereEqualTo("isScheduleFinish", true)
                    .orderBy("scheduleFinishTime", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener {
                        for (doc in it) {
                            client?.recentVisitDate = doc.toObject<ScheduleData>().scheduleFinishTime
                        }
                        scheduleFetch = true

                        if (contactFetch && scheduleFetch) {
                            callback(client)
                        }
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                    }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun updateBookmark(userId: String, clientIdx: Long, isBookmark: Boolean) {
        db.collection("userData").document(userId).collection("clientData").document("$clientIdx")
            .update("isBookmark", isBookmark)
    }

    fun deleteClient(userId: String, clientIdx: Long, callback: () -> Unit) {
        db.collection("userData").document(userId).collection("clientData").document("$clientIdx")
            .delete()
            .addOnSuccessListener {
                callback()
            }
    }

    fun getFullAddrGeocoding(fullAddr: String, callback: (Coordinate?) -> Unit) {
        val encodedFullAddr = URLEncoder.encode(fullAddr, "UTF-8")

        RetrofitManager.tMapTapiService.tMapFullTextGeocoding(fullAddr = encodedFullAddr, appKey = BuildConfig.TMAP_APP_KEY)
            .enqueue(object : Callback<FullAddrResponse> {
                override fun onResponse(
                    call: Call<FullAddrResponse>,
                    response: Response<FullAddrResponse>
                ) {
                    if (response.isSuccessful) {
                        callback(response.body()?.coordinateInfo?.coordinate?.get(0))
                    }
                }

                override fun onFailure(call: Call<FullAddrResponse>, t: Throwable) {
                    Log.d("brudenell", "네트워크 통신 실패")
                }
            })
    }
}