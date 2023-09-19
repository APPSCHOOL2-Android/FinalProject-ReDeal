package com.hifi.redeal.account.repository

import android.util.Log
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.account.model.ClientData
import com.hifi.redeal.account.model.ContactData
import com.hifi.redeal.account.model.ScheduleData
import java.util.UUID

class AccountListRepository {
    val db = Firebase.firestore

    fun getClientList(
        userId: Long,
        filter: Int,
        sortBy: Int,
        descending: Boolean,
        callback: (List<ClientData>) -> Unit
    ) {
        val userRef = db.collection("userData").document("$userId").collection("clientData")
        userRef.get().addOnSuccessListener { docs ->
            val clientList = docs.map {
                it.toObject<ClientData>()
            }

            var clientListSize = clientList.size
            var contactFetchCount = 0
            var scheduleFetchCount = 0

            val contactRef =
                db.collection("userData").document("$userId").collection("contactData")

            val scheduleRef =
                db.collection("userData").document("$userId").collection("scheduleData")

            for (client in clientList) {
                contactRef.whereEqualTo("clientIdx", client.clientIdx).orderBy("contactDate", Query.Direction.DESCENDING).limit(1)
                    .get()
                    .addOnSuccessListener {
                        for (doc in it) {
                            client.recentContactDate = doc.toObject<ContactData>().contactDate
                        }
                        contactFetchCount += 1

                        if (contactFetchCount == clientListSize && scheduleFetchCount == clientListSize) {
                            clientListFiltering(clientList, filter, sortBy, descending, callback)
                        }
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                    }

                scheduleRef
                    .whereEqualTo("clientIdx", client.clientIdx)
                    .whereEqualTo("isVisitSchedule", true)
                    .whereEqualTo("isScheduleFinish", true)
                    .orderBy("scheduleFinishTime", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener {
                        for (doc in it) {
                            client.recentVisitDate = doc.toObject<ScheduleData>().scheduleFinishTime
                        }
                        scheduleFetchCount += 1

                        if (contactFetchCount == clientListSize && scheduleFetchCount == clientListSize) {
                            clientListFiltering(clientList, filter, sortBy, descending, callback)
                        }
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                    }
            }
        }.addOnFailureListener {
            it.printStackTrace()
        }
    }

    fun clientListFiltering(
        clientList: List<ClientData>,
        filter: Int,
        sortBy: Int,
        descending: Boolean,
        callback: (List<ClientData>) -> Unit
    ) {
        val filteredClientList = when (filter) {
            0 -> {
                clientList.filter {
                    it.isBookmark ?: false
                }
            }
            1 -> {
                clientList.filter {
                    it.clientState == 1L
                }
            }
            2 -> {
                clientList.filter {
                    it.clientState == 2L
                }
            }
            3 -> {
                clientList.filter {
                    it.clientState == 3L
                }
            }
            else -> {
                clientList
            }
        }

        val resultClientList = when (sortBy) {
            0 -> {
                filteredClientList.sortedByDescending { it.viewCount }
            }
            1 -> {
                filteredClientList.sortedByDescending { it.recentVisitDate }
            }
            2 -> {
                filteredClientList.sortedByDescending { it.recentContactDate }
            }
            else -> {
                filteredClientList.sortedByDescending { it.clientIdx }
            }
        }

        if (!descending) {
            callback(resultClientList.reversed())
        } else {
            callback(resultClientList)
        }
    }

    fun incClientViewCount(userId: Long, clientIdx: Long, viewCount: Long) {
        if (clientIdx == 0L) {
            return
        }

        db.collection("userData").document("$userId").collection("clientData").document("$clientIdx")
            .update("viewCount", viewCount + 1)
    }
}